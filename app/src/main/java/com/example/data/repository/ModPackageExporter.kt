package com.example.data.repository

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ModPackageExporter {

    fun buildZipBytes(files: Map<String, String>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            files.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    fun buildPlayStoreBundleBytes(
        packageId: String,
        appTitle: String,
        versionName: String
    ): ByteArray {
        val manifestFiles = mapOf(
            "BundleConfig.pb.json" to """
                {
                  "bundletool": { "version": "1.17.1" },
                  "optimizations": {
                    "splitsConfig": { "splitDimension": [{ "value": "ABI" }, { "value": "SCREEN_DENSITY" }] }
                  },
                  "generator": "OmniCore Free Play Store Packager"
                }
            """.trimIndent(),
            "base/manifest/AndroidManifest.xml" to """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="$packageId"
                    android:versionCode="1"
                    android:versionName="$versionName">
                    <application android:label="$appTitle" />
                </manifest>
            """.trimIndent(),
            "PLAY_STORE_CHECKLIST.txt" to """
                OmniCore - Publication Google Play Store (0% commission OmniCore)
                Package ID : $packageId
                Titre      : $appTitle
                Version    : $versionName
                1. Créer une fiche sur Google Play Console (gratuit via OmniCore).
                2. Importer l'archive .aab générée dans Test interne ou Production.
                3. Déclarer les visuels et la politique de confidentialité.
            """.trimIndent()
        )
        return buildZipBytes(manifestFiles)
    }

    fun writeBytesToUri(context: Context, uri: Uri, bytes: ByteArray): Result<Int> {
        return runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: error("Impossible d'ouvrir le flux de destination.")
            bytes.size
        }
    }
}
