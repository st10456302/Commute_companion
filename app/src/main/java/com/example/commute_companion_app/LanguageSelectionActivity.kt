package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {

    // Tracks which language is currently highlighted, defaults to English
    // since that is the option shown as selected when the screen first opens.
    private var currentlySelectedLanguage: String = "English"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        val optionEnglish = findViewById<LinearLayout>(R.id.optionEnglish)
        val optionZulu = findViewById<LinearLayout>(R.id.optionZulu)
        val optionAfrikaans = findViewById<LinearLayout>(R.id.optionAfrikaans)
        val options = listOf(optionEnglish, optionZulu, optionAfrikaans)
        val optionLanguageNames = listOf("English", "isiZulu", "Afrikaans")

        val radios = listOf(
            findViewById<android.widget.RadioButton>(R.id.radioEnglish),
            findViewById<android.widget.RadioButton>(R.id.radioZulu),
            findViewById<android.widget.RadioButton>(R.id.radioAfrikaans)
        )

        // Restore whatever was previously saved (if this screen is revisited)
        val prefs = AppPreferences(this)
        currentlySelectedLanguage = prefs.selectedLanguage
        val initialIndex = optionLanguageNames.indexOf(currentlySelectedLanguage).let {
            if (it == -1) 0 else it
        }

        fun selectOption(index: Int) {
            options.forEachIndexed { i, layout ->
                layout.setBackgroundResource(if (i == index) R.drawable.bg_card_selected else R.drawable.bg_card_white)
                radios[i].isChecked = i == index
            }
            currentlySelectedLanguage = optionLanguageNames[index]
        }

        // Apply the restored/default selection visually on screen load
        selectOption(initialIndex)

        options.forEachIndexed { i, layout -> layout.setOnClickListener { selectOption(i) } }

        findViewById<android.widget.Button>(R.id.btnContinue).setOnClickListener {
            prefs.selectedLanguage = currentlySelectedLanguage
            Log.d("CommuteCompanion", "selectedLanguage saved = ${prefs.selectedLanguage}")
            startActivity(Intent(this, AccountEntryActivity::class.java))
        }
    }
}