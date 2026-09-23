package com.example.qrcodescannerapp.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.example.qrcodescannerapp.R
import com.example.qrcodescannerapp.databinding.FragmentMyCardBinding

class MyCardFragment : Fragment() {

    private var _binding: FragmentMyCardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFieldLabelsAndHints()
        setupClearButtons()
        setupRealTimeVCardGeneration()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveCard.setOnClickListener {
            generateAndShowQr()
        }
    }

    private fun setupFieldLabelsAndHints() {
        binding.fieldName.tvLabel.text = "Name"
        binding.fieldName.etValue.hint = "Full name"
        binding.fieldName.ivIcon.setImageResource(R.drawable.ic_person)

        binding.fieldPhone.tvLabel.text = "Phone Number"
        binding.fieldPhone.etValue.hint = "+92 3XX XXXXXXX"
        binding.fieldPhone.etValue.inputType = android.text.InputType.TYPE_CLASS_PHONE
        binding.fieldPhone.ivIcon.setImageResource(R.drawable.ic_phone)

        binding.fieldEmail.tvLabel.text = "Email Address"
        binding.fieldEmail.etValue.hint = "you@email.com"
        binding.fieldEmail.etValue.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.fieldEmail.ivIcon.setImageResource(R.drawable.ic_email)

        binding.fieldAddress.tvLabel.text = "Address"
        binding.fieldAddress.etValue.hint = "Street, City"
        binding.fieldAddress.ivIcon.setImageResource(R.drawable.ic_location)

        binding.fieldBirthday.tvLabel.text = "Birthday"
        binding.fieldBirthday.etValue.hint = "DD-MMM-YYYY"
        binding.fieldBirthday.ivIcon.setImageResource(R.drawable.ic_cake)

        binding.fieldOrg.tvLabel.text = "Organization"
        binding.fieldOrg.etValue.hint = "Company name"
        binding.fieldOrg.ivIcon.setImageResource(R.drawable.ic_briefcase)
    }

    private fun setupClearButtons() {
        val fields = listOf(
            binding.fieldName, binding.fieldPhone, binding.fieldEmail,
            binding.fieldAddress, binding.fieldBirthday, binding.fieldOrg
        )
        fields.forEach { field ->
            field.btnClear.setOnClickListener { field.etValue.text?.clear() }
        }
    }

    /**
     * Real-time: har text change par vCard preview QR update hota hai.
     * Ye "guessing" ya fake UI nahi — actual ZXing se live bitmap generate hota hai.
     */
    private fun setupRealTimeVCardGeneration() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateQrPreview()
            }
        }
        binding.fieldName.etValue.addTextChangedListener(watcher)
        binding.fieldPhone.etValue.addTextChangedListener(watcher)
        binding.fieldEmail.etValue.addTextChangedListener(watcher)
        binding.fieldAddress.etValue.addTextChangedListener(watcher)
        binding.fieldBirthday.etValue.addTextChangedListener(watcher)
        binding.fieldOrg.etValue.addTextChangedListener(watcher)
        binding.etNote.addTextChangedListener(watcher)
    }

    private fun buildVCardString(): String {
        val name = binding.fieldName.etValue.text.toString().trim()
        val phone = binding.fieldPhone.etValue.text.toString().trim()
        val email = binding.fieldEmail.etValue.text.toString().trim()
        val address = binding.fieldAddress.etValue.text.toString().trim()
        val org = binding.fieldOrg.etValue.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        return buildString {
            append("BEGIN:VCARD\n")
            append("VERSION:3.0\n")
            if (name.isNotEmpty()) append("FN:$name\n")
            if (phone.isNotEmpty()) append("TEL:$phone\n")
            if (email.isNotEmpty()) append("EMAIL:$email\n")
            if (address.isNotEmpty()) append("ADR:;;$address;;;;\n")
            if (org.isNotEmpty()) append("ORG:$org\n")
            if (note.isNotEmpty()) append("NOTE:$note\n")
            append("END:VCARD")
        }
    }

    private fun updateQrPreview() {
        val vcard = buildVCardString()
        if (vcard.length < 25) {
            // sirf BEGIN/VERSION/END hai, koi actual data nahi — preview mat banao
            binding.imgQrPreview.setImageBitmap(null)
            return
        }
        val bitmap = generateQrBitmap(vcard, 400)
        binding.imgQrPreview.setImageBitmap(bitmap)
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bmp
        } catch (e: Exception) {
            null // empty/invalid content — crash nahi hoga
        }
    }

    private fun generateAndShowQr() {
        val vcard = buildVCardString()
        if (binding.fieldName.etValue.text.isNullOrBlank()) {
            binding.fieldName.etValue.error = "Name required"
            return
        }
        val bitmap = generateQrBitmap(vcard, 600)
        binding.imgQrPreview.setImageBitmap(bitmap)
        // Yahan aap bitmap ko save/share kar sakti hain, ya
        // agar History save karna hai to yahan Room DB call add hogi
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}