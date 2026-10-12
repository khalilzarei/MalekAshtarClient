package com.khz.malekclient.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.khz.malekclient.ui.theme.GoldPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * دیالوگ برش آواتار شبیه تلگرام/واتساپ
 * - تصویر از گالری (Uri) می‌گیرد
 * - pinch-to-zoom (1..4x) + پن با clamp + چرخش ۹۰ درجه + اسلایدر زوم
 * - خروجی مربعی ۵۱۲×۵۱۲ JPEG (کیفیت ۹۲)
 * - پیش‌نمایش دقیقاً با ماتریس خروجی cropBitmapToFile هماهنگ است
 */
@Composable
fun AvatarCropDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (File) -> Unit,
    outputSize: Int = 512
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // state ویرایش
    var userScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) } // 0,90,180,270

    val boxSizeDp = 300.dp
    val boxSizePx = with(density) { boxSizeDp.toPx() }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                val bmp = loadBitmapFromUri(
                    context,
                    uri,
                    maxDim = 1024
                )
                withContext(Dispatchers.Main) { bitmap = bmp }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = "خطا در بارگذاری تصویر"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(16.dp)
        ) {
            when {
                error != null  -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            error!!,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text(
                                "بستن",
                                color = Color.White
                            )
                        }
                    }
                }

                bitmap == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "در حال بارگذاری...",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else           -> {
                    val bmp = bitmap!!

                    // fitScale: کمترین زومی که عکس کل باکس را بپوشاند (centerCrop)
                    val fitScale = remember(
                        bmp,
                        boxSizePx
                    ) {
                        max(
                            boxSizePx / bmp.width,
                            boxSizePx / bmp.height
                        )
                    }
                    val totalScale = fitScale * userScale

                    // حد پن: نمی‌گذاریم فضای خالی دیده شود
                    val maxOffsetX = remember(
                        bmp,
                        totalScale,
                        boxSizePx
                    ) {
                        max(
                            0f,
                            (bmp.width * totalScale - boxSizePx) / 2f
                        )
                    }
                    val maxOffsetY = remember(
                        bmp,
                        totalScale,
                        boxSizePx
                    ) {
                        max(
                            0f,
                            (bmp.height * totalScale - boxSizePx) / 2f
                        )
                    }
                    // اختیارات کاربر را هنگام تغییر max clamp کن
                    LaunchedEffect(
                        maxOffsetX,
                        maxOffsetY
                    ) {
                        offset = Offset(
                            x = offset.x.coerceIn(
                                -maxOffsetX,
                                maxOffsetX
                            ),
                            y = offset.y.coerceIn(
                                -maxOffsetY,
                                maxOffsetY
                            )
                        )
                    }

                    // سایز پایه‌ی عکس در حالت fit (قبل از userScale) به px، سپس به dp
                    val baseWpx = bmp.width * fitScale
                    val baseHpx = bmp.height * fitScale
                    val baseWdp = with(density) { baseWpx.toDp() }
                    val baseHdp = with(density) { baseHpx.toDp() }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // هدر
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 4.dp,
                                    vertical = 8.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "انصراف",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                "ویرایش عکس",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = {
                                    if (isSaving) return@IconButton
                                    isSaving = true
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val outFile = cropBitmapToFile(
                                                context = context,
                                                original = bmp,
                                                boxSizePx = boxSizePx.toInt(),
                                                outputSize = outputSize,
                                                fitScale = fitScale,
                                                userScale = userScale,
                                                offset = offset,
                                                rotation = rotation
                                            )
                                            withContext(Dispatchers.Main) {
                                                isSaving = false
                                                onConfirm(outFile)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isSaving = false
                                                error = "خطا در ذخیره تصویر"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        GoldPrimary,
                                        CircleShape
                                    )
                            ) {
                                if (isSaving) CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                else Icon(
                                    Icons.Default.Check,
                                    contentDescription = "تأیید",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "عکس را با دو انگشت بزرگ/کوچک و با یک انگشت جابجا کنید",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(16.dp))

                        // باکس کراپ مربعی با پیش‌نمایش دایره‌ای
                        Box(
                            modifier = Modifier
                                .size(boxSizeDp)
                                .border(
                                    2.dp,
                                    Color.White.copy(alpha = 0.9f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF111111)),
                            contentAlignment = Alignment.Center
                        ) {
                            // لایه‌ی تصویر قابل ژست
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(
                                        bmp,
                                        userScale,
                                        rotation
                                    ) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val newScale = (userScale * zoom).coerceIn(
                                                1f,
                                                4f
                                            )
                                            if (newScale != userScale) userScale = newScale
                                            // پن را با max فعلی clamp کن (max در recomposition بعدی به‌روز می‌شود ولی اینجا هم محدود می‌کنیم)
                                            val newOffset = offset + pan
                                            offset = Offset(
                                                x = newOffset.x.coerceIn(
                                                    -maxOffsetX,
                                                    maxOffsetX
                                                ),
                                                y = newOffset.y.coerceIn(
                                                    -maxOffsetY,
                                                    maxOffsetY
                                                )
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // تک Image با سایز fit + graphicsLayer برای userScale/offset/rotation
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .size(
                                            baseWdp,
                                            baseHdp
                                        )
                                        .graphicsLayer(
                                            scaleX = userScale,
                                            scaleY = userScale,
                                            translationX = offset.x,
                                            translationY = offset.y,
                                            rotationZ = rotation
                                        )
                                )
                            }

                            // راهنمای دایره‌ای (مثل تلگرام)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.65f),
                                        CircleShape
                                    )
                            )
                            // شبکه ۳×۳ ظریف
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            ) {
                                // خطوط عمودی
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .align(Alignment.Center)
                                        .background(Color.White.copy(alpha = 0.18f))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .align(Alignment.Center)
                                        .background(Color.White.copy(alpha = 0.18f))
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // کنترل‌ها: اسلایدر زوم + دکمه‌ها
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "−",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            Slider(
                                value = userScale,
                                onValueChange = { v ->
                                    userScale = v
                                    // offset را در محدوده نگه دار
                                    offset = Offset(
                                        x = offset.x.coerceIn(
                                            -maxOffsetX,
                                            maxOffsetX
                                        ),
                                        y = offset.y.coerceIn(
                                            -maxOffsetY,
                                            maxOffsetY
                                        )
                                    )
                                },
                                valueRange = 1f..4f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                )
                            )
                            Text(
                                "+",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    rotation = (rotation + 90f) % 360f
                                    // پس از چرخش، پن را دوباره clamp کن (width/height جا‌به‌جا می‌شود ولی max تقریبی است — در حالت مربعی فرق نمی‌کند)
                                    offset = Offset(
                                        x = offset.x.coerceIn(
                                            -maxOffsetY,
                                            maxOffsetY
                                        ),
                                        y = offset.y.coerceIn(
                                            -maxOffsetX,
                                            maxOffsetX
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    Icons.Default.RotateRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("چرخش ۹۰°")
                            }
                            OutlinedButton(
                                onClick = {
                                    userScale = 1f
                                    offset = Offset.Zero
                                    rotation = 0f
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("بازنشانی")
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // پیش‌نمایش دایره‌ای کوچک از خروجی واقعی (همان totalScale/offset)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "پیش‌نمایش:",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF222222))
                                    .border(
                                        2.dp,
                                        Color.White.copy(alpha = 0.7f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .size(
                                                baseWdp * 0.24f,
                                                baseHdp * 0.24f
                                            )
                                            .graphicsLayer(
                                                scaleX = userScale,
                                                scaleY = userScale,
                                                translationX = offset.x * 0.24f,
                                                translationY = offset.y * 0.24f,
                                                rotationZ = rotation
                                            )
                                    )
                                }
                            }
                            Text(
                                "خروجی مربعی ۵۱۲×۵۱۲ ذخیره می‌شود",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

private fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    maxDim: Int = 1024
): Bitmap {
    val opts = BitmapFactory.Options()
        .apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)
        ?.use {
            BitmapFactory.decodeStream(
                it,
                null,
                opts
            )
        }
    var sample = 1
    val maxSide = maxOf(
        opts.outWidth,
        opts.outHeight
    )
    while (maxSide / sample > maxDim) sample *= 2

    val decodeOpts = BitmapFactory.Options()
        .apply { inSampleSize = sample }
    var bmp = context.contentResolver.openInputStream(uri)
        ?.use {
            BitmapFactory.decodeStream(
                it,
                null,
                decodeOpts
            )
        }
            ?: throw IllegalArgumentException("decode failed")
    // Exif orientation
    try {
        context.contentResolver.openInputStream(uri)
            ?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val angle = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else                                 -> 0f
                }
                if (angle != 0f) {
                    val m = Matrix().apply { postRotate(angle) }
                    val rotated = Bitmap.createBitmap(
                        bmp,
                        0,
                        0,
                        bmp.width,
                        bmp.height,
                        m,
                        true
                    )
                    if (rotated != bmp) {
                        bmp.recycle(); bmp = rotated
                    }
                }
            }
    } catch (_: Exception) {
    }
    return bmp
}

