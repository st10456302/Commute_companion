package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OfflineDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_dashboard)

        findViewById<TextView>(
            R.id.btnRetryOffline
        ).setOnClickListener {

            retryConnection()
        }

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    HomeActivity::class.java
                )
            )

            finish()
        }

        findViewById<LinearLayout>(
            R.id.navAlerts
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AlertsActivity::class.java
                )
            )
        }

        findViewById<LinearLayout>(
            R.id.navLocations
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SetLocationActivity::class.java
                )
            )
        }

        findViewById<LinearLayout>(
            R.id.navProfile
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
    }

    private fun retryConnection() {

        if (hasInternetConnection()) {

            Toast.makeText(
                this,
                "Connection restored.",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(
                    this,
                    LiveDashboardActivity::class.java
                )
            )

            finish()

        } else {

            Toast.makeText(
                this,
                "Still offline. Check your internet connection.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hasInternetConnection(): Boolean {

        val connectivityManager =
            getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                network
            ) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }
}