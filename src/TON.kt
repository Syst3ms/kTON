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
                stack.push(C(a,b))
            }
            else -> throw IllegalArgumentException()
        }
    }
    if (stack.size != 1)
        throw IllegalArgumentException()
    return stack.pop()
}

fun String.toTON() = try { fromPostfix(this) } catch (ex : IllegalArgumentException) { fromPostfix(this.reversed().replace("[(),]".toRegex(),""))}

fun nBuilt(n: Int, a: TONElement, b: TONElement, c: TONElement): Boolean = a < b ||
        a is Omega && (n >= 1 || a < c) ||
        a is C && n >= 1 && nBuilt(n-1, a.a, b, a) && nBuilt(n-1, a.b, b, a) ||
        a is C && a < c && nBuilt(n, a.a, b, c) && nBuilt(n, a.b, b, c)

fun standardCheck(n:Int, e: TONElement): Boolean {
    if (e is Zero || e is Omega) {
        return true
    } else if (e is C) {
        return standardCheck(n, e.a) && standardCheck(n, e.b) &&
                (e.b !is C || e.a <= e.b.a) &&
                nBuilt(n, e.a, e, Zero)
    }
    throw IllegalStateException()
}

fun fs(e: TONElement, n: Int, system: Int): TONElement {
    var standard = false
    var output = e
    val terms: MutableList<TONElement> = mutableListOf(e)
    while (!standard) {
        while (terms[0] is C) {
            terms.add(1,(terms[0] as C).b)
            terms[0] = (terms[0] as C).a
        }
        if (terms[0] is Omega) {
            terms[0] = Zero
        } else if (terms[0] is Zero) {
            if (terms.size == 2) {
                terms.removeAt(0)
                continue
            } else if (terms[1] >= Omega) {
                terms[0] = Omega
            }
            terms[1] = C(terms[1],terms[2])
            terms.removeAt(2)
        }
        output = reconstruct(terms)
        if (standardCheck(system, output)) {
            var currentC = countC(output)
            val neededC = countC(e) + n
            while (currentC < neededC) {
                terms.add(0, Omega)
                currentC++
            }
            output = reconstruct(terms)
        }
        if (standardCheck(system, output)) {
            standard = true
        }
    }
    return output
}

fun countC(e: TONElement): Int = when (e) {
    is Zero, is Omega -> 0
    is C -> countC(e.a) + countC(e.b) + 1
}

fun reconstruct(terms : List<TONElement>) = terms.reduce(::C)

fun main() {
    val expr = "C(C(C(C(Z,Z),0),Z),0)".toTON()
    println(fs(expr, 3, 2).toPrefix())
}