package io.github.syst3ms.toncalc

fun nBuilt(n: Int, a: TONElement, b: TONElement, c: TONElement): Boolean = a < b ||
        a is Omega && (n >= 1 || a < c) ||
        a is C && n >= 1 && nBuilt(
    n - 1,
    a.a,
    b,
    a
) && nBuilt(n - 1, a.b, b, a) ||
        a is C && a < c && nBuilt(
    n,
    a.a,
    b,
    c
) && nBuilt(n, a.b, b, c)

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
            terms[1] = C(terms[1], terms[2])
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