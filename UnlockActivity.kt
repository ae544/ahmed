package com.gymlock.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Invisible activity: lets the person choose camera or gallery, and once a
 * photo is confirmed it saves it, bumps the streak, and stops
 * LockOverlayService so the phone unlocks. Backing out without a photo
 * leaves the overlay up.
 */
class UnlockActivity : AppCompatActivity() {

    private var cameraUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            onPhotoConfirmed(cameraUri!!)
        } else {
            notEnoughAndFinish()
        }
    }

    private val pickFromGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoConfirmed(uri)
        } else {
            notEnoughAndFinish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSourceChooser()
    }

    private fun showSourceChooser() {
        AlertDialog.Builder(this)
            .setTitle("اثبات إنك في الجيم")
            .setMessage("عايز تصور دلوقتي ولا ترفع صورة من المعرض؟")
            .setPositiveButton("📷 الكاميرا") { _, _ -> launchCamera() }
            .setNegativeButton("🖼️ المعرض") { _, _ -> launchGallery() }
            .setOnCancelListener { notEnoughAndFinish() }
            .setCancelable(true)
            .show()
    }

    private fun launchCamera() {
        val photosDir = File(filesDir, "proof_photos").apply { mkdirs() }
        val fileName = "gym_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val file = File(photosDir, fileName)
        cameraUri = FileProvider.getUriForFile(this, "com.gymlock.app.fileprovider", file)
        takePicture.launch(cameraUri)
    }

    private fun launchGallery() {
        pickFromGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun onPhotoConfirmed(uri: Uri) {
        Prefs.saveLastPhoto(this, uri.toString())
        val streak = Prefs.incrementStreak(this)
        Toast.makeText(this, "تمام! سلسلة الأيام: $streak 🔥", Toast.LENGTH_LONG).show()
        LockOverlayService.stop(this)
        finish()
    }

    private fun notEnoughAndFinish() {
        Toast.makeText(this, "لازم صورة عشان الموبايل يتفتح", Toast.LENGTH_SHORT).show()
        finish()
    }
}
