package io.github.sintrastes.stlk

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

interface Apply<F, A>

object IdOf
data class Id<A>(val underlying: A): Apply<IdOf, A>
@JvmName("fixId")
fun <A> Apply<IdOf, A>.fix(): A {
    return (this as Id<A>).underlying
}

class ConstOf<A>
data class Const<A, X>(val underlying: A): Apply<ConstOf<A>, X>
@JvmName("fixConst")
fun <A, X> Apply<ConstOf<A>, X>.fix(): A {
    return (this as Const<A,X>).underlying
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

    object Serializer: ArithAlg<ConstOf<RawExpr>> {
        override fun <X, Y> func(
            x: String,
            expr: (Apply<ConstOf<RawExpr>, X>) -> Apply<ConstOf<RawExpr>, Y>
        ): Apply<ConstOf<RawExpr>, (X) -> Y> = Const(
            expr(Const(RawExpr.Var(x)))
                .fix()
        )

        override fun <X, Y> Apply<ConstOf<RawExpr>, (X) -> Y>.invoke(x: Apply<ConstOf<RawExpr>, X>): Apply<ConstOf<RawExpr>, Y> = Const(
            RawExpr.App(
                fix(),
                x.fix()
            )
        )

        override fun int(x: Int): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.Const(x)
        )

        override fun Apply<ConstOf<RawExpr>, Int>.minus(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.Const("minus"),
                listOf(fix(), other.fix())
            )
        )

        override fun Apply<ConstOf<RawExpr>, Int>.times(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.Const("times"),
                listOf(fix(), other.fix())
            )
        )

        override fun Apply<ConstOf<RawExpr>, Int>.plus(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.Const("plus"),
                listOf(fix(), other.fix())
            )
        )
    }
}

@Serializable
sealed class RawExpr {
    @Serializable
    data class Var(val label: String): RawExpr()
    @Serializable
    data class App(val f: RawExpr, val x: RawExpr): RawExpr()
    @Serializable
    data class AppOp(val f: RawExpr, val args: List<RawExpr>): RawExpr()
    @Serializable
    data class Const(val value: @Contextual Any): RawExpr()
}

fun <F> ArithAlg<F>.example(): Apply<F, (Int) -> Int> {
    return func("x") { x ->
        x * int(5) + int(6)
    }
}

val test = ArithAlg.Interpreter
    .example()
    .fix()