package com.ticketvault.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.domain.usecase.AssignTicketStoreUseCase
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

@HiltViewModel
class TicketReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val extractTicketDataUseCase: ExtractTicketDataUseCase,
    private val saveReviewedTicketUseCase: SaveReviewedTicketUseCase,
    private val assignTicketStoreUseCase: AssignTicketStoreUseCase,
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
            extractTicketDataUseCase(ticketId)
                .onSuccess { extraction ->
                    _uiState.value = TicketReviewUiState.Editing(
                        cleanedImagePath = extraction.ticket.cleanedImagePath,
                        needsManualReview = extraction.ticket.status == TicketStatus.NEEDS_MANUAL_REVIEW,
                        store = extraction.store,
                        draft = extraction.toDraft(),
                    )
                }
                .onFailure { _uiState.value = TicketReviewUiState.LoadFailed }
        }
    }

    fun onDateChange(value: String) = editDraft { it.copy(date = value) }

    fun onTimeChange(value: String) = editDraft { it.copy(time = value) }

    fun onTotalChange(value: String) = editDraft { it.copy(total = value) }

    fun onItemChange(item: TicketItemDraft) = editDraft { draft ->
        draft.copy(items = draft.items.map { if (it.key == item.key) item else it })
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
        extractTicketDataUseCase(ticketId).onSuccess { extraction ->
            updateEditing { state -> if (state.isEdited) state else state.copy(draft = extraction.toDraft()) }
        }
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
