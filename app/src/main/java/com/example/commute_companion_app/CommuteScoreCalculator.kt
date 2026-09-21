package com.example.commute_companion_app

import com.example.commute_companion_app.api.DashboardResponse

object CommuteScoreCalculator {

    fun calculate(
        dashboard: DashboardResponse
    ): Int {

        var score = 100

        // Traffic condition
        score -= when (
            dashboard.traffic.status.lowercase()
        ) {
            "moderate" -> 10
            "heavy" -> 20
            "severe" -> 30
            else -> 0
        }

        // Traffic delay
        score -= when {
            dashboard.traffic.delayMinutes >= 11 -> 15
            dashboard.traffic.delayMinutes >= 6 -> 10
            dashboard.traffic.delayMinutes >= 1 -> 5
            else -> 0
        }

        // Traffic incidents
        score -= when {
            dashboard.traffic.incidentCount >= 3 -> 10
            dashboard.traffic.incidentCount >= 1 -> 5
            else -> 0
        }

        // Load-shedding stage
        score -= when {
            dashboard.loadShedding.stage >= 5 -> 15
            dashboard.loadShedding.stage >= 3 -> 10
            dashboard.loadShedding.stage >= 1 -> 5
            else -> 0
        }

        // Wet road conditions
        if (dashboard.weather.wetRoads) {
            score -= 10
        }

        return score.coerceIn(0, 100)
    }
}