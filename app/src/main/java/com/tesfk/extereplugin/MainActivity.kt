package com.tesfk.extereplugin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tesfk.extereplugin.data.PluginBlueprint
import com.tesfk.extereplugin.data.PluginDraft
import com.tesfk.extereplugin.data.PluginDraftStorage
import com.tesfk.extereplugin.data.PluginValidator
import com.tesfk.extereplugin.files.PluginFileManager
import com.tesfk.extereplugin.ui.theme.ExterePluginTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val draftStorage = PluginDraftStorage(this)
        val fileManager = PluginFileManager(this)
        setContent {
            ExterePluginTheme {
                PluginLabApp(draftStorage, fileManager)
            }
        }
    }
}

@Composable
fun PluginLabApp(
    draftStorage: PluginDraftStorage,
    fileManager: PluginFileManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val draft by draftStorage.draft.collectAsState(initial = PluginDraft())
    var blueprint by remember { mutableStateOf(draft.toBlueprint()) }
    var recentFiles by remember { mutableStateOf(fileManager.listRecentFiles()) }

    LaunchedEffect(draft) {
        blueprint = draft.toBlueprint()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PluginIdentityCard(blueprint) { updated ->
                    blueprint = updated
                    coroutineScope.launch { draftStorage.saveDraft(updated.asDraft()) }
                }
            }
            item {
                EntryPointEditor(
                    entryPoint = blueprint.entryPoint,
                    onChange = {
                        blueprint = blueprint.copy(entryPoint = it)
                        coroutineScope.launch { draftStorage.saveDraft(blueprint.asDraft()) }
                    }
                )
            }
            item {
                ActionButtons(
                    onGenerate = {
                        val result = PluginValidator.validate(blueprint)
                        if (result.isValid) {
                            coroutineScope.launch {
                                try {
                                    val file = fileManager.writePluginFile(blueprint)
                                    recentFiles = fileManager.listRecentFiles()
                                    snackbarHostState.showSnackbar(
                                        message = "Плагин сохранён: ${file.name}",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (error: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Не удалось записать файл: ${error.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = result.issues.joinToString("\n"),
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    onShare = {
                        coroutineScope.launch {
                            val file = recentFiles.firstOrNull()
                            if (file == null) {
                                snackbarHostState.showSnackbar("Сначала сгенерируйте файл")
                            } else {
                                val intent = Intent.createChooser(
                                    fileManager.shareIntent(file),
                                    "Отправить .plugin"
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }
            item {
                RecentFilesCard(recentFiles)
            }
        }
    }
}

@Composable
fun PluginIdentityCard(
    blueprint: PluginBlueprint,
    onChange: (PluginBlueprint) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Основные метаданные",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LabeledField(
                label = "Название",
                value = blueprint.name,
                onValueChange = { onChange(blueprint.copy(name = it)) }
            )
            LabeledField(
                label = "Package ID",
                value = blueprint.packageId,
                supportingText = "Например: com.extere.plugin.echo",
                onValueChange = { onChange(blueprint.copy(packageId = it)) }
            )
            LabeledField(
                label = "Версия",
                value = blueprint.version,
                onValueChange = { onChange(blueprint.copy(version = it)) }
            )
            LabeledField(
                label = "Автор",
                value = blueprint.author,
                onValueChange = { onChange(blueprint.copy(author = it)) }
            )
            LabeledField(
                label = "Описание",
                value = blueprint.description,
                singleLine = false,
                onValueChange = { onChange(blueprint.copy(description = it)) }
            )
        }
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } }
    )
}

@Composable
fun EntryPointEditor(
    entryPoint: String,
    onChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Python точка входа", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                value = entryPoint,
                onValueChange = onChange,
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = false,
                placeholder = { Text(stringResource(id = R.string.plugin_code_placeholder)) }
            )
        }
    }
}

@Composable
fun ActionButtons(
    onGenerate: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                Text("Сформировать .plugin")
            }
            OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                Text("Поделиться последним файлом")
            }
        }
    }
}

@Composable
fun RecentFilesCard(files: List<File>) {
    if (files.isEmpty()) return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Недавние плагины", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            files.take(3).forEach { file ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(file.name, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${file.length()} bytes · ${file.lastModified().asDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun Long.asDate(): String {
    val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    return formatter.format(this)
}

private fun PluginDraft.toBlueprint() = PluginBlueprint(
    name = name,
    packageId = packageId,
    description = description,
    version = version,
    author = author,
    entryPoint = entryPoint
)

private fun PluginBlueprint.asDraft() = PluginDraft(
    name = name,
    packageId = packageId,
    description = description,
    version = version,
    author = author,
    entryPoint = entryPoint
)
