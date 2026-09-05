package com.mukhtari.app.domain.usecase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGeneratorUseCase(private val context: Context) {
    fun generateResidencyCertificate(
        snapshotName: String,
        snapshotFamily: String,
        snapshotAddress: String,
        transactionCode: String,
        issuedAt: Long
    ): String {
        val pdfDocument = PdfDocument()

        // A4 size: 595 x 842 points at 72 dpi
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT // Arabic is RTL
        }

        // Header
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("تأييد سكن", 400f, 100f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = sdf.format(Date(issuedAt))
        canvas.drawText("التاريخ: $dateString", 550f, 150f, paint)
        canvas.drawText("رقم المعاملة: $transactionCode", 550f, 180f, paint)

        // Body
        paint.textSize = 18f
        canvas.drawText("إلى من يهمه الأمر،", 550f, 250f, paint)

        canvas.drawText("نؤيد لكم بأن المواطن (ة) المذكور تفاصيله أدناه يسكن في محلتنا:", 550f, 300f, paint)

        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("الاسم: $snapshotName", 500f, 350f, paint)
        canvas.drawText("العائلة: $snapshotFamily", 500f, 400f, paint)
        canvas.drawText("العنوان: $snapshotAddress", 500f, 450f, paint)

        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("وقد أعطي هذا التأييد بناءً على طلبه.", 550f, 550f, paint)

        // Footer (Signature)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("توقيع المختار", 200f, 650f, paint)
        canvas.drawText("الختم", 200f, 700f, paint)

        pdfDocument.finishPage(page)

        // Save to file
        val outputDir = File(context.getExternalFilesDir(null), "Certificates")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val file = File(outputDir, "certificate_$transactionCode.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return file.absolutePath
    }
}
