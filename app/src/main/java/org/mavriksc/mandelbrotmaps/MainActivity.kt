package org.mavriksc.mandelbrotmaps

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

    private lateinit var task: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        task = Runnable {
            val editText = findViewById<ImageView>(R.id.colorView)
            val img = editText.drawToBitmap()
            val x = Random.nextInt(img.width - 20)
            val y = Random.nextInt(img.height - 20)

            for (i in 0..20)
                for (j in 0..20) {
                    img.set(
                        x = i + x,
                        y = j + y,
                        color = Color.CYAN
                    )
                }
            editText.setImageBitmap(img)
            mHandler.post(task)
        }
        mHandler.postDelayed(task, 5000)
    }
}
