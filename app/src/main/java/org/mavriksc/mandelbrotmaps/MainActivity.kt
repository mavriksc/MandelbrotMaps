package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import org.mavriksc.mandelbrotmaps.type.ImaginaryNumber
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }

    private val MAX_ITER = 20

    private val colorMap: Map<Int, Int> = mapOf(1 to Color.WHITE,2 to Color.CYAN,3 to Color.BLUE, 4 to Color.GREEN, 5 to Color.YELLOW)

    private val initialRealRange = -2..1
    private val initialImaginaryRange = -1..1

    private lateinit var task: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        task = Runnable {
            val x = Random.nextInt(bitmap.width - 20)
            val y = Random.nextInt(bitmap.height - 20)

            for (i in 0..20)
                for (j in 0..20) {
                    bitmap.set(
                        x = i + x,
                        y = j + y,
                        color = Color.CYAN
                    )
                }
            colorView.setImageBitmap(bitmap)
            mHandler.post(task)
        }
        mHandler.postDelayed(task, 500)
        testImaginaryNums()
    }

    private fun testImaginaryNums() {
        val x = ImaginaryNumber(1.0, 1.0)
        val y = ImaginaryNumber(2.0, 2.0)
        println("x=$x")
        println("y=$y")
        println("||X|| = " + x.magnitude())
        println("||Y|| = " + y.magnitude())
        println(x + y)
        println(y * y)
        println(getCount(x))
        println(getCount(y))

    }

    private fun getCount(coord: ImaginaryNumber): Int {
        var z = ImaginaryNumber(0.0, 0.0)
        var count = 0
        while (z.magnitude() < 2 && count < MAX_ITER) {
            z = z * z + coord
            count++
        }
        return count
    }

}
