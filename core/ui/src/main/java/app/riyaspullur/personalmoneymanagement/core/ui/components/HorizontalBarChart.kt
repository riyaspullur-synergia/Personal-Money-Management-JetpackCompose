package app.riyaspullur.personalmoneymanagement.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

data class HorizontalBarData(
    val label: String,
    val value: Float,
    val money: Money,
    val color: Color
)

@Composable
fun HorizontalBarChart(
    data: List<HorizontalBarData>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.value } ?: 1f
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        data.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(item.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    MoneyText(
                        money = item.money,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (maxVal > 0) item.value / maxVal else 0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(item.color)
                    )
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun HorizontalBarChartPreview() {
    val data = listOf(
        HorizontalBarData("Food", 1500f, Money(150000L, Currency.AED), Color(0xFFF44336)),
        HorizontalBarData("Rent", 5000f, Money(500000L, Currency.AED), Color(0xFF2196F3)),
        HorizontalBarData("Transport", 800f, Money(80000L, Currency.AED), Color(0xFFFFC107)),
        HorizontalBarData("Entertainment", 1200f, Money(120000L, Currency.AED), Color(0xFF4CAF50))
    )
    PersonalMoneyManagemntTheme {
        androidx.compose.material3.Surface(modifier = Modifier.padding(16.dp)) {
            HorizontalBarChart(data = data)
        }
    }
}
