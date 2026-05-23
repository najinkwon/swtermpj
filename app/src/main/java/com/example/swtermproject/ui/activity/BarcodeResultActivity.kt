package com.example.swtermproject.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swtermproject.R
import com.example.swtermproject.ui.ingredient.ABarcodeResultFragment

class BarcodeResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragment)

        val barcode = intent.getStringExtra(EXTRA_BARCODE).orEmpty()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.singleFragmentContainer,
                    ABarcodeResultFragment.newInstance(barcode)
                )
                .commit()
        }
    }

    companion object {
        const val EXTRA_BARCODE = "extra_barcode"
    }
}
