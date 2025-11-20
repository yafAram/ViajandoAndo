package com.example.app_andando_ando.presentation.route

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.detectVerticalDragGestures(
    onDragStart: (() -> Unit) = {},
    onDragEnd: (() -> Unit) = {},
    onVerticalDrag: (dragAmount: Float) -> Unit
): Modifier = pointerInput(Unit) {
    coroutineScope {
        var dragStarted = false

        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val dragEvent = event.changes.firstOrNull()

                if (dragEvent != null) {
                    val verticalDrag = dragEvent.positionChange().y

                    when {
                        dragEvent.pressed && !dragStarted -> {
                            dragStarted = true
                            onDragStart()
                        }
                        dragStarted && dragEvent.changedToUp() -> {
                            dragStarted = false
                            onDragEnd()
                        }
                        dragStarted && verticalDrag != 0f -> {
                            onVerticalDrag(verticalDrag)
                            dragEvent.consume()
                        }
                    }
                }
            }
        }
    }
}