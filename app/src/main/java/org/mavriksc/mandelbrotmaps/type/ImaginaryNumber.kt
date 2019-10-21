package org.mavriksc.mandelbrotmaps.type

import kotlin.math.sqrt

class ImaginaryNumber(val real: Double = 0.0, val imaginary: Double = 0.0) {

    val magnitude by lazy{ sqrt(real * real + imaginary * imaginary)}

    operator fun plus(other: ImaginaryNumber): ImaginaryNumber =
        ImaginaryNumber(real + other.real, imaginary + other.imaginary)

    operator fun times(other: ImaginaryNumber): ImaginaryNumber {
        val newReal = real * other.real - imaginary * other.imaginary
        val newImaginary = real * other.imaginary + imaginary * other.real
        return ImaginaryNumber(newReal, newImaginary)
    }

    override fun toString(): String {
        val join = if (imaginary < 0) "-" else "+"
        return "$real $join $imaginary" + "i"
    }

}