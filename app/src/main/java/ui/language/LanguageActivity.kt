package ui.language

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrcodescannerapp.MainActivity
import com.example.qrcodescannerapp.R

class LanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val rvLanguages = findViewById<RecyclerView>(R.id.rvLanguages)
        val btnDone = findViewById<ImageView>(R.id.btnDone)

        // Figma ke mutabiq languages ki list
        val languages = listOf(
            LanguageModel("English"),
            LanguageModel("عربى"),
            LanguageModel("Hindi"),
            LanguageModel("Bengali"),
            LanguageModel("Japanese"),
            LanguageModel("Chinese")
        )

        rvLanguages.layoutManager = LinearLayoutManager(this)
        rvLanguages.adapter = LanguageAdapter(languages)

        btnDone.setOnClickListener {
            // Language select hone ke baad MainActivity (Scanner) par bhejein ga
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}