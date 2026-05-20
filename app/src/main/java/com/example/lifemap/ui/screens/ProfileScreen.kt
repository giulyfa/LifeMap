package com.example.lifemap.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.ui.CategoryStat
import com.example.lifemap.ui.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val categoryColors: Map<MemoryCategory, Color> = mapOf(
    MemoryCategory.VIAGGI to Color(0xFF66BB6A),
    MemoryCategory.AMICI  to Color(0xFFF06292),
    MemoryCategory.CIBO   to Color(0xFFFFA726),
    MemoryCategory.SPORT  to Color(0xFF42A5F5),
    MemoryCategory.VARIE  to Color(0xFFAB47BC)
)

private val fallbackColors = listOf(
    Color(0xFF64B5F6), Color(0xFFA5D6A7), Color(0xFFFFCC02),
    Color(0xFFCE93D8), Color(0xFFEF9A9A), Color(0xFF80DEEA)
)

private fun colorForCategory(category: MemoryCategory, index: Int): Color =
    categoryColors[category] ?: fallbackColors[index % fallbackColors.size]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    // Stato per mostrare/nascondere il dialog di scelta
    var showSourceDialog by remember { mutableStateOf(false) }

    // Stato per salvare l'Uri temporaneo della fotocamera
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // --- LAUNCHER PER LA GALLERIA (PHOTO PICKER SENZA PERMESSI) ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            vm.updateProfilePhoto(it.toString())
        }
    }

    // --- LAUNCHERS PER LA FOTOCAMERA ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempCameraUri?.let { vm.updateProfilePhoto(it.toString()) }
        } else {
            Toast.makeText(context, "Scatto annullato", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            tempCameraUri = context.createTempImageUri()
            tempCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Permesso negato per la fotocamera", Toast.LENGTH_SHORT).show()
        }
    }

    // --- DIALOG DI SCELTA ---
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Cambia foto profilo") },
            text = { Text("Scegli da dove vuoi prendere la foto.") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        tempCameraUri = context.createTempImageUri()
                        tempCameraUri?.let { cameraLauncher.launch(it) }
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Fotocamera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    // Lanciamo il Photo Picker! Nessun permesso richiesto.
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Galleria")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserInfoCard(
                fname = state.user?.nome ?: "User",
                lname = state.user?.cognome ?: "Sconosciuto",
                email = state.user?.email ?: "—",
                profilePhotoUri = state.profilePhotoUri,
                onChangePhoto = { showSourceDialog = true }
            )

            StatsCard(totalMemories = state.totalMemories)

            if (state.categoryStats.isNotEmpty()) PieChartCard(stats = state.categoryStats)
            else EmptyMemoriesCard()
        }
    }
}

// Funzione helper per creare l'Uri temporaneo per la fotocamera
private fun Context.createTempImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        cacheDir // Usiamo la cacheDir interna in modo sicuro
    )

    // IMPORTANTE: deve corrispondere all'authorities nel Manifest
    return FileProvider.getUriForFile(
        this,
        "com.example.lifemap.fileprovider",
        imageFile
    )
}

// ... I COMPONENTI UI (UserInfoCard, StatsCard, PieChartCard, PieChart, Legend, EmptyMemoriesCard) RIMANGONO INVARIATI ...
// Ricordati di incollare qui sotto i componenti UI che c'erano prima, o di non cancellarli dal tuo file originale!

@Composable
private fun UserInfoCard(
    fname: String,
    lname: String,
    email: String,
    profilePhotoUri: String?,
    onChangePhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onChangePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profilePhotoUri.toUri())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto profilo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable { onChangePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Cambia foto profilo",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$fname $lname",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Text(
                    text = "Tocca la foto per cambiarla",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StatsCard(totalMemories: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "$totalMemories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (totalMemories == 1) "ricordo salvato" else "ricordi salvati",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PieChartCard(stats: List<CategoryStat>) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Ricordi per categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    stats = stats,
                    animationProgress = animationProgress.value,
                    modifier = Modifier.size(200.dp)
                )
            }

            Legend(stats = stats)
        }
    }
}

@Composable
private fun PieChart(
    stats: List<CategoryStat>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 48.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(x = center.x - radius, y = center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        var startAngle = -90f
        val totalSweep = 360f * animationProgress
        var remaining = totalSweep

        stats.forEachIndexed { index, stat ->
            val sweep = minOf(360f * stat.percentage, remaining)
            remaining -= sweep

            drawArc(
                color = colorForCategory(stat.category, index),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun Legend(stats: List<CategoryStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEachIndexed { index, stat ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorForCategory(stat.category, index))
                )
                Text(
                    text = stat.category.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${stat.count} (${(stat.percentage * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyMemoriesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = "Nessun ricordo ancora",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Aggiungi il tuo primo ricordo per vedere le statistiche",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}