package com.example.commute_companion_app

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

object BottomNavigationHelper {

    enum class Tab {
        HOME,
        ALERTS,
        LOCATIONS,
        PROFILE
    }

    fun setSelectedTab(
        rootView: View,
        selectedTab: Tab
    ) {
        val tabs = listOf(
            Tab.HOME to R.id.navHome,
            Tab.ALERTS to R.id.navAlerts,
            Tab.LOCATIONS to R.id.navLocations,
            Tab.PROFILE to R.id.navProfile
        )

        tabs.forEach { (tab, containerId) ->

            val navigationItem =
                rootView.findViewById<LinearLayout>(
                    containerId
                )

            val iconContainer =
                when (tab) {
                    Tab.HOME ->
                        rootView.findViewById<FrameLayout>(
                            R.id.navHomeIconContainer
                        )

                    Tab.ALERTS ->
                        rootView.findViewById<FrameLayout>(
                            R.id.navAlertsIconContainer
                        )

                    Tab.LOCATIONS ->
                        rootView.findViewById<FrameLayout>(
                            R.id.navLocationsIconContainer
                        )

                    Tab.PROFILE ->
                        rootView.findViewById<FrameLayout>(
                            R.id.navProfileIconContainer
                        )
                }

            val icon =
                when (tab) {
                    Tab.HOME ->
                        rootView.findViewById<ImageView>(
                            R.id.navHomeIcon
                        )

                    Tab.ALERTS ->
                        rootView.findViewById<ImageView>(
                            R.id.navAlertsIcon
                        )

                    Tab.LOCATIONS ->
                        rootView.findViewById<ImageView>(
                            R.id.navLocationsIcon
                        )

                    Tab.PROFILE ->
                        rootView.findViewById<ImageView>(
                            R.id.navProfileIcon
                        )
                }

            val text =
                when (tab) {
                    Tab.HOME ->
                        rootView.findViewById<TextView>(
                            R.id.navHomeText
                        )

                    Tab.ALERTS ->
                        rootView.findViewById<TextView>(
                            R.id.navAlertsText
                        )

                    Tab.LOCATIONS ->
                        rootView.findViewById<TextView>(
                            R.id.navLocationsText
                        )

                    Tab.PROFILE ->
                        rootView.findViewById<TextView>(
                            R.id.navProfileText
                        )
                }

            if (tab == selectedTab) {

                iconContainer.setBackgroundResource(
                    R.drawable.bg_badge_teal
                )

                icon.setColorFilter(
                    rootView.context.getColor(
                        R.color.teal_dark
                    )
                )

                text.setTextColor(
                    rootView.context.getColor(
                        R.color.teal_dark
                    )
                )

            } else {

                iconContainer.background = null

                icon.setColorFilter(
                    rootView.context.getColor(
                        R.color.nav_unselected
                    )
                )

                text.setTextColor(
                    rootView.context.getColor(
                        R.color.nav_unselected
                    )
                )
            }

            navigationItem.isSelected =
                tab == selectedTab
        }
    }
}