package com.example.swtermproject.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        playSplashAnimation()

        handler.postDelayed({
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            finish()
        }, SPLASH_DELAY_MS)
    }

    private fun playSplashAnimation() {
        val logoGroup = findViewById<View>(R.id.logoGroup)
        val bottomText = findViewById<View>(R.id.textSplashBottom)

        val blobs = listOf(
            findViewById<View>(R.id.blobBlueTop),
            findViewById<View>(R.id.blobMintTop),
            findViewById<View>(R.id.blobPinkTop),
            findViewById<View>(R.id.blobYellowRight),
            findViewById<View>(R.id.blobOrangeLeft),
            findViewById<View>(R.id.blobBlueBottom),
            findViewById<View>(R.id.blobYellowBottom)
        )

        logoGroup.scaleX = 0.94f
        logoGroup.scaleY = 0.94f
        logoGroup.translationY = 18f

        logoGroup.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(620L)
            .setInterpolator(DecelerateInterpolator())
            .start()

        bottomText.animate()
            .alpha(1f)
            .setStartDelay(260L)
            .setDuration(520L)
            .start()

        blobs.forEachIndexed { index, blob ->
            blob.translationY = 18f + index * 2f
            blob.scaleX = 0.92f
            blob.scaleY = 0.92f

            blob.animate()
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay((index * 45).toLong())
                .setDuration(700L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
