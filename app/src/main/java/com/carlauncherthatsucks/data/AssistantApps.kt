package com.carlauncherthatsucks.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.widget.Toast

/** Assistente con un'app propria (Claude, Gemini). */
data class AssistantApp(val packageName: String, val name: String)

/**
 * Avvio della modalità vocale di un'app assistente.
 *
 * Non esistono deep link documentati per la modalità vocale: essendo il launcher predefinito,
 * cerchiamo tra le scorciatoie dell'app (quelle del tocco prolungato sull'icona) una scorciatoia
 * "vocale" e la avviamo; altrimenti apriamo semplicemente l'app.
 */
object AssistantApps {
    val CLAUDE = AssistantApp("com.anthropic.claude", "Claude")
    val GEMINI = AssistantApp("com.google.android.apps.bard", "Gemini")

    fun isInstalled(context: Context, app: AssistantApp): Boolean = try {
        context.packageManager.getPackageInfo(app.packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun startVoice(context: Context, app: AssistantApp) {
        if (!isInstalled(context, app)) {
            Toast.makeText(context, "L'app ${app.name} non è installata", Toast.LENGTH_LONG).show()
            openStore(context, app)
            return
        }
        if (startVoiceShortcut(context, app)) return
        val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launch != null) {
            context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Toast.makeText(context, "Tocca l'icona della voce per parlare con ${app.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceShortcut(context: Context, app: AssistantApp): Boolean {
        val la = context.getSystemService(LauncherApps::class.java) ?: return false
        return try {
            if (!la.hasShortcutHostPermission()) return false
            val query = LauncherApps.ShortcutQuery()
                .setPackage(app.packageName)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            val shortcuts = la.getShortcuts(query, Process.myUserHandle()).orEmpty()
            val voice = pickVoiceShortcut(
                shortcuts.map { ShortcutCandidate(it.id, it.shortLabel?.toString(), it.longLabel?.toString()) }
            ) ?: return false
            la.startShortcut(shortcuts.first { it.id == voice.id }, null, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun openStore(context: Context, app: AssistantApp) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(market)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
}

data class ShortcutCandidate(val id: String, val shortLabel: String?, val longLabel: String?)

/** "live" copre Gemini Live; le altre parole la modalità vocale di Claude. */
private val VOICE_WORDS = listOf("voice", "voce", "vocal", "talk", "parla", "speak", "live")

/** Sceglie la scorciatoia che sembra avviare la modalità vocale (per id o etichetta). */
fun pickVoiceShortcut(candidates: List<ShortcutCandidate>): ShortcutCandidate? =
    candidates.firstOrNull { c ->
        listOfNotNull(c.id, c.shortLabel, c.longLabel).any { text ->
            VOICE_WORDS.any { text.contains(it, ignoreCase = true) }
        }
    }
