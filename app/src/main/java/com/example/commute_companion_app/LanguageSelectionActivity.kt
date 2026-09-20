package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {

    private var currentlySelectedLanguage: String = "English"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        val optionEnglish = findViewById<LinearLayout>(R.id.optionEnglish)
        val optionZulu = findViewById<LinearLayout>(R.id.optionZulu)
        val optionAfrikaans = findViewById<LinearLayout>(R.id.optionAfrikaans)

        val options = listOf(
            optionEnglish,
            optionZulu,
            optionAfrikaans
        )

        val optionLanguageNames = listOf(
            "English",
            "isiZulu",
            "Afrikaans"
        )

        val radios = listOf(
            findViewById<RadioButton>(R.id.radioEnglish),
            findViewById<RadioButton>(R.id.radioZulu),
            findViewById<RadioButton>(R.id.radioAfrikaans)
        )

        val prefs = AppPreferences(this)

        currentlySelectedLanguage = prefs.selectedLanguage

        val initialIndex = optionLanguageNames
            .indexOf(currentlySelectedLanguage)
            .let {
                if (it == -1) 0 else it
            }

        fun selectOption(index: Int) {

            options.forEachIndexed { i, layout ->

                layout.setBackgroundResource(
                    if (i == index) {
                        R.drawable.bg_card_selected
                    } else {
                        R.drawable.bg_card_white
                    }
                )

                radios[i].isChecked = i == index
            }

            currentlySelectedLanguage = optionLanguageNames[index]
        }

        selectOption(initialIndex)

        options.forEachIndexed { i, layout ->

            layout.setOnClickListener {
                selectOption(i)
            }
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {

            // Save the newly selected language.
            prefs.selectedLanguage = currentlySelectedLanguage

            Log.d(
                "CommuteCompanion",
                "selectedLanguage saved = ${prefs.selectedLanguage}"
            )

            /*
             * Check whether this screen was opened from Settings.
             */
            val openedFromSettings =
                intent.getBooleanExtra("opened_from_settings", false)

            if (openedFromSettings) {

                /*
                 * The language has changed.
                 *
                 * Create a completely new ProfileActivity so that
                 * LanguageManager reads the newly selected language
                 * inside attachBaseContext().
                 *
                 * CLEAR_TASK removes the old Profile -> Settings ->
                 * Language Selection stack.
                 */
                val profileIntent =
                    Intent(this, ProfileActivity::class.java)

                profileIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(profileIntent)

                finish()

            } else {

                /*
                 * Normal onboarding flow.
                 *
                 * This remains unchanged from the original
                 * language-selection behaviour.
                 */
                startActivity(
                    Intent(
                        this,
                        AccountEntryActivity::class.java
                    )
                )

                finish()
            }
        }
    }
}