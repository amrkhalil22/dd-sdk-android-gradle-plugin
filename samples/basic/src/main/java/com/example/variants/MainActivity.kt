package com.example.variants

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lib.Placeholder // ktlint-disable no-unused-imports  unused import is on purpose

class MainActivity : AppCompatActivity() {

    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toaster = Toaster(this)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        toaster.toast("Hello world !")
    }
}
