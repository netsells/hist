package com.netsells.hist.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        logMessage.setOnClickListener {
            Timber.i("This is a test log!")
        }

        logException.setOnClickListener {
            try {
                val array = arrayOf(1, 2, 3)
                array[3]
            } catch (e: ArrayIndexOutOfBoundsException) {
                Timber.e(e)
            }
        }
    }
}