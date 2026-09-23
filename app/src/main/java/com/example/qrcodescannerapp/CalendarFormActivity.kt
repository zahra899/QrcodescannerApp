package com.example.qrcodescannerapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFormActivity : AppCompatActivity() {

    private var startDateTime: Calendar? = null
    private var endDateTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackCalendar)
        val etTitle = findViewById<EditText>(R.id.etEventTitle)
        val etLocation = findViewById<EditText>(R.id.etEventLocation)
        val etStart = findViewById<EditText>(R.id.etEventStart)
        val etEnd = findViewById<EditText>(R.id.etEventEnd)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateCalendar)

        btnBack.setOnClickListener { finish() }

        etStart.setOnClickListener { showDatePicker { cal -> startDateTime = cal; etStart.setText(formatDate(cal)) } }
        etEnd.setOnClickListener { showDatePicker { cal -> endDateTime = cal; etEnd.setText(formatDate(cal)) } }

        btnGenerate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Event title likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startDateTime == null || endDateTime == null) {
                Toast.makeText(this, "Start aur End date select karein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fmt = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            val location = etLocation.text.toString().trim()

            // Standard VEVENT (iCalendar) format
            val vEvent = buildString {
                append("BEGIN:VEVENT\n")
                append("SUMMARY:$title\n")
                if (location.isNotEmpty()) append("LOCATION:$location\n")
                append("DTSTART:${fmt.format(startDateTime!!.time)}\n")
                append("DTEND:${fmt.format(endDateTime!!.time)}\n")
                append("END:VEVENT")
            }

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", vEvent)
            intent.putExtra("QR_TYPE", "Calender")
            startActivity(intent)
        }
    }

    private fun showDatePicker(onPicked: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day)
            onPicked(cal)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(cal: Calendar): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
    }
}