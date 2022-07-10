package io.github.sintrastes.stlk

import kotlin.reflect.KClass

/**
 * The base object algebra for STLK. Provides the simply-typed lambda calculus
 *  operations of lambda abstraction (given by [func]), and application (by
 *  Kotlin's overloaded function application operator -- [invoke]).
 */
interface LambdaAlg<F> {
    fun <X, Y> func(expr: (Apply<F, X>) -> Apply<F, Y>): Apply<F, (X) -> Y>
    operator fun <X, Y> Apply<F, (X) -> Y>.invoke(x: Apply<F, X>): Apply<F, Y>

    object Interpreter : LambdaAlg<IdOf> {
        override fun <X, Y> Apply<IdOf, (X) -> Y>.invoke(x: Apply<IdOf, X>) = Id(fix()(x.fix()))

        override fun <X, Y> func(expr: (Apply<IdOf, X>) -> Apply<IdOf, Y>): Apply<IdOf, (X) -> Y> =
            Id { expr(Id(it)).fix() }
    }

    object Serializer : LambdaAlg<ConstOf<RawExpr>>, GenSym by GenSym.default {
        override fun <X, Y> func(
            expr: (Apply<ConstOf<RawExpr>, X>) -> Apply<ConstOf<RawExpr>, Y>
        ): Apply<ConstOf<RawExpr>, (X) -> Y> = Const(
            expr(Const(RawExpr.Var(genSym("x"))))
                .fix()
        )

        override fun <X, Y> Apply<ConstOf<RawExpr>, (X) -> Y>.invoke(x: Apply<ConstOf<RawExpr>, X>): Apply<ConstOf<RawExpr>, Y> =
            Const(
                RawExpr.App(
                    fix(),
                    x.fix()
                )
            )
    }

    companion object {
        /**
         * Deserializer for deserializing a value from a RawExpr for the [LambdaAlg] DSL
         * using open recursion to that deserializers for other algebras can be built
         * on top of this one. */
        fun <A : Any> deserialize(clazz: KClass<A>, raw: RawExpr, rec: ExprDeserializer = ExprDeserializer.Empty): A? {
            return when (raw) {
                is RawExpr.App -> {
                    rec.deserialize(clazz, raw.f)?.let { f ->
                        rec.deserialize(clazz, raw.x)?.let { x ->
                            TODO()
                        }
                    }
                }
                is RawExpr.AppOp -> {
                    rec.deserialize(clazz, raw)
                }
                is RawExpr.Const -> {
                    rec.deserialize(clazz, raw)
                }
                is RawExpr.Var -> {
                    rec.deserialize(clazz, raw)
                }
                is RawExpr.CustomOp -> {
                    rec.deserialize(clazz, raw)
                }
            }
        }
    }
}

/**
 * Interface for a function that can deserialize a raw expression into
 *  a specified class.
 */
interface ExprDeserializer {
    fun <A : Any> deserialize(clazz: KClass<A>, raw: RawExpr): A?

    object Empty: ExprDeserializer {
        override fun <A : Any> deserialize(clazz: KClass<A>, raw: RawExpr): A? {
            return null
        }
    }
}