package com.ticketvault.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticketvault.R
import com.ticketvault.classification.CategoryDefinition
import com.ticketvault.classification.ProductCategory

@Composable
fun CategoryPickerDialog(
    productName: String,
    categories: List<CategoryDefinition>,
    selected: ProductCategory?,
    onCategorySelected: (ProductCategory?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = productName.ifBlank { stringResource(R.string.category_picker_title) },
                maxLines = 2,
            )
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                item {
                    CategoryOption(
                        label = stringResource(R.string.category_unclassified),
                        isSelected = selected == null,
                        onClick = { onCategorySelected(null) },
                    )
                    HorizontalDivider()
                }
                categories.forEach { category ->
                    item(key = category.name) {
                        CategoryOption(
                            label = category.name,
                            isSelected = selected == ProductCategory(category.name, null),
                            isHeader = true,
                            onClick = { onCategorySelected(ProductCategory(category.name, null)) },
                        )
                    }
                    items(category.subcategories, key = { "${category.name}/${it.name}" }) { subcategory ->
                        val option = ProductCategory(category.name, subcategory.name)
                        CategoryOption(
                            label = subcategory.name,
                            isSelected = selected == option,
                            modifier = Modifier.padding(start = 16.dp),
                            onClick = { onCategorySelected(option) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.category_picker_cancel))
            }
        },
    )
}

@Composable
private fun CategoryOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHeader) FontWeight.SemiBold else null,
            )
        },
        trailingContent = {
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}
