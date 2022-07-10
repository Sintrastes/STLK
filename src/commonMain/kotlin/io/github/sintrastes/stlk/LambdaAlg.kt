package io.github.sintrastes.stlk

interface Apply<F, A>

object IdOf
data class Id<A>(val underlying: A): Apply<IdOf, A>
fun <A> Apply<IdOf, A>.fix(): A {
    return (this as Id<A>).underlying
}

interface LambdaAlg<F> {
    fun <X, Y> func(x: String, expr: (Apply<F, X>) -> Apply<F, Y>): Apply<F, (X) -> Y>
    operator fun <X,Y> Apply<F, (X) -> Y>.invoke(x: Apply<F, X>): Apply<F, Y>
}

interface ArithAlg<F>: LambdaAlg<F> {
    fun int(x: Int): Apply<F, Int>
    operator fun Apply<F, Int>.plus(other: Apply<F, Int>): Apply<F, Int>
    operator fun Apply<F, Int>.times(other: Apply<F, Int>): Apply<F, Int>
    operator fun Apply<F, Int>.minus(other: Apply<F, Int>): Apply<F, Int>

    object Interpreter: ArithAlg<IdOf> {
        override fun int(x: Int): Apply<IdOf, Int> = Id(x)

        override fun Apply<IdOf, Int>.minus(other: Apply<IdOf, Int>)
                = Id(fix() - other.fix())

        override fun Apply<IdOf, Int>.times(other: Apply<IdOf, Int>)
                = Id(fix() * other.fix())

        override fun Apply<IdOf, Int>.plus(other: Apply<IdOf, Int>)
                = Id(fix() + other.fix())

        override fun <X, Y> Apply<IdOf, (X) -> Y>.invoke(x: Apply<IdOf, X>)
                = Id(fix()(x.fix()))

        override fun <X, Y> func(x: String, expr: (Apply<IdOf, X>) -> Apply<IdOf, Y>): Apply<IdOf, (X) -> Y>
                = Id { expr(Id(it)).fix() }
    }
}

fun <F> ArithAlg<F>.example(): Apply<F, (Int) -> Int> {
    return func("x") { x ->
        x * int(5) + int(6)
    }
}

val test = ArithAlg.Interpreter
    .example()
    .fix()