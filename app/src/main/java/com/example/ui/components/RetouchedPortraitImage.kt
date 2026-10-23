package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.PhotoEdits
import com.example.data.model.UserPhoto
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max

/**
 * Renders a dating profile portrait photo using Compose Canvas, Android native drawBitmapMesh
 * perspective/jawline warping, and ColorMatrix image processing filters (brightness, contrast,
 * skin smoothing, golden warmth, eye clarity, and jawline contour).
 */
@Composable
fun RetouchedPortraitImage(
    photo: UserPhoto,
    modifier: Modifier = Modifier,
    overrideEdits: PhotoEdits? = null,
    showOriginal: Boolean = false,
    showMeshGrid: Boolean = false,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val activeEdits = if (showOriginal) PhotoEdits() else (overrideEdits ?: photo.edits)

    val skinSoft = activeEdits.skinSoftening.coerceIn(0f, 1f)
    val jawWidth = activeEdits.jawWidth.coerceIn(-0.5f, 1f)
    val warmth = activeEdits.skinWarmth.coerceIn(0f, 1f)
    val clarity = activeEdits.eyeClarity.coerceIn(0f, 1f)
    val contour = activeEdits.jawContour.coerceIn(0f, 1f)
    val brightness = activeEdits.brightness.coerceIn(-0.5f, 0.5f)
    val contrast = activeEdits.contrast.coerceIn(-0.5f, 0.5f)
    val perspective = activeEdits.perspectiveWarp.coerceIn(-0.5f, 0.5f)

    // Load software-accessible ARGB_8888 Bitmap for real Canvas drawBitmapMesh perspective/jawline warping
    var loadedBitmap by remember(photo.uriString) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photo.uriString) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(photo.uriString)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bmp = (result.drawable as? BitmapDrawable)?.bitmap
                if (bmp != null) {
                    loadedBitmap = bmp.copy(Bitmap.Config.ARGB_8888, false)
                }
            }
        } catch (_: Throwable) {
            loadedBitmap = null
        }
    }

    val colorMatrixValues = remember(skinSoft, warmth, clarity, contour, brightness, contrast) {
        val c = (1f + (contrast * 0.75f) + (clarity * 0.18f) + (contour * 0.08f) - (skinSoft * 0.06f)).coerceAtLeast(0.25f)
        val bOffset = (brightness * 180f) + (skinSoft * 9f) + (clarity * 3f)
        val translate = (1f - c) * 128f + bOffset
        val rShift = (warmth * 24f) + (skinSoft * 7f)
        val gShift = (warmth * 10f) + (skinSoft * 4f)
        val bShift = -(warmth * 15f) + (clarity * 4f)

        floatArrayOf(
            c, 0f, 0f, 0f, translate + rShift,
            0f, c, 0f, 0f, translate + gShift,
            0f, 0f, c, 0f, translate + bShift,
            0f, 0f, 0f, 1f, 0f
        )
    }

    val baseColorFilter = remember(colorMatrixValues, activeEdits.isEdited) {
        if (!activeEdits.isEdited) null else ColorFilter.colorMatrix(ColorMatrix(colorMatrixValues))
    }

    val softLayerColorFilter = remember(skinSoft, warmth, brightness, contrast) {
        val c = (1.02f + (contrast * 0.35f) - (skinSoft * 0.08f)).coerceAtLeast(0.3f)
        val bOffset = (brightness * 160f) + (skinSoft * 12f)
        val translate = (1f - c) * 128f + bOffset
        val rShift = (warmth * 26f) + (skinSoft * 12f)
        val gShift = (warmth * 12f) + (skinSoft * 6f)
        val bShift = -(warmth * 14f) - (skinSoft * 2f)
        val matrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate + rShift,
                0f, c, 0f, 0f, translate + gShift,
                0f, 0f, c, 0f, translate + bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        ColorFilter.colorMatrix(matrix)
    }

    Box(modifier = modifier) {
        val bmp = loadedBitmap
        if (bmp != null && (abs(jawWidth) > 0.01f || abs(perspective) > 0.01f || showMeshGrid)) {
            // =========================================================================
            // COMPOSE CANVAS + NATIVE BITMAP MESH PERSPECTIVE & JAWLINE WARP ENGINE
            // =========================================================================
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                if (canvasW > 0f && canvasH > 0f) {
                    val meshW = 12
                    val meshH = 12
                    val verts = FloatArray((meshW + 1) * (meshH + 1) * 2)

                    // Calculate Center-Crop geometry mapping Bitmap to Canvas
                    val scale = max(canvasW / bmp.width.toFloat(), canvasH / bmp.height.toFloat())
                    val scaledW = bmp.width * scale
                    val scaledH = bmp.height * scale
                    val offsetX = (canvasW - scaledW) * 0.5f
                    val offsetY = (canvasH - scaledH) * 0.5f

                    var index = 0
                    for (y in 0..meshH) {
                        val fy = y.toFloat() / meshH.toFloat()
                        val baseY = offsetY + fy * scaledH
                        val ny = (baseY / canvasH).coerceIn(0f, 1f)

                        for (x in 0..meshW) {
                            val fx = x.toFloat() / meshW.toFloat()
                            val baseX = offsetX + fx * scaledW
                            val nx = (baseX / canvasW).coerceIn(0f, 1f)

                            // Jawline localized radial mesh weight (centered at lower third of face: x=0.5, y=0.64)
                            val dxCenter = nx - 0.5f
                            val dyJaw = ny - 0.64f
                            val jawGaussian = exp(-((dxCenter * dxCenter) / 0.095f + (dyJaw * dyJaw) / 0.048f))

                            // Lower-face perspective keystone weight
                            val lowerProgress = ((ny - 0.42f).coerceAtLeast(0f) / 0.58f)
                            val perspectiveGaussian = lowerProgress * exp(-((dxCenter * dxCenter) / 0.16f))

                            // Horizontal jawline widening + perspective keystone warp
                            val warpX = (dxCenter * jawWidth * 0.36f * jawGaussian * canvasW) +
                                    (dxCenter * perspective * 0.28f * perspectiveGaussian * canvasW)

                            // Vertical chin contour & perspective tilt
                            val warpY = (perspective * 0.08f * jawGaussian * canvasH) +
                                    (jawWidth * 0.025f * jawGaussian * canvasH)

                            verts[index * 2] = baseX + warpX
                            verts[index * 2 + 1] = baseY + warpY
                            index++
                        }
                    }

                    val nativePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrixValues)
                    }

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawBitmapMesh(
                            bmp,
                            meshW,
                            meshH,
                            verts,
                            0,
                            null,
                            0,
                            nativePaint
                        )
                    }
                }
            }
        } else {
            // Fallback / Fast GPU Layer when no mesh warp is active or while Bitmap is decoding
            AsyncImage(
                model = photo.uriString,
                contentDescription = contentDescription,
                contentScale = contentScale,
                colorFilter = baseColorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1f + (jawWidth * 0.06f) + (perspective * 0.04f)
                        rotationX = perspective * 6f
                    }
            )

            if (abs(jawWidth) > 0.01f || abs(perspective) > 0.01f) {
                AsyncImage(
                    model = photo.uriString,
                    contentDescription = null,
                    contentScale = contentScale,
                    colorFilter = baseColorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            val jawCenter = Offset(size.width * 0.5f, size.height * 0.63f)
                            val maskRadius = size.minDimension * 0.46f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Black,
                                        0.55f to Color.Black.copy(alpha = 0.85f),
                                        0.85f to Color.Black.copy(alpha = 0.25f),
                                        1.0f to Color.Transparent
                                    ),
                                    center = jawCenter,
                                    radius = maskRadius.coerceAtLeast(1f)
                                ),
                                radius = maskRadius,
                                center = jawCenter,
                                blendMode = BlendMode.DstIn
                            )
                        }
                        .graphicsLayer {
                            scaleX = 1f + (jawWidth * 0.26f) + (perspective * 0.16f)
                            scaleY = 1f + (jawWidth * 0.04f) + (perspective * 0.06f)
                        }
                )
            }
        }

        // Skin Tone Softening Layer (frequency-separated soft-focus dermal diffusion on central face)
        if (skinSoft > 0.01f) {
            val blurRadiusDp = (skinSoft * 8.5f).coerceAtLeast(0.5f).dp
            AsyncImage(
                model = photo.uriString,
                contentDescription = null,
                contentScale = contentScale,
                colorFilter = softLayerColorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        alpha = (skinSoft * 0.62f).coerceIn(0f, 0.75f)
                        scaleX = 1f + (jawWidth * 0.14f) + (perspective * 0.08f)
                    }
                    .blur(blurRadiusDp)
                    .drawWithContent {
                        drawContent()
                        val faceCenter = Offset(size.width * 0.5f, size.height * 0.48f)
                        val faceRadius = size.minDimension * 0.48f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black,
                                    0.65f to Color.Black.copy(alpha = 0.75f),
                                    1.0f to Color.Transparent
                                ),
                                center = faceCenter,
                                radius = faceRadius.coerceAtLeast(1f)
                            ),
                            radius = faceRadius,
                            center = faceCenter,
                            blendMode = BlendMode.DstIn
                        )
                    }
            )
        }

        // Studio Lighting, Dermal Glow, Eye Catchlight, Jawline Contour & Optional Warp Mesh Overlay
        if (skinSoft > 0.01f || warmth > 0.01f || clarity > 0.01f || contour > 0.01f || showMeshGrid) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Soft peach/rose skin-tone evening veil
                if (skinSoft > 0.01f || warmth > 0.01f) {
                    val glowAlpha = ((skinSoft * 0.13f) + (warmth * 0.11f)).coerceIn(0f, 0.22f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFDFC4).copy(alpha = glowAlpha),
                                Color(0xFFFFB59E).copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.46f),
                            radius = (size.minDimension * 0.6f).coerceAtLeast(1f)
                        )
                    )
                }

                // Eye zone brightness catchlight
                if (clarity > 0.01f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = clarity * 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.36f),
                            radius = (size.minDimension * 0.32f).coerceAtLeast(1f)
                        )
                    )
                }

                // Cheekbone & jawline studio contour sculpting
                if (contour > 0.01f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.68f to Color.Transparent,
                                1.0f to Color(0xFF1F1418).copy(alpha = contour * 0.28f)
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.52f),
                            radius = (size.maxDimension * 0.65f).coerceAtLeast(1f)
                        )
                    )
                }

                // Interactive Jawline & Facial Perspective Warp Mesh Grid Overlay
                if (showMeshGrid) {
                    val gridCols = 8
                    val gridRows = 8
                    val gridColor = Color(0xFF00E5FF).copy(alpha = 0.45f)
                    val jawHighlightColor = Color(0xFFFF4081).copy(alpha = 0.85f)

                    fun warpedPoint(col: Int, row: Int): Offset {
                        val nx = col.toFloat() / gridCols.toFloat()
                        val ny = row.toFloat() / gridRows.toFloat()
                        val dxCenter = nx - 0.5f
                        val dyJaw = ny - 0.64f
                        val jawGaussian = exp(-((dxCenter * dxCenter) / 0.095f + (dyJaw * dyJaw) / 0.048f))
                        val lowerProgress = ((ny - 0.42f).coerceAtLeast(0f) / 0.58f)
                        val perspectiveGaussian = lowerProgress * exp(-((dxCenter * dxCenter) / 0.16f))

                        val wx = (nx * size.width) +
                                (dxCenter * jawWidth * 0.36f * jawGaussian * size.width) +
                                (dxCenter * perspective * 0.28f * perspectiveGaussian * size.width)
                        val wy = (ny * size.height) +
                                (perspective * 0.08f * jawGaussian * size.height) +
                                (jawWidth * 0.025f * jawGaussian * size.height)
                        return Offset(wx, wy)
                    }

                    for (r in 0..gridRows) {
                        for (c in 0 until gridCols) {
                            val p1 = warpedPoint(c, r)
                            val p2 = warpedPoint(c + 1, r)
                            val isJawRow = r in 4..6 && c in 2..5
                            drawLine(
                                color = if (isJawRow) jawHighlightColor else gridColor,
                                start = p1,
                                end = p2,
                                strokeWidth = if (isJawRow) 2.5f else 1.2f
                            )
                        }
                    }
                    for (c in 0..gridCols) {
                        for (r in 0 until gridRows) {
                            val p1 = warpedPoint(c, r)
                            val p2 = warpedPoint(c, r + 1)
                            val isJawCol = r in 4..6 && c in 2..6
                            drawLine(
                                color = if (isJawCol) jawHighlightColor else gridColor,
                                start = p1,
                                end = p2,
                                strokeWidth = if (isJawCol) 2.5f else 1.2f
                            )
                        }
                    }

                    // Draw jawline control nodes
                    for (r in 4..6) {
                        for (c in 2..6) {
                            val pt = warpedPoint(c, r)
                            drawCircle(
                                color = Color.White,
                                radius = 4.5f,
                                center = pt
                            )
                            drawCircle(
                                color = jawHighlightColor,
                                radius = 4.5f,
                                center = pt,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
