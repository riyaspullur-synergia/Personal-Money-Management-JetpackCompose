package app.riyaspullur.personalmoneymanagement.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

data class PieChartData(
    val value: Float,
    val color: Color
)

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 40f
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    
    Canvas(modifier = modifier.aspectRatio(1f)) {
        var startAngle = -90f
        
        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun PieChartPreview() {
    val data = listOf(
        PieChartData(40f, Color(0xFF4CAF50)),
        PieChartData(30f, Color(0xFFF44336)),
        PieChartData(20f, Color(0xFF2196F3)),
        PieChartData(10f, Color(0xFFFFC107))
    )
    PersonalMoneyManagemntTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PieChart(
                data = data,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}
