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
import org.mavriksc.mandelbrotmaps.type.Sector
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }

    private val sectors: MutableList<Sector> by lazy {
        val sectorSize = 50
        val list = mutableListOf<Sector>()
        for (x in 0 until bitmap.width step sectorSize) {
            for (y in 0 until bitmap.height step sectorSize) {
                list.add(
                    Sector(
                        x,
                        y,
                        min(sectorSize, bitmap.width - x),
                        min(sectorSize, bitmap.height - y)
                    )
                )
            }
        }
        list
    }

    private val maxLoops = 1000

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

    private fun doIt() {
        hScale = 3.0 / bitmap.width
        vScale = -2.5 / bitmap.height
        val remove = mutableSetOf<Sector>()

        sectors.forEach {
            for (x in 0 until it.width) {
                for (y in 0 until it.height) {
                    val xs = x + it.x
                    val ys = y + it.y
                    if (loop == 0 || bitmap[xs, ys] == Color.BLACK) {
                        //val color = getColorMB(it,x, y)
                       val color = getColorJulia(it, x, y, ImaginaryNumber(0.3543, 0.3543))
                        bitmap[xs, ys] = color

                        if (color != Color.BLACK) {
                            it.unFilled--
                        }
                    }
                }
            }
            if (it.unFilled == 0) remove.add(it)
        }
        remove.forEach { println("removing sector (${it.x},${it.y})") }
        if (sectors.removeAll(remove)) println("Sectors left ${sectors.size}")

        colorView.setImageBitmap(bitmap)
        loop++
    }

    private fun pixelToPoint(x: Int, y: Int): ImaginaryNumber {
        return ImaginaryNumber(x * hScale + hOffset, y * vScale + vOffset)

    }

    private fun getColorMB(sector: Sector, x: Int, y: Int): Int {
        val c = pixelToPoint(x + sector.x, y + sector.y)
        sector.zs[x][y] = sector.zs[x][y] * sector.zs[x][y] + c
        return if (sector.zs[x][y].magnitude() > 2) colors[loop % colors.size] else Color.BLACK
    }

    private fun getColorJulia(sector: Sector, x: Int, y: Int, c: ImaginaryNumber): Int {
        val z = pixelToPoint(x + sector.x, y + sector.y)
        if (loop == 0) {
            sector.zs[x][y] = z * z + c
        } else {
            sector.zs[x][y] = sector.zs[x][y] * sector.zs[x][y] + c
        }
        return if (sector.zs[x][y].magnitude() > 2) colors[loop % colors.size] else Color.BLACK
    }

}
