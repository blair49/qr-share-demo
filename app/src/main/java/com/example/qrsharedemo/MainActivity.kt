package com.example.qrsharedemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.EnumMap

class MainActivity : AppCompatActivity() {

    private lateinit var generateButton: Button
    private lateinit var scanButton: Button
    private lateinit var qrCodeImageView: ImageView
    private lateinit var cardList: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private var scannedCards: MutableList<String> = mutableListOf()

    private val qrCodeWidthPixels = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generateButton = findViewById(R.id.generateButton)
        scanButton = findViewById(R.id.scanButton)
        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        cardList = findViewById(R.id.cardList)

        cardAdapter = CardAdapter(scannedCards)
        cardList.layoutManager = LinearLayoutManager(this)
        cardList.adapter = cardAdapter

        generateButton.setOnClickListener {
            val inputData = "John Doe\nCEO\nAcme Corporation\njohndoe@example.com" // Business card data

            val qrCodeBitmap = generateQRCode(inputData)
            qrCodeImageView.setImageBitmap(qrCodeBitmap)
            scannedCards.add(inputData)
            cardAdapter.notifyDataSetChanged()
        }

        scanButton.setOnClickListener {
            startQRCodeScanner()
        }
    }

    private fun generateQRCode(data: String): Bitmap? {
        val bitMatrix: BitMatrix = try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                qrCodeWidthPixels,
                qrCodeWidthPixels,
                hints
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val qrCodeWidth = bitMatrix.width
        val qrCodeHeight = bitMatrix.height
        val pixels = IntArray(qrCodeWidth * qrCodeHeight)

        for (y in 0 until qrCodeHeight) {
            val offset = y * qrCodeWidth
            for (x in 0 until qrCodeWidth) {
                pixels[offset + x] = if (bitMatrix[x, y]) {
                    resources.getColor(R.color.secondary, theme) // QR code color (black)
                } else {
                    resources.getColor(R.color.primary, theme) // Background color (white)
                }
            }
        }

        val bitmap = Bitmap.createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, qrCodeWidth, 0, 0, qrCodeWidth, qrCodeHeight)

        // Customize the QR code bitmap (e.g., add a logo)
        val logoBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val scaledLogoBitmap =
            Bitmap.createScaledBitmap(logoBitmap, qrCodeWidth / 4, qrCodeHeight / 4, false)

        return combineBitmaps(bitmap, scaledLogoBitmap)
    }

    private fun combineBitmaps(backgroundBitmap: Bitmap, overlayBitmap: Bitmap): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(backgroundBitmap.width, backgroundBitmap.height, backgroundBitmap.config)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
        val left = (backgroundBitmap.width - overlayBitmap.width) / 2
        val top = (backgroundBitmap.height - overlayBitmap.height) / 2
        canvas.drawBitmap(overlayBitmap, left.toFloat(), top.toFloat(), null)
        return combinedBitmap
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
                scannedCards.add(result.contents)
                cardAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}