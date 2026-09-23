package com.example.qrcodescannerapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import android.content.ContentUris
import android.database.Cursor
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts

class MyCardFormActivity : AppCompatActivity() {
    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            val cursor: Cursor? = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    val name = if (nameIndex >= 0) it.getString(nameIndex) else ""
                    val contactId = if (idIndex >= 0) it.getString(idIndex) else null
                    val hasPhone = if (hasPhoneIndex >= 0) it.getInt(hasPhoneIndex) else 0

                    findViewById<EditText>(R.id.etCardName).setText(name)

                    if (hasPhone > 0 && contactId != null) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex >= 0) {
                                    findViewById<EditText>(R.id.etCardPhone).setText(pc.getString(numberIndex))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val requestContactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(this, "Contacts permission zaroori hai", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_card_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackMyCard)
        val etName = findViewById<EditText>(R.id.etCardName)
        val etPhone = findViewById<EditText>(R.id.etCardPhone)
        val etEmail = findViewById<EditText>(R.id.etCardEmail)
        val etAddress = findViewById<EditText>(R.id.etCardAddress)
        val etBirthday = findViewById<EditText>(R.id.etCardBirthday)
        val etOrg = findViewById<EditText>(R.id.etCardOrg)
        val etNote = findViewById<EditText>(R.id.etCardNote)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateMyCard)

        btnBack.setOnClickListener { finish() }

        // Clear (X) buttons
        findViewById<ImageView>(R.id.btnClearName).setOnClickListener { etName.text.clear() }
        findViewById<ImageView>(R.id.btnClearEmail).setOnClickListener { etEmail.text.clear() }
        findViewById<ImageView>(R.id.btnClearAddress).setOnClickListener { etAddress.text.clear() }
        findViewById<ImageView>(R.id.btnClearBirthday).setOnClickListener { etBirthday.text.clear() }
        findViewById<ImageView>(R.id.btnClearOrg).setOnClickListener { etOrg.text.clear() }
        findViewById<ImageView>(R.id.btnClearNote).setOnClickListener { etNote.text.clear() }

        // Birthday date picker
        etBirthday.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etBirthday.setText(String.format("%02d-%s-%d", day, monthName(month), year))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        findViewById<ImageView>(R.id.btnPickContact).setOnClickListener {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                pickContactLauncher.launch(null)
            } else {
                requestContactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
            }
        }

        btnGenerate.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Naam likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                Toast.makeText(this, "Phone number likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val vCard = buildString {
                append("BEGIN:VCARD\n")
                append("VERSION:3.0\n")
                append("FN:$name\n")
                append("TEL:$phone\n")
                if (etEmail.text.isNotEmpty()) append("EMAIL:${etEmail.text}\n")
                if (etAddress.text.isNotEmpty()) append("ADR:${etAddress.text}\n")
                if (etBirthday.text.isNotEmpty()) append("BDAY:${etBirthday.text}\n")
                if (etOrg.text.isNotEmpty()) append("ORG:${etOrg.text}\n")
                if (etNote.text.isNotEmpty()) append("NOTE:${etNote.text}\n")
                append("END:VCARD")
            }

            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", vCard)
            intent.putExtra("QR_TYPE", "My Card")
            startActivity(intent)
        }
    }


    private fun monthName(month: Int): String {
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return months[month]
    }
}