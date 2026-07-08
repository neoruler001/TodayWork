package com.todaywork.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour Picker
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            WheelNumberPicker(
                range = 0..23,
                initialValue = initialHour,
                onValueChange = {
                    selectedHour = it
                    onTimeSelected(selectedHour, selectedMinute)
                }
            )
            Text(
                text = "시",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Minute Picker
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            WheelNumberPicker(
                range = 0..59,
                initialValue = initialMinute,
                onValueChange = {
                    selectedMinute = it
                    onTimeSelected(selectedHour, selectedMinute)
                }
            )
            Text(
                text = "분",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun WheelNumberPicker(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit
) {
    val items = range.toList()
    // 무한 스크롤 효과를 위한 리스트 확장 및 가상 인덱스 매핑
    val multiplier = 100
    val virtualList = List(items.size * multiplier) { index -> items[index % items.size] }
    val initialIndex = (items.size * (multiplier / 2)) + items.indexOf(initialValue)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 선택된 항목 트래킹
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> virtualList.getOrNull(index) ?: initialValue }
            .distinctUntilChanged()
            .collect { value ->
                onValueChange(value)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 선택 하이라이팅 가이드 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentPadding = PaddingValues(vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(virtualList.size) { index ->
                val item = virtualList[index]
                val isSelected = listState.firstVisibleItemIndex == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(item),
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
