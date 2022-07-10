package io.github.sintrastes.stlk


/**
 * The base object algebra for STLK. Provides the simply-typed lambda calculus
 *  operations of lambda abstraction (given by [func]), and application (by
 *  Kotlin's overloaded function application operator -- [invoke]).
 */
interface LambdaAlg<F> {
    fun <X, Y> func(expr: (Apply<F, X>) -> Apply<F, Y>): Apply<F, (X) -> Y>
    operator fun <X,Y> Apply<F, (X) -> Y>.invoke(x: Apply<F, X>): Apply<F, Y>

    object Interpreter: LambdaAlg<IdOf> {
        override fun <X, Y> Apply<IdOf, (X) -> Y>.invoke(x: Apply<IdOf, X>)
                = Id(fix()(x.fix()))

        override fun <X, Y> func(expr: (Apply<IdOf, X>) -> Apply<IdOf, Y>): Apply<IdOf, (X) -> Y>
                = Id { expr(Id(it)).fix() }
    }

    object Serializer: LambdaAlg<ConstOf<RawExpr>>, GenSym by GenSym.default {
        override fun <X, Y> func(
            expr: (Apply<ConstOf<RawExpr>, X>) -> Apply<ConstOf<RawExpr>, Y>
        ): Apply<ConstOf<RawExpr>, (X) -> Y> = Const(
            expr(Const(RawExpr.Var(genSym("x"))))
                .fix()
        )

        override fun <X, Y> Apply<ConstOf<RawExpr>, (X) -> Y>.invoke(x: Apply<ConstOf<RawExpr>, X>): Apply<ConstOf<RawExpr>, Y> = Const(
            RawExpr.App(
                fix(),
                x.fix()
            )
        )
    }

    companion object {
        /** Deserialize a value from a RawExpr for the [LambdaAlg] DSL. */
        inline fun <reified A> deserialize(raw: RawExpr): A? {
            return when (raw) {
                is RawExpr.App -> TODO()
                is RawExpr.AppOp -> TODO()
                is RawExpr.Const -> TODO()
                is RawExpr.Var -> TODO()
                is RawExpr.CustomOp -> TODO()
            }
        }
    }
}