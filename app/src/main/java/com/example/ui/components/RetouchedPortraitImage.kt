package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.PhotoEdits
import com.example.data.model.UserPhoto
import kotlin.math.abs

/**
 * Renders a dating profile portrait photo with real-time visual retouching effects:
 * - Skin Tone Softening (frequency-separated dermal smoothing & warm complexion glow)
 * - Jawline Widening / Sculpting (seamless feathered lower-face horizontal expansion)
 * - Golden Hour Skin Warmth (warm amber/peach color matrix shift)
 * - Eye Clarity & Contrast (crisp upper-face catchlight & micro-contrast)
 * - Cheekbone & Jaw Contour (subtle studio sculpting vignette)
 */
@Composable
fun RetouchedPortraitImage(
    photo: UserPhoto,
    modifier: Modifier = Modifier,
    overrideEdits: PhotoEdits? = null,
    showOriginal: Boolean = false,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val activeEdits = if (showOriginal) PhotoEdits() else (overrideEdits ?: photo.edits)

    val skinSoft = activeEdits.skinSoftening.coerceIn(0f, 1f)
    val jawWidth = activeEdits.jawWidth.coerceIn(-0.5f, 1f)
    val warmth = activeEdits.skinWarmth.coerceIn(0f, 1f)
    val clarity = activeEdits.eyeClarity.coerceIn(0f, 1f)
    val contour = activeEdits.jawContour.coerceIn(0f, 1f)

    val baseColorFilter = remember(skinSoft, warmth, clarity, contour) {
        if (!activeEdits.isEdited) {
            null
        } else {
            val contrast = 1f + (clarity * 0.18f) + (contour * 0.08f) - (skinSoft * 0.05f)
            val translate = (1f - contrast) * 128f + (skinSoft * 9f) + (clarity * 3f)
            val rShift = (warmth * 24f) + (skinSoft * 7f)
            val gShift = (warmth * 10f) + (skinSoft * 4f)
            val bShift = -(warmth * 15f) + (clarity * 4f)

            val matrix = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, translate + rShift,
                    0f, contrast, 0f, 0f, translate + gShift,
                    0f, 0f, contrast, 0f, translate + bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            ColorFilter.colorMatrix(matrix)
        }
    }

    val softLayerColorFilter = remember(skinSoft, warmth) {
        val rShift = (warmth * 26f) + (skinSoft * 12f)
        val gShift = (warmth * 12f) + (skinSoft * 6f)
        val bShift = -(warmth * 14f) + (skinSoft * 2f)
        val matrix = ColorMatrix(
            floatArrayOf(
                1.02f, 0f, 0f, 0f, rShift,
                0f, 1.02f, 0f, 0f, gShift,
                0f, 0f, 1.0f, 0f, bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        ColorFilter.colorMatrix(matrix)
    }

    Box(modifier = modifier) {
        // 1. Base sharp portrait layer (with subtle overall frame proportion adjustment when jaw width is active)
        AsyncImage(
            model = photo.uriString,
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = baseColorFilter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1f + (jawWidth * 0.06f)
                }
        )

        // 2. Jawline Widening / Sculpting Feathered Mesh Layer (targets lower third of portrait face)
        if (abs(jawWidth) > 0.01f) {
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
                        // Feathered radial mask focused on the lower-face / jawline region (y ~ 63%)
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
                        // Horizontal expansion/compression centered on the jawline
                        scaleX = 1f + (jawWidth * 0.26f)
                        scaleY = 1f + (jawWidth * 0.04f)
                    }
            )
        }

        // 3. Skin Tone Softening Layer (frequency-separated soft-focus dermal diffusion on central face)
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
                        scaleX = 1f + (jawWidth * 0.14f)
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

        // 4. Studio Lighting, Dermal Glow, Eye Catchlight & Jawline Contour Overlays
        if (skinSoft > 0.01f || warmth > 0.01f || clarity > 0.01f || contour > 0.01f) {
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
            }
        }
    }
}