/**
 * کراپ مربعی: boxSizePx×boxSizePx از مرکز => scale به outputSize×outputSize
 * ماتریس دقیقاً همان چیزی است که در پیش‌نمایش نمایش داده می‌شود.
 */
private fun cropBitmapToFile(
    context: Context,
    original: Bitmap,
    boxSizePx: Int,
    outputSize: Int,
    fitScale: Float,
    userScale: Float,
    offset: Offset,
    rotation: Float
): File {
    val totalScale = fitScale * userScale
    // بوم مربعی خروجی
    val out = Bitmap.createBitmap(
        outputSize,
        outputSize,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(out)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    val matrix = Matrix()

    // ترتیب: مثل پیش‌نمایش - ابتدا fit+userScale، سپس offset، و چرخش حول مرکز
    // 1) مقیاس کل
    matrix.postScale(
        totalScale,
        totalScale
    )
    // 2) انتقال به مرکز باکس (+پن کاربر)
    // در حالت fit، عکس به اندازه fitScale بزرگ شده و در مرکز باکس قرار دارد
    // نقطه ۰,۰ عکس باید به (box/2 - bmp*totalScale/2 + offset) منتقل شود، ولی در بوم خروجی مبدأ (0,0) گوشه‌ی باکس است
    val dx = (boxSizePx - original.width * totalScale) / 2f + offset.x
    val dy = (boxSizePx - original.height * totalScale) / 2f + offset.y
    matrix.postTranslate(
        dx,
        dy
    )
    // 3) چرخش حول مرکز باکس
    if (rotation != 0f) {
        matrix.postRotate(
            rotation,
            boxSizePx / 2f,
            boxSizePx / 2f
        )
    }
    // 4) مقیاس باکس به اندازه‌ی خروجی
    val boxToOut = outputSize.toFloat() / boxSizePx
    matrix.postScale(
        boxToOut,
        boxToOut
    )

    canvas.drawBitmap(
        original,
        matrix,
        paint
    )

    val outFile = File(
        context.cacheDir,
        "avatar_crop_${System.currentTimeMillis()}.jpg"
    )
    FileOutputStream(outFile).use { fos ->
        out.compress(
            Bitmap.CompressFormat.JPEG,
            92,
            fos
        )
    }
    out.recycle()
    return outFile
}
