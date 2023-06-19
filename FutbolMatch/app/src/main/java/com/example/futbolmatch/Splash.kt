package com.example.futbolmatch

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Splash : AppCompatActivity() {
    private var timer: Timer? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)

        val isConnected = isNetworkConnected()
        if (isConnected) {
            startMainActivityAfterDelay()
        } else {
            showLoadingView()
            checkConnectivityPeriodically()
        }

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    // Comprobar la conexion
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun startMainActivityAfterDelay() {
        val timerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val intent = Intent(this@Splash, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        timer = Timer()
        timer?.schedule(timerTask, 5000)
    }

    // Comprobar la conexion hasta que tenga red
    private fun checkConnectivityPeriodically() {
        val timerTask = object : TimerTask() {
            override fun run() {
                val isConnected = isNetworkConnected()
                if (isConnected) {
                    timer?.cancel()
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        startMainActivityAfterDelay()
                    }
                }
            }
        }

        timer = Timer()
        timer?.schedule(timerTask, 1000, 1000)
    }

    private fun showLoadingView() {
        progressBar.visibility = View.VISIBLE
    }
}