package com.ferrari.ferrariplantspacingdistancetable.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.alpha

@Composable
fun WheelPicker(
    items: List<Int>,
    selected: Int?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeStartIndex = when {
        selected != null -> {
            val indexOf = items.indexOf(selected)
            if (indexOf >= 0) indexOf else items.size / 2
        }
        else -> items.size / 2
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = safeStartIndex
    )

    // Przewiń do wybranego przy zmianie
    LaunchedEffect(selected) {
        val index = items.indexOf(selected)
        if (index >= 0) {
            listState.animateScrollToItem(index.coerceIn(0, items.size - 1))
        }
    }

    Box(
        modifier = modifier
            .height(120.dp)
            .fillMaxWidth()
    ) {
        // Gradient – wyróżnia środek
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.4f to Color.Black.copy(alpha = 0.1f),
                    0.5f to Color.Black.copy(alpha = 0.3f),
                    0.6f to Color.Black.copy(alpha = 0.1f),
                    1f to Color.Transparent
                )
            )
        }

        // Lista z paddingiem (2 puste na górze/dole)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size + 4) { index ->
                val actualIndex = (index - 2 + items.size).mod(items.size)
                val item = items[actualIndex]
                val isSelected = item == selected

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .alpha(if (isSelected) 1f else 0.5f)
                ) {
                    Text(
                        text = item.toString(),
                        style = if (isSelected)
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        else
                            MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // DETEKCJA ŚRODKA – DZIAŁA PRZY SCROLLU
        LaunchedEffect(listState) {
            if (items.isEmpty()) return@LaunchedEffect

            snapshotFlow { listState.firstVisibleItemScrollOffset }
                .collect {
                    val visibleIndex = listState.firstVisibleItemIndex
                    val centerIndex = visibleIndex + 2  // +2 bo 2 puste na górze
                    val actualIndex = (centerIndex - 2 + items.size).mod(items.size)
                    onSelected(items[actualIndex])
                }
        }
    }
}