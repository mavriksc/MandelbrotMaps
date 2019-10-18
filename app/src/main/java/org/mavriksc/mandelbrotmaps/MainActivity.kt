package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import org.mavriksc.mandelbrotmaps.type.ImaginaryNumber

class MainActivity : AppCompatActivity() {
    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }

    private val zs: Array<Array<ImaginaryNumber>> by lazy {
        Array(bitmap.width) {
            Array(bitmap.height) {
                ImaginaryNumber(
                    0.0,
                    0.0
                )
            }
        }
    }


    private val maxLoops = 500

    private var loop = 0

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
    private var hOffset = -2.25
    private var vOffset = 1.25

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

    @Deprecated("old")
    private fun doItOld() {
        hScale = 3.0 / bitmap.width
        vScale = -2.5 / bitmap.height

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (loop == 1 || bitmap[x, y] == Color.BLACK) {
                    val point = pixelToPoint(x, y)
                    //val count = getCountJ(point,ImaginaryNumber(0.3543,0.3543))
                    val count = getCount(point)
                    val color = if (count == loop) Color.BLACK else colors[count % colors.size]
                    bitmap[x, y] = color
                }
            }
        }
        colorView.setImageBitmap(bitmap)
        loop++
    }

    private fun doIt() {
        hScale = 3.0 / bitmap.width
        vScale = -2.5 / bitmap.height

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (loop == 0 || bitmap[x, y] == Color.BLACK) {
                    bitmap[x, y] = getColorMB(x, y)
                    //bitmap[x, y] = getColorJulia(x, y, ImaginaryNumber(0.3543,0.3543))
                }
            }
        }

        colorView.setImageBitmap(bitmap)
        loop++
    }

    private fun pixelToPoint(x: Int, y: Int): ImaginaryNumber {
        return ImaginaryNumber(x * hScale + hOffset, y * vScale + vOffset)

    }

    private fun getColorMB(x: Int, y: Int): Int {
        val c = pixelToPoint(x, y)
        zs[x][y] = zs[x][y] * zs[x][y] + c
        return if (zs[x][y].magnitude() > 2) colors[loop % colors.size] else Color.BLACK
    }

    private fun getColorJulia(x: Int, y: Int, c: ImaginaryNumber): Int {
        val z = pixelToPoint(x, y)
        if (loop == 0) {
            zs[x][y] = z * z + c
        } else {
            zs[x][y] = zs[x][y] * zs[x][y] + c
        }
        return if (zs[x][y].magnitude() > 2) colors[loop % colors.size] else Color.BLACK
    }

    private fun getCount(c: ImaginaryNumber): Int {
        var z = ImaginaryNumber(0.0, 0.0)
        var count = -1
        while (z.magnitude() < 2 && count < loop) {
            z = z * z + c
            count++
        }
        return count
    }

    private fun getCountJ(zStart: ImaginaryNumber, c: ImaginaryNumber): Int {
        var z = ImaginaryNumber(zStart.real, zStart.imaginary)
        var count = 0
        while (z.magnitude() < 2 && count < loop) {
            z = z * z + c
            count++
        }
        return count
    }

}
