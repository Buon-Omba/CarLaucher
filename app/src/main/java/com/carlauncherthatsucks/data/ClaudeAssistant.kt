package com.carlauncherthatsucks.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.widget.Toast

/**
 * Avvio della chat vocale con Claude.
 *
 * Non esiste un deep link documentato per la modalità vocale: essendo il launcher predefinito,
 * cerchiamo tra le scorciatoie dell'app Claude (quelle del tocco prolungato sull'icona) una
 * scorciatoia "vocale" e la avviamo; altrimenti apriamo semplicemente l'app.
 */
object ClaudeAssistant {
    const val PACKAGE = "com.anthropic.claude"

    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun startVoice(context: Context) {
        if (!isInstalled(context)) {
            Toast.makeText(context, "L'app Claude non è installata", Toast.LENGTH_LONG).show()
            openStore(context)
            return
        }
        if (startVoiceShortcut(context)) return
        val launch = context.packageManager.getLaunchIntentForPackage(PACKAGE)
        if (launch != null) {
            context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Toast.makeText(context, "Tocca l'icona della voce per parlare con Claude", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceShortcut(context: Context): Boolean {
        val la = context.getSystemService(LauncherApps::class.java) ?: return false
        return try {
            if (!la.hasShortcutHostPermission()) return false
            val query = LauncherApps.ShortcutQuery()
                .setPackage(PACKAGE)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            val shortcuts = la.getShortcuts(query, Process.myUserHandle()).orEmpty()
            val voice = pickVoiceShortcut(
                shortcuts.map { ShortcutCandidate(it.id, it.shortLabel?.toString(), it.longLabel?.toString()) }
            ) ?: return false
            val info = shortcuts.first { it.id == voice.id }
            la.startShortcut(info, null, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun openStore(context: Context) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(market)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://claude.ai/download"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
}

data class ShortcutCandidate(val id: String, val shortLabel: String?, val longLabel: String?)

private val VOICE_WORDS = listOf("voice", "voce", "vocal", "talk", "parla", "speak")

/** Sceglie la scorciatoia che sembra avviare la modalità vocale (per id o etichetta). */
fun pickVoiceShortcut(candidates: List<ShortcutCandidate>): ShortcutCandidate? =
    candidates.firstOrNull { c ->
        listOfNotNull(c.id, c.shortLabel, c.longLabel).any { text ->
            VOICE_WORDS.any { text.contains(it, ignoreCase = true) }
        }
    }
