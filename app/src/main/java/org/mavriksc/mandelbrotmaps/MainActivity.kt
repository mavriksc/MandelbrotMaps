package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import org.mavriksc.mandelbrotmaps.type.ImaginaryNumber



//TODO check value if over 2 color. look at all adjacent black pixels.
// if over 2 color else add to next loop search seeds

class MainActivity : AppCompatActivity() {

    private val trueForMandelbrotFalseForJulia = false
    private val maxLoops = 1000
    private var loop = 0

    private val hScale by lazy { 3.0 / bitmap.width }
    private val vScale by lazy { -2.5 / bitmap.height }
    private val hOffset = -2.25
    private val vOffset = 1.25

    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }


    private val pixels: MutableList<Pixel> by lazy {
        if (trueForMandelbrotFalseForJulia)
            MutableList<Pixel>(bitmap.width * bitmap.height) {
                Pixel(
                    it % bitmap.width, it / bitmap.width,
                    ImaginaryNumber(0.0, 0.0)
                )
            }
        else
            MutableList<Pixel>(bitmap.width * bitmap.height) {
                Pixel(
                    it % bitmap.width, it / bitmap.width,
                    pixelToImaginaryNumber(it % bitmap.width, it / bitmap.width)
                )
            }
    }

    private val colors: List<Int> = listOf(
        Color.LTGRAY,
        Color.WHITE,
        Color.CYAN,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.RED,
        Color.MAGENTA
    )

    private lateinit var task: Runnable
    private val mHideHandler = Handler()

    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        task = Runnable {
            doIt()
            if (loop < maxLoops || pixels.isNotEmpty())
                mHandler.post(task)
        }
        mHandler.postDelayed(task, 500)
    }

    private fun doIt() {
        val remove = mutableSetOf<Pixel>()
        pixels.forEach {
            when {
                it.value.magnitude > 2 -> {
                    bitmap[it.x, it.y] = colors[loop % colors.size]
                    remove.add(it)
                }
                else -> {
                    it.value = zIter(it)
                    bitmap[it.x, it.y] = Color.BLACK
                }
            }
        }
        pixels.removeAll(remove)
        colorView.setImageBitmap(bitmap)
        loop++
        println("loop:$loop")
    }

    private fun zIter(pixel: Pixel): ImaginaryNumber {
        return if (trueForMandelbrotFalseForJulia) mandelbrotIter(pixel)
        else juliaIter(pixel, ImaginaryNumber(-0.7269, 0.1889))
        // good julia set values
        // ImaginaryNumber(0.3543, 0.3543)
        // Compare ImaginaryNumber(-0.75, 0.0) to  ImaginaryNumber(-0.75, 0.025)
        // ImaginaryNumber(-0.8, 0.156)
        // ImaginaryNumber(-0.7269, 0.1889)
    }

    private fun mandelbrotIter(pixel: Pixel) =
        pixel.value * pixel.value + pixelToImaginaryNumber(pixel.x, pixel.y)

    private fun juliaIter(pixel: Pixel, c: ImaginaryNumber) = pixel.value * pixel.value + c

    //screen pixel to point in imaginary plane
    private fun pixelToImaginaryNumber(x: Int, y: Int) =
        ImaginaryNumber(x * hScale + hOffset, y * vScale + vOffset)


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

    }

    private fun show() {
        // Show the system bar
        mVisible = true

    }


    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

}

data class Pixel(val x: Int, val y: Int, var value: ImaginaryNumber)
