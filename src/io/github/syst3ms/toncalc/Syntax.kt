package io.github.syst3ms.toncalc

import java.lang.IllegalArgumentException
import java.util.*

sealed class TONElement {
    operator fun compareTo(o: TONElement) = lex(this.toString()).compareTo(lex(o.toString()))

    private fun lex(s: String) = s.replace('0','1').replace('Ω','2').replace('C','0')

    abstract override fun toString(): String

    abstract fun toPrefix(): String
}

object Zero : TONElement() {
    override fun toString() = "0"

    override fun toPrefix() = "0"
}

object Omega : TONElement() {
    override fun toString() = "Ω"

    override fun toPrefix() = "Ω"
}

class C(val a: TONElement, val b : TONElement) : TONElement() {
    override fun toString() = "$b${a}C"

    override fun toPrefix() = "C(${a.toPrefix()},${b.toPrefix()})"
}

fun fromPostfix(s: String): TONElement {
    val stack = Stack<TONElement>()
    for (c in s) {
        when (c) {
            '0' -> stack.push(Zero)
            'W', 'Z', 'X', 'Ω' -> stack.push(Omega)
            'C' -> {
                if (stack.size < 2)
                    throw IllegalArgumentException()
                val a = stack.pop()
                val b = stack.pop()
                stack.push(C(a, b))
            }
            else -> throw IllegalArgumentException()
        }
    }
    if (stack.size != 1)
        throw IllegalArgumentException()
    return stack.pop()
}

fun String.toTON() = try {
    fromPostfix(this)
} catch (ex : IllegalArgumentException) {
    fromPostfix(this.reversed().replace("[(),]".toRegex(), ""))
}