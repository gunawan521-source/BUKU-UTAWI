package id.rumusutawi.bukudigital

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max

class MainActivity : Activity() {
    private val brown = Color.rgb(91, 53, 36)
    private val darkBrown = Color.rgb(63, 39, 27)
    private val cream = Color.rgb(250, 242, 224)
    private val paper = Color.rgb(255, 252, 245)
    private lateinit var renderer: PdfRenderer
    private lateinit var descriptor: ParcelFileDescriptor
    private lateinit var pageImage: ImageView
    private lateinit var pageCounter: TextView
    private lateinit var pageSpinner: Spinner
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var currentPage = 0
    private var downX = 0f
    private var downY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = darkBrown
        window.navigationBarColor = darkBrown
        window.decorView.systemUiVisibility = 0

        try {
            val pdfFile = File(cacheDir, "Salinan Rumus Utawi.pdf")
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                assets.open("Salinan Rumus Utawi.pdf").use { input ->
                    FileOutputStream(pdfFile).use { output -> input.copyTo(output) }
                }
            }
            descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor)
            buildInterface()
            showPage(0, false)
        } catch (e: Exception) {
            val message = TextView(this).apply {
                text = "Buku tidak dapat dibuka.\n${e.localizedMessage ?: "Terjadi kesalahan."}"
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(darkBrown)
                setPadding(24, 24, 24, 24)
            }
            setContentView(message)
        }
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
            setPadding(dp(14), dp(12), dp(14), dp(10))
        }

        val title = TextView(this).apply {
            text = "📖  BUKU DIGITAL\nRUMUS UTAWI"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(brown)
            typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
            setPadding(dp(6), dp(6), dp(6), dp(4))
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val subtitle = TextView(this).apply {
            text = "Kitab klasik • Geser untuk membalik halaman"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(117, 91, 68))
            setPadding(0, 0, 0, dp(10))
        }
        root.addView(subtitle)

        val bookFrame = FrameLayout(this).apply {
            setBackgroundColor(brown)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            elevation = dp(7).toFloat()
        }
        pageImage = ImageView(this).apply {
            setBackgroundColor(paper)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = false
            contentDescription = "Halaman buku Rumus Utawi"
            setPadding(dp(3), dp(3), dp(3), dp(3))
        }
        bookFrame.addView(pageImage, FrameLayout.LayoutParams(-1, -1))
        val bookParams = LinearLayout.LayoutParams(-1, 0, 1f).apply {
            setMargins(dp(2), dp(4), dp(2), dp(12))
        }
        root.addView(bookFrame, bookParams)

        pageCounter = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(darkBrown)
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(pageCounter)

        pageSpinner = Spinner(this).apply {
            val labels = (1..renderer.pageCount).map { "Buka halaman $it" }
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, labels)
            setSelection(0)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position != currentPage) showPage(position, true)
                }
            }
        }
        root.addView(pageSpinner, LinearLayout.LayoutParams(-1, dp(46)))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(2))
        }
        prevButton = makeButton("‹ Sebelumnya") { showPage(currentPage - 1, true) }
        nextButton = makeButton("Berikutnya ›") { showPage(currentPage + 1, true) }
        nav.addView(prevButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(0, 0, dp(6), 0) })
        nav.addView(nextButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(6), 0, 0, 0) })
        root.addView(nav)

        pageImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y; true }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (abs(dx) > dp(45) && abs(dx) > abs(dy)) {
                        if (dx < 0) showPage(currentPage + 1, true) else showPage(currentPage - 1, true)
                        true
                    } else false
                }
                else -> true
            }
        }
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(brown)
        setOnClickListener { action() }
    }

    private fun showPage(index: Int, animate: Boolean) {
        if (!::renderer.isInitialized) return
        if (index !in 0 until renderer.pageCount) {
            Toast.makeText(this, if (index < 0) "Ini halaman pertama" else "Ini halaman terakhir", Toast.LENGTH_SHORT).show()
            return
        }
        val old = currentPage
        currentPage = index
        try {
            val pdfPage = renderer.openPage(index)
            val screenWidth = resources.displayMetrics.widthPixels - dp(60)
            val screenHeight = max(dp(300), (resources.displayMetrics.heightPixels * 0.54f).toInt())
            val scale = minOf(screenWidth.toFloat() / pdfPage.width, screenHeight.toFloat() / pdfPage.height, 2.0f)
            val bitmap = Bitmap.createBitmap(
                max(1, (pdfPage.width * scale).toInt()),
                max(1, (pdfPage.height * scale).toInt()),
                Bitmap.Config.ARGB_8888
            )
            bitmap.eraseColor(Color.WHITE)
            pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()
            pageImage.setImageBitmap(bitmap)
            if (animate && old != index) {
                pageImage.rotationY = if (index > old) 70f else -70f
                pageImage.alpha = 0.65f
                pageImage.animate().rotationY(0f).alpha(1f).setDuration(280)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
            pageCounter.text = "Halaman ${index + 1} dari ${renderer.pageCount}"
            if (pageSpinner.selectedItemPosition != index) pageSpinner.setSelection(index, false)
            prevButton.isEnabled = index > 0
            nextButton.isEnabled = index < renderer.pageCount - 1
            prevButton.alpha = if (index > 0) 1f else 0.45f
            nextButton.alpha = if (index < renderer.pageCount - 1) 1f else 0.45f
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka halaman: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        if (::renderer.isInitialized) renderer.close()
        if (::descriptor.isInitialized) descriptor.close()
        super.onDestroy()
    }
}
