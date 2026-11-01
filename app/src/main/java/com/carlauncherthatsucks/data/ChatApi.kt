package com.carlauncherthatsucks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Quale assistente usa il pulsante del footer. */
enum class AssistantProvider(val label: String) {
    CLAUDE("Claude"),
    GEMINI("Gemini"),
    OPENAI_COMPATIBLE("API"),
}

/** Endpoint compatibile con l'API Chat Completions di OpenAI. */
data class ApiConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
) {
    val isComplete: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank()
}

/** Configurazioni pronte; il modello è solo un esempio modificabile. */
data class ApiPreset(val name: String, val baseUrl: String, val exampleModel: String, val needsKey: Boolean = true)

val API_PRESETS = listOf(
    ApiPreset("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
    ApiPreset("Gemini API", "https://generativelanguage.googleapis.com/v1beta/openai", "gemini-2.5-flash"),
    ApiPreset("Claude API", "https://api.anthropic.com/v1", "claude-haiku-4-5"),
    ApiPreset("OpenRouter", "https://openrouter.ai/api/v1", "openrouter/auto"),
    ApiPreset("Ollama (rete locale)", "http://192.168.1.10:11434/v1", "llama3.2", needsKey = false),
)

data class ChatMessage(val role: String, val content: String)

/** Istruzioni di sistema: risposte brevi, da ascoltare mentre si guida. */
const val DRIVING_SYSTEM_PROMPT =
    "Sei un assistente vocale in auto. Rispondi nella lingua dell'utente (di solito italiano), " +
        "in modo breve: al massimo due o tre frasi. Niente elenchi, tabelle, emoji o markdown: " +
        "la risposta verrà letta ad alta voce a chi sta guidando."

class ChatApiException(message: String) : Exception(message)

object ChatApi {
    /** Invia la conversazione e restituisce la risposta testuale dell'assistente. */
    suspend fun complete(config: ApiConfig, history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        if (!config.isComplete) throw ChatApiException("Configura URL e modello nelle impostazioni")
        val conn = URL(chatCompletionsUrl(config.baseUrl)).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            if (config.apiKey.isNotBlank()) conn.setRequestProperty("Authorization", "Bearer ${config.apiKey.trim()}")
            conn.outputStream.use { it.write(buildRequestBody(config.model, history).toByteArray()) }
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw ChatApiException(parseError(code, body))
            parseCompletion(body)
        } catch (e: ChatApiException) {
            throw e
        } catch (e: java.io.IOException) {
            throw ChatApiException("Connessione non riuscita: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            conn.disconnect()
        }
    }

    /** Accetta ".../v1", ".../v1/" o l'URL completo ".../chat/completions". */
    fun chatCompletionsUrl(baseUrl: String): String {
        val base = baseUrl.trim().trimEnd('/')
        return if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
    }

    fun buildRequestBody(model: String, history: List<ChatMessage>): String {
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", DRIVING_SYSTEM_PROMPT))
        history.forEach { messages.put(JSONObject().put("role", it.role).put("content", it.content)) }
        return JSONObject()
            .put("model", model.trim())
            .put("messages", messages)
            .put("stream", false)
            .toString()
    }

    fun parseCompletion(json: String): String {
        val choices = JSONObject(json).optJSONArray("choices")
        if (choices == null || choices.length() == 0) throw ChatApiException("Risposta senza testo")
        val message = choices.getJSONObject(0).optJSONObject("message")
        val content = message?.opt("content")
        val text = when (content) {
            is String -> content
            // Alcuni server restituiscono il contenuto a blocchi [{type:"text", text:"..."}].
            is JSONArray -> (0 until content.length())
                .mapNotNull { content.optJSONObject(it)?.optString("text") }
                .joinToString("")
            else -> ""
        }
        if (text.isBlank()) throw ChatApiException("Risposta vuota")
        return text.trim()
    }

    fun parseError(code: Int, body: String): String {
        val detail = try {
            val o = JSONObject(body)
            o.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: o.optString("message").takeIf { it.isNotBlank() }
                ?: o.optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val hint = when (code) {
            401, 403 -> "chiave API non valida"
            404 -> "URL o modello non trovato"
            429 -> "troppe richieste o credito esaurito"
            else -> "errore $code"
        }
        return if (detail != null) "$hint: $detail" else hint.replaceFirstChar { it.uppercase() }
    }
}

/** Toglie il markdown prima di leggere ad alta voce. */
fun speakable(text: String): String = text
    .replace(Regex("```[\\s\\S]*?```"), " ")
    .replace(Regex("[*_#`>|]+"), "")
    .replace(Regex("^\\s*[-•]\\s+", RegexOption.MULTILINE), "")
    .replace(Regex("\\[(.*?)]\\((.*?)\\)"), "$1")
    .replace(Regex("\\s+"), " ")
    .trim()
