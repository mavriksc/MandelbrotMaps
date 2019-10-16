package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import org.mavriksc.mandelbrotmaps.type.ImaginaryNumber

class MainActivity : AppCompatActivity() {
    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }

    private val maxLoops = 100

    private var loop = 1

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

    private var hScale = 0.0
    private var vScale = 0.0
    private var hOffset = -2
    private var vOffset = 1

    private lateinit var task: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        task = Runnable {
            doIt()
            if (loop < maxLoops)
                mHandler.post(task)
        }
        mHandler.postDelayed(task, 500)
    }

    private fun doIt() {
        hScale = 3.0 / bitmap.width
        vScale = -2.0 / bitmap.height

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (loop==1 || bitmap[x, y] == Color.BLACK){
                    val point = pixelToPoint(x, y)
                    val count = getCount(point)
                    val color = if (count == loop) Color.BLACK else colors[count % colors.size]
                    bitmap[x, y] = color
                }
            }
        }
        colorView.setImageBitmap(bitmap)
        loop++
    }

    private fun pixelToPoint(x: Int, y: Int): ImaginaryNumber {
        return ImaginaryNumber(x * hScale + hOffset, y * vScale + vOffset)

    }

    private fun getCount(coord: ImaginaryNumber): Int {
        var z = ImaginaryNumber(0.0, 0.0)
        var count = -1
        while (z.magnitude() < 2 && count < loop) {
            z = z * z + coord
            count++
        }
        return count
    }

}
