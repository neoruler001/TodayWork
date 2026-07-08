package com.todaywork.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todaywork.app.data.model.ShiftType

/**
 * 근무 타입 배지 컴포넌트 (원형/라운드 박스 두 가지)
 */
@Composable
fun ShiftBadgeCircle(
    shiftType: ShiftType,
    size: Dp = 32.dp,
    fontSize: Int = 13
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(shiftType.toColor()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shiftType.shortLabel,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun ShiftBadgeChip(
    shiftType: ShiftType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(shiftType.toColor().copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(shiftType.toColor())
            )
            Text(
                text = shiftType.label,
                color = shiftType.toColor(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ShiftLegend(modifier: Modifier = Modifier) {
    val types = listOf(ShiftType.DAY, ShiftType.NIGHT, ShiftType.REST, ShiftType.OFF, ShiftType.DUTY)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(type.toColor())
                )
                Text(
                    text = type.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
