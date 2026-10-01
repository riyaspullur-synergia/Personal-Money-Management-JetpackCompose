package app.riyaspullur.personalmoneymanagement.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.getIconResId
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@Composable
fun MoneyText(
    money: Money,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    prefix: String = ""
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (prefix.isNotEmpty()) {
            Text(text = prefix, style = style, color = color)
        }
        
        val iconResId = money.currency.getIconResId()
        if (iconResId != null) {
            val iconSize = if (style.fontSize.isSp) style.fontSize.value.dp * 0.8f else 18.dp
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = money.currency.symbol,
                modifier = Modifier
                    .size(iconSize)
                    .padding(end = 4.dp),
                colorFilter = if (color != Color.Unspecified) ColorFilter.tint(color) else null
            )
            Text(
                text = money.format(includeSymbol = false),
                style = style,
                color = color
            )
        } else {
            Text(
                text = money.format(includeSymbol = true),
                style = style,
                color = color
            )
        }
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun MoneyTextPreview() {
    PersonalMoneyManagemntTheme {
        androidx.compose.material3.Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyText(money = Money(123456L, Currency.AED))
                MoneyText(money = Money(123456L, Currency.INR), color = Color(0xFF4CAF50), prefix = "+")
                MoneyText(money = Money(5000L, Currency.USD), color = Color(0xFFF44336), prefix = "-")
                MoneyText(
                    money = Money(1000000L, Currency.AED),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}
