package app.riyaspullur.personalmoneymanagement.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

data class BarChartData(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Box(modifier = modifier.fillMaxWidth()) {
        val barWidth = 20.dp
        
        // Background Grid Lines
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(4) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp)) // space for labels
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val barHeightRatio = if (maxValue > 0) item.value / maxValue else 0f
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Value Text
                    if (item.value > 0) {
                        Text(
                            text = if (item.value >= 1000) "${String.format(java.util.Locale.US, "%.1f", item.value / 1000)}k" else item.value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = item.color,
                            modifier = Modifier.padding(bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // The Bar
                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .fillMaxHeight(barHeightRatio.coerceIn(0.01f, 0.85f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                color = item.color.copy(alpha = 0.9f)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Label
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun BarChartPreview() {
    val data = listOf(
        BarChartData(1500f, Color(0xFF4CAF50), "Aug 1"),
        BarChartData(2500f, Color(0xFFF44336), "Aug 2"),
        BarChartData(1800f, Color(0xFF2196F3), "Aug 3"),
        BarChartData(3000f, Color(0xFFFFC107), "Aug 4")
    )
    PersonalMoneyManagemntTheme {
        Surface(modifier = Modifier.padding(16.dp).height(200.dp)) {
            BarChart(data = data)
        }
    }
}
