package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.mavriksc.mandelbrotmaps.type.ImaginaryNumber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


//TODO check value if over 2 color. look at all adjacent black pixels.
// if over 2 color else add to next loop search seeds
// will probably be better to use 2D array
// 2d array of imaginary numbers and count 0 for init
// set of points to explore now (init with corners)
// set of points to start with next round
// if points aren't over threshold add to next round
// since we're not calculating value each time will need to iter upt to count on unknown values


// structure too deep runs out of memory better to do in batches
// done still kinda slow :*(

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val trueForMandelbrotFalseForJulia = true
    private val maxLoops = 1000

    private val hScale by lazy { 2.5 / bitmap.width }
    private val vScale by lazy { -2.5 / bitmap.height }
    private val hOffset = -1.25
    private val vOffset = 1.25

    private val solved: Array<Array<AtomicBoolean>> by lazy {
        Array(bitmap.width) { x ->
            Array(bitmap.height) { y ->
                AtomicBoolean(false)
            }
        }
    }
    private val count = AtomicInteger(0)

    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy {
        val bm = colorView.drawToBitmap()
        (0 until bm.width).forEach { x ->
            (0 until bm.height).forEach { y ->
                bm[x, y] = Color.BLACK
            }
        }
        colorView.setImageBitmap(bm)
        bm
    }

    //  first:x,last:y
    private val firstRound: Set<Pair<Int, Int>> by lazy {
        setOf(
            Pair(0, 0),
            Pair(bitmap.width - 1, 0),
            Pair(0, bitmap.height - 1),
            Pair(bitmap.width - 1, bitmap.height - 1)
        )
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        task = Runnable {
            doItCoroutines()
        }
        mHandler.postDelayed(task, 1500)
    }

    private fun doItCoroutines() {
        firstRound.forEach {
            launch(Dispatchers.Default) {
                solveColorLaunchNeighbors(it.first, it.second)
            }
        }
    }

    private fun solveColorLaunchNeighbors(x: Int, y: Int) {
        solved[x][y].set(true)
        bitmap[x, y] = solveColor(x, y)
        updateUI()
        allNeighbors(x, y)
            .filter {
                it.first >= 0 && it.first < bitmap.width
                        && it.second >= 0 && it.second < bitmap.height
                        && !solved[it.first][it.second].get()
            }.forEach {
                launch(Dispatchers.Default) {
                    solveColorLaunchNeighbors(it.first, it.second)
                }
            }
    }

    private fun updateUI() {
        if (count.incrementAndGet() % bitmap.width == 0)
            launch(Dispatchers.Main.immediate) {
                colorView.setImageBitmap(bitmap)
            }
    }

    private fun solveColor(x: Int, y: Int): Int {
        var loop = 0
        var z = if (trueForMandelbrotFalseForJulia)
            ImaginaryNumber(0.0, 0.0)
        else
            pixelToImaginaryNumber(x, y)
        while (z.magnitude < 2 && loop < maxLoops) {
            z = zIterator(x, y, z)
            loop++
        }
        return if (loop == maxLoops)
            Color.BLACK
        else
            colors[loop % colors.size]
    }

    private fun allNeighbors(x: Int, y: Int): Set<Pair<Int, Int>> =
        setOf(
            Pair(x - 1, y - 1), Pair(x, y - 1), Pair(x + 1, y - 1),
            Pair(x - 1, y), Pair(x + 1, y),
            Pair(x - 1, y + 1), Pair(x, y + 1), Pair(x + 1, y + 1)
        )

    // good julia set values
    // ImaginaryNumber(0.1, 0.6)
    // ImaginaryNumber(0.125, 0.6)
    // ImaginaryNumber(0.3543, 0.3543)
    // Compare ImaginaryNumber(-0.75, 0.0) to  ImaginaryNumber(-0.75, 0.025)
    // ImaginaryNumber(-0.8, 0.156)
    // ImaginaryNumber(-0.7269, 0.1889)
    private fun zIterator(x: Int, y: Int, z: ImaginaryNumber): ImaginaryNumber {
        return z*z + if (trueForMandelbrotFalseForJulia)
            pixelToImaginaryNumber(x, y)
        else ImaginaryNumber(-0.5, 0.5)
    }

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

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false
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
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

    }

}

