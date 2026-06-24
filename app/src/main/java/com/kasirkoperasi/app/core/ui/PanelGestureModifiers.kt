package com.kasirkoperasi.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ModalOverlayWindow(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        content()
    }
}

fun Modifier.dismissPanelOnTap(onDismiss: () -> Unit): Modifier {
    return pointerInput(onDismiss) {
        detectTapGestures(onTap = { onDismiss() })
    }
}
