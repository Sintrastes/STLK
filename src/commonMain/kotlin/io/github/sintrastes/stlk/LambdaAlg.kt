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

    object Interpreter: LambdaAlg<IdOf> {
        override fun <X, Y> Apply<IdOf, (X) -> Y>.invoke(x: Apply<IdOf, X>)
                = Id(fix()(x.fix()))

        override fun <X, Y> func(x: String, expr: (Apply<IdOf, X>) -> Apply<IdOf, Y>): Apply<IdOf, (X) -> Y>
                = Id { expr(Id(it)).fix() }
    }

    object Serializer: LambdaAlg<ConstOf<RawExpr>> {
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