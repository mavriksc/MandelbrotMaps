package org.mavriksc.mandelbrotmaps.type

class Sector(val x: Int,val y: Int,val width: Int,val height: Int) {

    val zs: Array<Array<ImaginaryNumber>> by lazy {
        Array(width) {
            Array(height) {
                ImaginaryNumber(
                    0.0,
                    0.0
                )
            }
        }
    }
    var unFilled = width*height
    //var allBlack = true

}