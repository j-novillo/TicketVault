package com.ticketvault.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ticketvault.R
import com.ticketvault.domain.model.Store

@Composable
fun StorePickerDialog(
    stores: List<Store>,
    selectedStoreId: Long?,
    onStoreSelected: (Long) -> Unit,
    onStoreCreated: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val trimmedQuery = query.trim()
    val filteredStores = stores.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
    val canCreate = trimmedQuery.isNotEmpty() &&
        stores.none { it.name.equals(trimmedQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.store_picker_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.store_picker_search_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    if (canCreate) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(stringResource(R.string.store_picker_create, trimmedQuery))
                                },
                                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                                modifier = Modifier.clickable { onStoreCreated(trimmedQuery) },
                            )
                            HorizontalDivider()
                        }
                    }
                    items(filteredStores, key = { it.id }) { store ->
                        ListItem(
                            headlineContent = { Text(store.name) },
                            trailingContent = {
                                if (store.id == selectedStoreId) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.clickable { onStoreSelected(store.id) },
                        )
                    }
                    if (filteredStores.isEmpty() && !canCreate) {
                        item {
                            Text(
                                text = stringResource(R.string.store_picker_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.store_picker_cancel))
            }
        },
    )
}
