package com.khz.malekclient.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.Coil
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.util.appViewModel
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.Guardian
import com.khz.malekclient.domain.model.PlayerProfile
import com.khz.malekclient.ui.components.AvatarView
import com.khz.malekclient.ui.components.ErrorContent
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.components.GlassTopBar
import com.khz.malekclient.ui.components.JalaliDateField
import com.khz.malekclient.ui.components.LoadingContent
import com.khz.malekclient.ui.theme.GlassBorder
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.PurplePrimary
import com.khz.malekclient.ui.theme.RedError
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ProfileViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val showLogoutDialog = remember { mutableStateOf(false) }
    val localPreviewUri = remember { mutableStateOf<Uri?>(null) }

    // Image picker - پیش‌نمایش فوری + آپلود
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            localPreviewUri.value = uri // نمایش فوری
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(
                    context.cacheDir,
                    "avatar_${System.currentTimeMillis()}.jpg"
                )
                FileOutputStream(file).use { out ->
                    inputStream?.copyTo(out)
                }
                // پاک کردن کش Coil برای آواتار قبلی تا عکس جدید لود شود
                try {
                    Coil.imageLoader(context).memoryCache?.clear()
                } catch (_: Exception) {
                }
                viewModel.uploadAvatar(file)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    // بعد از موفقیت آپلود، کش را پاک کن و پیش‌نمایش لوکال را نگه دار تا URL جدید بیاید
    LaunchedEffect(
        state.profile?.player?.avatarUrl,
        state.profile?.user?.avatarUrl
    ) {
        // وقتی URL جدید از سرور آمد، پیش‌نمایش لوکال را پاک می‌کنیم تا URL سرور نمایش داده شود
        if (state.profile != null && !state.uploading) {
            // کمی تاخیر برای اطمینان از لود عکس جدید
            // localPreviewUri.value = null // نگه می‌داریم تا مطمئن شویم URL جدید لود شده
            // اگر URL جدید وجود دارد، کش را پاک کن
            try {
                Coil.imageLoader(context).memoryCache?.clear()
            } catch (_: Exception) {
            }
        }
    }

    if (showLogoutDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = {
                Text(
                    "خروج از حساب",
                    color = Color.White
                )
            },
            text = {
                Text(
                    "آیا مطمئن هستید که می‌خواهید از حساب خارج شوید؟",
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog.value = false
                    viewModel.logout()
                }) {
                    Text(
                        "خروج",
                        color = RedError
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) {
                    Text(
                        "انصراف",
                        color = Color.White
                    )
                }
            },
            containerColor = PurplePrimary.copy(alpha = 0.95f)
        )
    }

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "پروفایل من",
                onBack = onBack
            )

            when {
                state.loading -> LoadingContent()
                state.error != null && state.profile == null -> ErrorContent(
                    state.error!!,
                    onRetry = { viewModel.refresh() })

                state.profile != null -> {
                    val profile = state.profile!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // پیام موفقیت / خطا
                        if (state.error != null || state.successMessage != null) {
                            item {
                                if (state.error != null) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(RedError.copy(0.15f))
                                            .border(
                                                0.5.dp,
                                                RedError.copy(0.3f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                state.error!!.toPersianDigits(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { viewModel.clearMessage() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    null,
                                                    tint = Color.White.copy(0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (state.successMessage != null) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF4CAF50).copy(0.15f))
                                            .border(
                                                0.5.dp,
                                                Color(0xFF4CAF50).copy(0.3f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                state.successMessage!!.toPersianDigits(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { viewModel.clearMessage() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    null,
                                                    tint = Color.White.copy(0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // کارت اصلی بازیکن - GlassCard3D جدید با آواتار بزرگ
                        item {
                            PlayerHeaderCard(
                                profile = profile,
                                uploading = state.uploading,
                                localUri = localPreviewUri.value,
                                isEditing = state.isEditingPlayer,
                                firstName = state.playerFirstName,
                                lastName = state.playerLastName,
                                birthDate = state.playerBirthDate,
                                gender = state.playerGender,
                                saving = state.saving,
                                onFirstNameChange = viewModel::onPlayerFirstNameChange,
                                onLastNameChange = viewModel::onPlayerLastNameChange,
                                onBirthDateChange = viewModel::onPlayerBirthDateChange,
                                onGenderChange = viewModel::onPlayerGenderChange,
                                onEdit = { viewModel.startEditPlayer() },
                                onCancel = { viewModel.cancelEditPlayer() },
                                onSave = { viewModel.savePlayer() },
                                onPickImage = { imagePicker.launch("image/*") },
                                onDeleteAvatar = {
                                    localPreviewUri.value = null
                                    try {
                                        Coil.imageLoader(context).memoryCache?.clear()
                                    } catch (_: Exception) {
                                    }
                                    viewModel.deleteAvatar()
                                })
                        }

                        // اطلاعات حساب کاربری (user) - قابل ویرایش به جز کد ملی
                        item {
                            UserInfoCard(
                                profile = profile,
                                isEditing = state.isEditingUser,
                                fullName = state.userFullName,
                                mobile = state.userMobile,
                                saving = state.saving,
                                onFullNameChange = viewModel::onUserFullNameChange,
                                onMobileChange = viewModel::onUserMobileChange,
                                onEdit = { viewModel.startEditUser() },
                                onCancel = { viewModel.cancelEditUser() },
                                onSave = { viewModel.saveUser() })
                        }

                        // سرپرستان
                        if (profile.guardians.isNotEmpty()) {
                            item { SectionTitle("سرپرستان") }
                            profile.guardians.forEach { guardian ->
                                item {
                                    GuardianCard(
                                        guardian = guardian,
                                        isEditing = state.editingGuardianId == guardian.id,
                                        fullName = state.guardianFullName,
                                        mobile = state.guardianMobile,
                                        emergency = state.guardianEmergency,
                                        relation = state.guardianRelation,
                                        saving = state.saving,
                                        onFullNameChange = viewModel::onGuardianFullNameChange,
                                        onMobileChange = viewModel::onGuardianMobileChange,
                                        onEmergencyChange = viewModel::onGuardianEmergencyChange,
                                        onRelationChange = viewModel::onGuardianRelationChange,
                                        onEdit = { viewModel.startEditGuardian(guardian) },
                                        onCancel = { viewModel.cancelEditGuardian() },
                                        onSave = { viewModel.saveGuardian() })
                                }
                            }
                        }

                        // خروج - پایین صفحه
                        item {
                            Spacer(Modifier.height(16.dp))
                            GlassCard3D(
                                onClick = { showLogoutDialog.value = true }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "خروج از حساب",
                                        color = Color(0xFFFF8A80),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = GoldPrimary,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(
            top = 8.dp,
            start = 4.dp
        )
    )
}

@Composable
private fun PlayerHeaderCard(
    profile: PlayerProfile,
    uploading: Boolean,
    localUri: Uri?,
    isEditing: Boolean,
    firstName: String,
    lastName: String,
    birthDate: String,
    gender: String,
    saving: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onPickImage: () -> Unit,
    onDeleteAvatar: () -> Unit
) {
    val player = profile.player
    val user = profile.user
    GlassCard3D {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // آواتار بزرگ با دکمه ویرایش + پیش‌نمایش لوکال
                Box {
                    AvatarView(
                        name = player?.fullName
                                ?: user.fullName,
                        avatarUrl = player?.avatarUrl
                                ?: user.avatarUrl,
                        avatarUri = localUri,
                        size = 88.dp,
                        accentColor = GoldPrimary
                    )
                    if (uploading) {
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = GoldPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    // دکمه دوربین
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                            .border(
                                2.dp,
                                Color.White,
                                CircleShape
                            )
                            .clickable { onPickImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (player?.fullName
                                ?: user.fullName).toPersianDigits(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(4.dp))
                    // چیپ وضعیت مالی
                    val debt = player?.let {
                        // balance not in PlayerProfile yet, but we can show from child? For now use 0
                        0L
                    }
                            ?: 0L
                    // نمایش کد ملی غیرقابل ویرایش
                    if (!player?.nationalCode.isNullOrBlank()) {
                        Text(
                            text = "کد ملی: ${player?.nationalCode?.toPersianDigits()}",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    if (!player?.birthDate.isNullOrBlank()) {
                        Text(
                            text = "تولد: ${
                                DateUtils.toJalaliReadable(player?.birthDate)
                                    .toPersianDigits()
                            }",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (!player?.gender.isNullOrBlank()) {
                        Text(
                            text = when (player?.gender) {
                                "male" -> "پسر"; "female" -> "دختر"; else -> player?.gender
                                        ?: ""
                            },
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ویرایش",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // بخش ویرایش بازیکن
            if (isEditing) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(0.1f))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "ویرایش اطلاعات بازیکن",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassTextField(
                        value = firstName,
                        onValueChange = onFirstNameChange,
                        label = "نام",
                        modifier = Modifier.weight(1f)
                    )
                    GlassTextField(
                        value = lastName,
                        onValueChange = onLastNameChange,
                        label = "نام خانوادگی",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                // استفاده از دیالوگ شمسی ادمین برای ویرایش تاریخ تولد
                JalaliDateField(
                    label = "تاریخ تولد",
                    gregorianValue = birthDate.ifBlank { null },
                    onDatePicked = { gregorian -> onBirthDateChange(gregorian) })
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenderChip(
                        "پسر",
                        selected = gender == "male",
                        onClick = { onGenderChange("male") })
                    GenderChip(
                        "دختر",
                        selected = gender == "female",
                        onClick = { onGenderChange("female") })
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "کد ملی غیرقابل ویرایش است",
                    color = Color.White.copy(0.4f),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Button(
                        onClick = onSave,
                        enabled = !saving,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        if (saving) CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        else {
                            Icon(
                                Icons.Default.Save,
                                null,
                                modifier = Modifier.size(16.dp)
                            ); Spacer(Modifier.width(6.dp)); Text("ذخیره")
                        }
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            modifier = Modifier.size(16.dp)
                        ); Spacer(Modifier.width(6.dp)); Text("انصراف")
                    }
                }
            } else {
                // نمایش اطلاعات غیرقابل ویرایش + دکمه حذف آواتار
                if (!user.avatarUrl.isNullOrBlank() || !player?.avatarUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row {
                        TextButton(onClick = onDeleteAvatar) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "حذف عکس",
                                color = Color(0xFFFF8A80),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(
    profile: PlayerProfile,
    isEditing: Boolean,
    fullName: String,
    mobile: String,
    saving: Boolean,
    onFullNameChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    GlassCard3D {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "اطلاعات کاربری",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (!isEditing) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color.White.copy(0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (isEditing) {
                GlassTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = "نام کامل",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GlassTextField(
                    value = mobile,
                    onValueChange = onMobileChange,
                    label = "موبایل",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "کد ملی: ${(profile.user.nationalCode ?: "—").toPersianDigits()} (غیرقابل ویرایش)",
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Button(
                        onClick = onSave,
                        enabled = !saving,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        if (saving) CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        else Text("ذخیره")
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) { Text("انصراف") }
                }
            } else {
                InfoRow(
                    "نام کامل",
                    profile.user.fullName
                )
                InfoRow(
                    "موبایل",
                    profile.user.mobile
                            ?: "—"
                )
                InfoRow(
                    "کد ملی",
                    profile.user.nationalCode
                            ?: "—"
                )
            }
        }
    }
}

@Composable
private fun GuardianCard(
    guardian: Guardian,
    isEditing: Boolean,
    fullName: String,
    mobile: String,
    emergency: String,
    relation: String,
    saving: Boolean,
    onFullNameChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onEmergencyChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    GlassCard3D {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            guardian.fullName.toPersianDigits(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (!guardian.relation.isNullOrBlank()) {
                            Text(
                                guardian.relationLabel,
                                color = GoldPrimary.copy(0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                if (!isEditing) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color.White.copy(0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (isEditing) {
                GlassTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = "نام سرپرست",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GlassTextField(
                    value = mobile,
                    onValueChange = onMobileChange,
                    label = "موبایل",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GlassTextField(
                    value = emergency,
                    onValueChange = onEmergencyChange,
                    label = "تلفن اضطراری",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                // انتخاب نسبت — فقط برچسب‌های فارسی (ارزش ذخیره‌شده کلید انگلیسی است)
                Text(
                    "نسبت",
                    color = Color.White.copy(0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Guardian.relationOptions.forEach { (key, label) ->
                        RelationChip(
                            text = label,
                            selected = relation == key,
                            onClick = { onRelationChange(key) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "کد ملی سرپرست غیرقابل ویرایش است",
                    color = Color.White.copy(0.4f),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Button(
                        onClick = onSave,
                        enabled = !saving,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        if (saving) CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        else Text("ذخیره")
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) { Text("انصراف") }
                }
            } else {
                InfoRow(
                    "موبایل",
                    guardian.mobile
                            ?: "—"
                )
                InfoRow(
                    "اضطراری",
                    guardian.emergencyPhone
                            ?: "—"
                )
                InfoRow(
                    "کد ملی",
                    guardian.nationalCode
                            ?: "—"
                )
            }
        }
    }
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = Color.White.copy(0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = GlassBorder,
            cursorColor = GoldPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun RelationChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) GoldPrimary.copy(0.25f) else Color.White.copy(0.08f))
            .border(
                0.5.dp,
                if (selected) GoldPrimary else GlassBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            )) {
        Text(
            text,
            color = if (selected) GoldPrimary else Color.White.copy(0.7f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) GoldPrimary.copy(0.25f) else Color.White.copy(0.08f))
            .border(
                0.5.dp,
                if (selected) GoldPrimary else GlassBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )) {
        Text(
            text,
            color = if (selected) GoldPrimary else Color.White.copy(0.7f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color.White.copy(0.55f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value.toPersianDigits(),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
