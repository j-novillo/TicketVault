package com.ticketvault.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketvault.classification.CategoryDefinition
import com.ticketvault.classification.ProductCategory
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.ProductClassificationRepository
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.domain.usecase.AssignTicketStoreUseCase
import com.ticketvault.domain.usecase.ClassifyProductsUseCase
import com.ticketvault.domain.usecase.ExtractTicketDataUseCase
import com.ticketvault.domain.usecase.SaveReviewedTicketUseCase
import com.ticketvault.domain.usecase.TicketExtraction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class TicketReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val extractTicketDataUseCase: ExtractTicketDataUseCase,
    private val saveReviewedTicketUseCase: SaveReviewedTicketUseCase,
    private val assignTicketStoreUseCase: AssignTicketStoreUseCase,
    private val classifyProductsUseCase: ClassifyProductsUseCase,
    private val classificationRepository: ProductClassificationRepository,
    storeRepository: StoreRepository,
) : ViewModel() {

    private val ticketId: Long = checkNotNull(savedStateHandle[TICKET_ID_ARG])
    private var nextItemKey = 0L

    private val _uiState = MutableStateFlow<TicketReviewUiState>(TicketReviewUiState.Loading)
    val uiState: StateFlow<TicketReviewUiState> = _uiState.asStateFlow()

    val stores: StateFlow<List<Store>> = storeRepository.observeStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val _events = MutableSharedFlow<TicketReviewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TicketReviewEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val extraction = extractTicketDataUseCase(ticketId).getOrElse {
                _uiState.value = TicketReviewUiState.LoadFailed
                return@launch
            }
            _uiState.value = TicketReviewUiState.Editing(
                cleanedImagePath = extraction.ticket.cleanedImagePath,
                needsManualReview = extraction.ticket.status == TicketStatus.NEEDS_MANUAL_REVIEW,
                store = extraction.store,
                draft = classify(extraction.toDraft()),
                categories = loadCategories(),
            )
        }
    }

    fun onDateChange(value: String) = editDraft { it.copy(date = value) }

    fun onTimeChange(value: String) = editDraft { it.copy(time = value) }

    fun onTotalChange(value: String) = editDraft { it.copy(total = value) }

    fun onItemChange(item: TicketItemDraft) {
        val previous = (_uiState.value as? TicketReviewUiState.Editing)?.draft?.items?.find { it.key == item.key }
        replaceItem(item)
        if (previous != null && previous.name != item.name && !item.isCategoryCorrected) {
            reclassifyItem(item.key, item.name)
        }
    }

    fun onItemCategoryChange(key: Long, category: ProductCategory?) {
        val item = (_uiState.value as? TicketReviewUiState.Editing)?.draft?.items?.find { it.key == key } ?: return
        replaceItem(item.copy(category = category, isCategoryCorrected = true))
    }

    fun addItem() = editDraft { it.copy(items = it.items + TicketItemDraft(key = nextItemKey++)) }

    fun removeItem(key: Long) = editDraft { draft -> draft.copy(items = draft.items.filterNot { it.key == key }) }

    fun assignStore(storeId: Long) {
        viewModelScope.launch { onStoreAssigned(assignTicketStoreUseCase.assignExisting(ticketId, storeId)) }
    }

    fun createAndAssignStore(storeName: String) {
        if (storeName.isBlank()) return
        viewModelScope.launch { onStoreAssigned(assignTicketStoreUseCase.assignNew(ticketId, storeName)) }
    }

    fun save() {
        val state = _uiState.value as? TicketReviewUiState.Editing ?: return
        if (state.isSaving) return
        val reviewed = state.draft.toReviewedTicket()
        if (reviewed == null) {
            _uiState.value = state.copy(showErrors = true)
            _events.tryEmit(TicketReviewEvent.InvalidFields)
            return
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            saveReviewedTicketUseCase(ticketId, reviewed)
                .onSuccess { _events.emit(TicketReviewEvent.Saved) }
                .onFailure {
                    updateEditing { it.copy(isSaving = false) }
                    _events.emit(TicketReviewEvent.SaveFailed)
                }
        }
    }

    private suspend fun onStoreAssigned(result: Result<Store>) {
        val store = result.getOrElse {
            _events.emit(TicketReviewEvent.StoreAssignFailed)
            return
        }
        updateEditing { it.copy(store = store) }

        // Otro comercio puede tener otra plantilla de parsing.
        val editing = _uiState.value as? TicketReviewUiState.Editing ?: return
        if (editing.isEdited) return
        val extraction = extractTicketDataUseCase(ticketId).getOrNull() ?: return
        val draft = classify(extraction.toDraft())
        updateEditing { state -> if (state.isEdited) state else state.copy(draft = draft) }
    }

    private fun replaceItem(item: TicketItemDraft) = editDraft { draft ->
        draft.copy(items = draft.items.map { if (it.key == item.key) item else it })
    }

    private fun reclassifyItem(key: Long, name: String) {
        viewModelScope.launch {
            val category = classifyProductsUseCase(listOf(name)).firstOrNull()
            updateEditing { state ->
                state.copy(
                    draft = state.draft.copy(
                        items = state.draft.items.map { item ->
                            // Si el usuario ha seguido escribiendo, el resultado ya no aplica.
                            if (item.key == key && item.name == name && !item.isCategoryCorrected) {
                                item.copy(category = category)
                            } else {
                                item
                            }
                        },
                    ),
                )
            }
        }
    }

    private suspend fun classify(draft: TicketDraft): TicketDraft =
        draft.withSuggestedCategories(classifyProductsUseCase(draft.items.map { it.name }))

    private suspend fun loadCategories(): List<CategoryDefinition> =
        try {
            classificationRepository.getCategories()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }

    private fun editDraft(transform: (TicketDraft) -> TicketDraft) =
        updateEditing { it.copy(draft = transform(it.draft), isEdited = true) }

    private fun updateEditing(transform: (TicketReviewUiState.Editing) -> TicketReviewUiState.Editing) =
        _uiState.update { state -> if (state is TicketReviewUiState.Editing) transform(state) else state }

    private fun TicketExtraction.toDraft() = TicketDraft.from(data) { nextItemKey++ }

    companion object {
        const val TICKET_ID_ARG = "ticketId"
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
