package com.carlauncherthatsucks.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.carlauncherthatsucks.data.API_PRESETS
import com.carlauncherthatsucks.data.ApiConfig
import com.carlauncherthatsucks.data.AssistantProvider
import kotlinx.coroutines.launch

/** Scelta dell'assistente del footer e, per l'API, endpoint, chiave e modello. */
@Composable
fun AssistantSettings(
    provider: AssistantProvider,
    api: ApiConfig,
    onProvider: (AssistantProvider) -> Unit,
    onApi: (ApiConfig) -> Unit,
    onTestApi: suspend (ApiConfig) -> Result<String>,
) {
    PillTabs(
        options = listOf(
            Triple(Icons.Filled.GraphicEq, AssistantProvider.CLAUDE.label, provider == AssistantProvider.CLAUDE),
            Triple(Icons.Filled.AutoAwesome, AssistantProvider.GEMINI.label, provider == AssistantProvider.GEMINI),
            Triple(Icons.Filled.Api, AssistantProvider.OPENAI_COMPATIBLE.label, provider == AssistantProvider.OPENAI_COMPATIBLE),
        ),
        onSelect = { onProvider(AssistantProvider.entries[it]) },
    )
    Text(
        when (provider) {
            AssistantProvider.CLAUDE -> "Apre l'app Claude nella modalità vocale, se disponibile."
            AssistantProvider.GEMINI -> "Apre l'app Gemini (Gemini Live, se disponibile)."
            AssistantProvider.OPENAI_COMPATIBLE ->
                "Chat vocale dentro il launcher con qualsiasi API compatibile OpenAI (/chat/completions): " +
                    "parli, il modello risponde e la risposta viene letta ad alta voce."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = Dash.Label,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
    if (provider != AssistantProvider.OPENAI_COMPATIBLE) return

    var baseUrl by remember { mutableStateOf(api.baseUrl) }
    var apiKey by remember { mutableStateOf(api.apiKey) }
    var model by remember { mutableStateOf(api.model) }
    var showKey by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Result<String>?>(null) }
    val scope = rememberCoroutineScope()
    fun save() {
        testResult = null
        onApi(ApiConfig(baseUrl.trim(), apiKey.trim(), model.trim()))
    }

    // Preset: riempiono URL e un modello d'esempio (modificabile).
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        API_PRESETS.forEach { preset ->
            val selected = baseUrl.trim().trimEnd('/') == preset.baseUrl
            Surface(
                onClick = {
                    baseUrl = preset.baseUrl
                    if (model.isBlank() || !selected) model = preset.exampleModel
                    save()
                },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) Dash.BacklightDim else Dash.Panel,
                contentColor = if (selected) Dash.OnBacklightDim else Dash.Label,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 14.dp))
                }
            }
        }
    }

    OutlinedTextField(
        value = baseUrl,
        onValueChange = { baseUrl = it; save() },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        label = { Text("URL base") },
        placeholder = { Text("https://…/v1") },
        leadingIcon = { Icon(Icons.Filled.Link, null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )
    OutlinedTextField(
        value = apiKey,
        onValueChange = { apiKey = it; save() },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        label = { Text("Chiave API") },
        placeholder = { Text("Vuota per server locali") },
        leadingIcon = { Icon(Icons.Filled.Key, null) },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { showKey = !showKey }) {
                Icon(
                    if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    if (showKey) "Nascondi chiave" else "Mostra chiave",
                )
            }
        },
        supportingText = { Text("Salvata solo sul telefono, esclusa dai backup") },
    )
    OutlinedTextField(
        value = model,
        onValueChange = { model = it; save() },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        label = { Text("Modello") },
        leadingIcon = { Icon(Icons.Filled.SmartToy, null) },
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(
            onClick = {
                testing = true
                testResult = null
                scope.launch {
                    testResult = onTestApi(ApiConfig(baseUrl.trim(), apiKey.trim(), model.trim()))
                    testing = false
                }
            },
            enabled = !testing && baseUrl.isNotBlank() && model.isNotBlank(),
            modifier = Modifier.heightIn(min = 48.dp),
        ) { Text(if (testing) "Prova in corso…" else "Prova connessione") }
        Spacer(Modifier.width(12.dp))
        testResult?.let { r ->
            if (r.isSuccess) {
                Icon(Icons.Filled.CheckCircle, null, tint = Dash.Label, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Funziona", style = MaterialTheme.typography.labelLarge, color = Dash.Label)
            }
        }
    }
    testResult?.exceptionOrNull()?.let { e ->
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Lamp()
            Spacer(Modifier.width(8.dp))
            Text(e.message ?: "Errore", style = MaterialTheme.typography.bodyMedium, color = Dash.WarningLamp)
        }
    }
    if (baseUrl.trim().startsWith("http://")) {
        Text(
            "Connessione non cifrata: usala solo per server nella tua rete.",
            style = MaterialTheme.typography.bodySmall,
            color = Dash.Note,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

