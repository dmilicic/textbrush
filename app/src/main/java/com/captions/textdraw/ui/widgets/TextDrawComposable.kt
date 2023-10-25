package com.captions.textdraw.ui.widgets

import android.graphics.Bitmap
import android.graphics.PointF
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import com.captions.textdraw.R
import com.captions.textdraw.tools.TextDrawTool
import kotlin.math.nextDown

@Composable
fun ClearButton(onClearClick: () -> Unit) {
    IconButton(onClick = onClearClick) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Clear Button",
            tint = Color.Black
        )
    }
}

@Composable
fun UndoButton(modifier: Modifier = Modifier, onUndoClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = onUndoClick) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.undo),
            contentDescription = "Undo Button",
            tint = Color.Black
        )
    }
}

@Composable
fun SaveButton(modifier: Modifier = Modifier, onSaveClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = onSaveClick) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = "Save Button",
            tint = Color.Black
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextDrawComposable(tool: TextDrawTool) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var fontSize by remember { mutableStateOf(40.sp) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        fontSize *= zoomChange
    }

    val textMeasurer = rememberTextMeasurer()

    // this will store the edited photo
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var editedImage by remember { mutableStateOf<View?>(null) }

    tool.textMeasurer = textMeasurer
    tool.fontSize = fontSize

    Box(modifier = Modifier
        .fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            tool.onDragStarted(PointF(it.x, it.y))
                        },
                        onDragEnd = {
                            tool.onDragStopped()
                        }
                    ) { change, dragAmount ->
                        change.consume()

                        val currentPoint = PointF(change.position.x, change.position.y)
                        val prevPoint = PointF(change.previousPosition.x, change.previousPosition.y)

                        tool.onDragEvent(currentPoint, prevPoint)

                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
        ) {

            editedImage = LocalView.current

            if (bitmap == null) {
                Image(
                    painter = painterResource(id = R.drawable.antarctica),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Crop, // This will crop the image if it doesn't fit the Box
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Crop, // This will crop the image if it doesn't fit the Box
                    modifier = Modifier.fillMaxSize()
                )
            }

            ClearButton {
                tool.onClear()
            }

            Column(modifier = Modifier.align(Alignment.TopEnd)) {
                UndoButton {
                    tool.onUndo()
                }

                SaveButton {
                    bitmap = editedImage?.drawToBitmap()
                }
            }

            Canvas(modifier = Modifier.fillMaxSize(),
                onDraw = {
                    offsetX.nextDown() // triggers redraw
                    tool.onDraw(this)
                }
            )
        }
    }
}
