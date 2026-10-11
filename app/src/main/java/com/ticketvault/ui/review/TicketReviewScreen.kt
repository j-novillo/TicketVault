package com.ticketvault.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ticketvault.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketReviewScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TicketReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stores by viewModel.stores.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentOnSaved by rememberUpdatedState(onSaved)
    var showStorePicker by rememberSaveable { mutableStateOf(false) }
    var showImage by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            val messageRes = when (event) {
                TicketReviewEvent.Saved -> {
                    currentOnSaved()
                    return@collect
                }
                TicketReviewEvent.SaveFailed -> R.string.review_error_save_failed
                TicketReviewEvent.InvalidFields -> R.string.review_error_invalid_fields
                TicketReviewEvent.StoreAssignFailed -> R.string.review_error_store_assign_failed
            }
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.review_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            (uiState as? TicketReviewUiState.Editing)?.let { state ->
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.review_save))
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            TicketReviewUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            TicketReviewUiState.LoadFailed -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.review_load_failed),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            is TicketReviewUiState.Editing -> {
                TicketReviewContent(
                    state = state,
                    contentPadding = innerPadding,
                    onImageClick = { showImage = true },
                    onChangeStoreClick = { showStorePicker = true },
                    onDateChange = viewModel::onDateChange,
                    onTimeChange = viewModel::onTimeChange,
                    onTotalChange = viewModel::onTotalChange,
                    onItemChange = viewModel::onItemChange,
                    onAddItem = viewModel::addItem,
                    onRemoveItem = viewModel::removeItem,
                )

                if (showStorePicker) {
                    StorePickerDialog(
                        stores = stores,
                        selectedStoreId = state.store?.id,
                        onStoreSelected = { storeId ->
                            showStorePicker = false
                            viewModel.assignStore(storeId)
                        },
                        onStoreCreated = { name ->
                            showStorePicker = false
                            viewModel.createAndAssignStore(name)
                        },
                        onDismiss = { showStorePicker = false },
                    )
                }

                if (showImage && state.cleanedImagePath != null) {
                    Dialog(onDismissRequest = { showImage = false }) {
                        AsyncImage(
                            model = File(state.cleanedImagePath),
                            contentDescription = stringResource(R.string.review_thumbnail),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showImage = false },
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketReviewContent(
    state: TicketReviewUiState.Editing,
    contentPadding: PaddingValues,
    onImageClick: () -> Unit,
    onChangeStoreClick: () -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onTotalChange: (String) -> Unit,
    onItemChange: (TicketItemDraft) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Long) -> Unit,
) {
    val draft = state.draft
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.needsManualReview) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.review_needs_manual_review),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.cleanedImagePath != null) {
                    AsyncImage(
                        model = File(state.cleanedImagePath),
                        contentDescription = stringResource(R.string.review_thumbnail),
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onImageClick),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = state.store?.let {
                            stringResource(R.string.review_store_detected, it.name)
                        } ?: stringResource(R.string.review_store_not_detected),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(onClick = onChangeStoreClick) {
                        Text(
                            stringResource(
                                if (state.store == null) R.string.review_store_select else R.string.review_store_change,
                            ),
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.date,
                    onValueChange = onDateChange,
                    label = { Text(stringResource(R.string.review_date_label)) },
                    placeholder = { Text(stringResource(R.string.review_date_placeholder)) },
                    isError = state.showErrors && draft.dateError,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = draft.time,
                    onValueChange = onTimeChange,
                    label = { Text(stringResource(R.string.review_time_label)) },
                    placeholder = { Text(stringResource(R.string.review_time_placeholder)) },
                    isError = state.showErrors && draft.timeError,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            OutlinedTextField(
                value = draft.total,
                onValueChange = onTotalChange,
                label = { Text(stringResource(R.string.review_total_label)) },
                isError = state.showErrors && draft.totalError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.review_items_title, draft.items.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onAddItem) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.review_item_add), modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        if (draft.items.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.review_items_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(draft.items, key = { it.key }) { item ->
            TicketItemEditor(
                item = item,
                showErrors = state.showErrors,
                onChange = onItemChange,
                onRemove = { onRemoveItem(item.key) },
            )
        }
    }
}

@Composable
private fun TicketItemEditor(
    item: TicketItemDraft,
    showErrors: Boolean,
    onChange: (TicketItemDraft) -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedCard {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onChange(item.copy(name = it)) },
                    label = { Text(stringResource(R.string.review_item_name_label)) },
                    isError = showErrors && item.nameError,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.review_item_remove),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountField(
                    value = item.quantity,
                    onValueChange = { onChange(item.copy(quantity = it)) },
                    labelRes = R.string.review_item_quantity_label,
                    isError = showErrors && item.quantityError,
                    modifier = Modifier.weight(0.8f),
                )
                AmountField(
                    value = item.unitPrice,
                    onValueChange = { onChange(item.copy(unitPrice = it)) },
                    labelRes = R.string.review_item_unit_price_label,
                    isError = showErrors && item.unitPriceError,
                    modifier = Modifier.weight(1f),
                )
                AmountField(
                    value = item.totalPrice,
                    onValueChange = { onChange(item.copy(totalPrice = it)) },
                    labelRes = R.string.review_item_total_label,
                    isError = showErrors && item.totalPriceError,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
