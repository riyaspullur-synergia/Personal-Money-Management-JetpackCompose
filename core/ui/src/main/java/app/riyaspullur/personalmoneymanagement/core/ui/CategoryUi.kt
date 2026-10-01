package app.riyaspullur.personalmoneymanagement.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category

@Composable
fun Category.displayLabel(): String {
    return when (name) {
        "Food" -> stringResource(R.string.category_food)
        "Transport" -> stringResource(R.string.category_transport)
        "Salary" -> stringResource(R.string.category_salary)
        "Shopping" -> stringResource(R.string.category_shopping)
        "Bills" -> stringResource(R.string.category_bills)
        "Utilities" -> stringResource(R.string.category_utilities)
        "Entertainment" -> stringResource(R.string.category_entertainment)
        "Health" -> stringResource(R.string.category_health)
        "Gift" -> stringResource(R.string.category_gift)
        else -> name
    }
}
