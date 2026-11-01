package com.carlauncherthatsucks.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Integrazione con CoMaps (fork comunitario di Organic Maps).
 * CoMaps accetta gli URI standard `geo:` sia con coordinate sia con ricerca testuale (`q=`).
 */
object CoMaps {
    /** Package noti (F-Droid, Google Play, build generiche/debug). */
    private val KNOWN_PACKAGES = listOf(
        "app.comaps.fdroid",
        "app.comaps.google",
        "app.comaps",
        "app.comaps.debug",
    )

    private const val DOWNLOAD_URL = "https://f-droid.org/packages/app.comaps.fdroid/"

    fun isCoMapsPackage(pkg: String): Boolean = pkg.startsWith("app.comaps")

    /** Restituisce il package di CoMaps installato, se presente. */
    fun installedPackage(context: Context): String? {
        val pm = context.packageManager
        KNOWN_PACKAGES.firstOrNull { isInstalled(pm, it) }?.let { return it }
        // Fallback: qualunque app "app.comaps*" capace di aprire un geo:
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))
        return pm.queryIntentActivities(geo, 0)
            .map { it.activityInfo.packageName }
            .firstOrNull { isCoMapsPackage(it) }
    }

    private fun isInstalled(pm: PackageManager, pkg: String): Boolean = try {
        pm.getPackageInfo(pkg, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun open(context: Context) {
        val pkg = installedPackage(context)
        if (pkg == null) {
            promptInstall(context)
            return
        }
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            promptInstall(context)
        }
    }

    /** Cerca una destinazione (indirizzo, luogo, "benzinaio"...) in CoMaps. */
    fun search(context: Context, query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            open(context)
            return
        }
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(q))
        launchGeo(context, uri)
    }

    /** Apre una coordinata precisa in CoMaps. */
    fun showPoint(context: Context, lat: Double, lon: Double, label: String) {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(" + Uri.encode(label) + ")")
        launchGeo(context, uri)
    }

    private fun launchGeo(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pkg = installedPackage(context)
        if (pkg != null) intent.setPackage(pkg)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            promptInstall(context)
        }
    }

    private fun promptInstall(context: Context) {
        Toast.makeText(context, "CoMaps non è installato", Toast.LENGTH_LONG).show()
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
        }
    }
}
