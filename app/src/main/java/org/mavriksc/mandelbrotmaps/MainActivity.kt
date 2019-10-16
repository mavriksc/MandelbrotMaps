package org.mavriksc.mandelbrotmaps

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.core.graphics.set
import androidx.core.view.drawToBitmap
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var mHandler = Handler()

    private val colorView: ImageView by lazy { findViewById<ImageView>(R.id.colorView) }

    private val bitmap: Bitmap by lazy { colorView.drawToBitmap() }

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
    }

}
