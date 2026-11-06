package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DrawnStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun ScratchpadCanvas(
    onSolveDrawnFormula: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember { mutableStateListOf<DrawnStroke>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var selectedWidth by remember { mutableStateOf(8f) }

    val colorOptions = listOf(
        Color.White,
        Color(0xFF38BDF8), // Cyan
        Color(0xFF818CF8), // Indigo
        Color(0xFFFBBF24), // Amber
        Color(0xFF34D399)  // Emerald
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Handwriting",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Draw or Write Formula",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row {
                IconButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                        }
                    },
                    enabled = strokes.isNotEmpty(),
                    modifier = Modifier.testTag("scratchpad_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo stroke"
                    )
                }

                IconButton(
                    onClick = {
                        strokes.clear()
                        currentPoints = emptyList()
                    },
                    enabled = strokes.isNotEmpty() || currentPoints.isNotEmpty(),
                    modifier = Modifier.testTag("scratchpad_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear canvas"
                    )
                }
            }
        }

        // Color selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Color:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            colorOptions.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 2.5.dp else 1.dp,
                            color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .pointerInput(color) {
                            selectedColor = color
                        }
                )
            }
        }

        // Canvas Area
        var canvasWidth by remember { mutableStateOf(800) }
        var canvasHeight by remember { mutableStateOf(500) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F111D))
                .border(1.dp, Color(0xFF262C40), RoundedCornerShape(12.dp))
                .pointerInput(selectedColor, selectedWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints = currentPoints + change.position
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                strokes.add(DrawnStroke(currentPoints, selectedColor, selectedWidth))
                                currentPoints = emptyList()
                            }
                        },
                        onDragCancel = {
                            currentPoints = emptyList()
                        }
                    )
                }
                .testTag("scratchpad_canvas_box")
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                canvasWidth = size.width.toInt().coerceAtLeast(100)
                canvasHeight = size.height.toInt().coerceAtLeast(100)

                // Draw existing strokes
                for (stroke in strokes) {
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (p in stroke.points.drop(1)) {
                                lineTo(p.x, p.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.points.isNotEmpty()) {
                        drawCircle(
                            color = stroke.color,
                            radius = stroke.strokeWidth / 2,
                            center = stroke.points.first()
                        )
                    }
                }

                // Draw stroke currently being drawn
                if (currentPoints.size > 1) {
                    val currentPath = Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (p in currentPoints.drop(1)) {
                            lineTo(p.x, p.y)
                        }
                    }
                    drawPath(
                        path = currentPath,
                        color = selectedColor,
                        style = Stroke(
                            width = selectedWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Write an equation with your finger or stylus here...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Solve Button
        Button(
            onClick = {
                // Export strokes to a high-contrast white-on-black or black-on-white Bitmap
                val bmp = renderStrokesToBitmap(strokes, canvasWidth, canvasHeight)
                onSolveDrawnFormula(bmp)
            },
            enabled = strokes.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scratchpad_solve_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Solve Handwritten Problem")
        }
    }
}

fun renderStrokesToBitmap(
    strokes: List<DrawnStroke>,
    width: Int,
    height: Int
): Bitmap {
    val w = width.coerceIn(200, 1600)
    val h = height.coerceIn(200, 1600)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Fill white background for maximum OCR / Multimodal recognition clarity
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    for (stroke in strokes) {
        paint.strokeWidth = stroke.strokeWidth * 1.5f
        if (stroke.points.size > 1) {
            val path = android.graphics.Path()
            path.moveTo(stroke.points.first().x, stroke.points.first().y)
            for (p in stroke.points.drop(1)) {
                path.lineTo(p.x, p.y)
            }
            canvas.drawPath(path, paint)
        } else if (stroke.points.isNotEmpty()) {
            canvas.drawCircle(
                stroke.points.first().x,
                stroke.points.first().y,
                stroke.strokeWidth,
                paint
            )
        }
    }

    return bitmap
}
