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
// will probably be better to use 2D array
// 2d array of imaginary numbers and count 0 for init
// set of points to explore now (init with corners)
// set of points to start with next round
// if points aren't over threshold add to next round
// since we're not calculating value each time will need to iter upt to count on unknown values


// structure too deep runs out of memory better to do in batches
// done still kinda slow :*(

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

    private val bitmap: Bitmap by lazy {
        val bm = colorView.drawToBitmap()
        (0 until bm.width).forEach { x ->
            (0 until bm.height).forEach { y ->

                bm[x, y] = Color.BLACK
            }
        }
        bm
    }

    //  first:x,last:y
    private val nextRoundSeeds: MutableSet<Pair<Int, Int>> by lazy {
        mutableSetOf(
            Pair(0, 0)//,
//            Pair(bitmap.width - 1, 0),
//            Pair(0, bitmap.height - 1),
//            Pair(bitmap.width - 1, bitmap.height - 1)
        )
    }
    private val colorThisRound = mutableSetOf<Pair<Int, Int>>()

    // first: currentCount, last: currentValue
    private val mbSpace: Array<Array<Pair<Int, ImaginaryNumber>>> by lazy {
        Array(bitmap.width) { x ->
            Array(bitmap.height) { y ->
                if (trueForMandelbrotFalseForJulia)
                    Pair(-1, ImaginaryNumber(0.0, 0.0))
                else
                    Pair(-1, pixelToImaginaryNumber(x, y))
            }
        }
    }

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
            doItTwo()
            if (loop < maxLoops)
                mHandler.post(task)
        }
        mHandler.postDelayed(task, 500)
    }

//    private fun doIt() {
//        val remove = mutableSetOf<Pixel>()
//        pixels.forEach {
//            when {
//                it.value.magnitude > 2 -> {
//                    bitmap[it.x, it.y] = colors[loop % colors.size]
//                    remove.add(it)
//                }
//                else -> {
//                    it.value = zIter(it)
//                    bitmap[it.x, it.y] = Color.BLACK
//                }
//            }
//        }
//        pixels.removeAll(remove)
//        colorView.setImageBitmap(bitmap)
//        loop++
//        println("loop:$loop")
//    }

    //new try
    //maybe set display after each seed is dfs and then each round also
    private fun doItTwo() {
        val currentRound = nextRoundSeeds.toMutableSet()
        nextRoundSeeds.clear()
        currentRound.forEach {
            doingTheThing(it)
        }
        currentRound.clear()
        while (colorThisRound.isNotEmpty()){
            currentRound.addAll(colorThisRound)
            colorThisRound.clear()
            currentRound.forEach {
                doingTheThing(it)
            }
            currentRound.clear()
        }
        colorView.setImageBitmap(bitmap)
        loop++
        println("loop:$loop")
    }

    private fun doingTheThing(it: Pair<Int, Int>) {
        iterToCurrent(it.first, it.second)
        if (mbSpace[it.first][it.second].second.magnitude > 2)
            colorExploreNeighbors(it.first, it.second)
        else
            nextRoundSeeds.add(it)
    }

    private fun colorExploreNeighbors(x: Int, y: Int) {
        bitmap[x, y] = colors[loop % colors.size]
        val neighbors = getUntouchedNeighbors(x, y)
        neighbors.forEach { neighbor ->
            iterToCurrent(neighbor.first, neighbor.second)
            if (mbSpace[neighbor.first][neighbor.second].second.magnitude > 2)
                colorThisRound.add(neighbor)
            else
                nextRoundSeeds.add(Pair(neighbor.first, neighbor.second))
        }
    }

    private fun iterToCurrent(x: Int, y: Int) {
        val start = mbSpace[x][y].first
        if (start >-1) {
            (start + 1..loop).forEach {
                mbSpace[x][y] = Pair(it, zIter(x, y))
            }
        }
        else
            mbSpace[x][y] = Pair(0, mbSpace[x][y].second)
    }

    private fun getUntouchedNeighbors(x: Int, y: Int): Set<Pair<Int, Int>> {
        return allNeighbors(x, y)
            .filter {
                it.first >= 0 && it.first < bitmap.width
                        && it.second >= 0 && it.second < bitmap.height
                        && mbSpace[it.first][it.second].first < loop
            }
            .toSet()
    }

    private fun allNeighbors(x: Int, y: Int): Set<Pair<Int, Int>> =
        setOf(
            Pair(x - 1, y - 1), Pair(x, y - 1), Pair(x + 1, y - 1),
            Pair(x - 1, y), Pair(x + 1, y),
            Pair(x - 1, y + 1), Pair(x, y + 1), Pair(x + 1, y + 1)
        )


    private fun zIter(x: Int, y: Int): ImaginaryNumber {
        return if (trueForMandelbrotFalseForJulia) mandelbrotIter(x, y)
        else juliaIter(x, y, ImaginaryNumber(-0.7269, 0.1889))
        // good julia set values
        // ImaginaryNumber(0.3543, 0.3543)
        // Compare ImaginaryNumber(-0.75, 0.0) to  ImaginaryNumber(-0.75, 0.025)
        // ImaginaryNumber(-0.8, 0.156)
        // ImaginaryNumber(-0.7269, 0.1889)
    }

    private fun mandelbrotIter(x: Int, y: Int) =
        mbSpace[x][y].second * mbSpace[x][y].second + pixelToImaginaryNumber(x, y)

    private fun juliaIter(x: Int, y: Int, c: ImaginaryNumber) =
        mbSpace[x][y].second * mbSpace[x][y].second + c

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
