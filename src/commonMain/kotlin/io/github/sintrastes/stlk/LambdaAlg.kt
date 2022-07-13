package io.github.sintrastes.stlk

import io.github.sintrastes.stlk.typeutils.FunMatcher
import io.github.sintrastes.stlk.typeutils.TypeVisitor
import io.github.sintrastes.stlk.typeutils.patternMatchFunType
import io.github.sintrastes.stlk.typeutils.visitType
import kotlin.reflect.*

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
        ): Apply<ConstOf<RawExpr>, (X) -> Y> {
            val sym = genSym("x")

            return Const(
                RawExpr.Lam(
                    sym,
                    expr(Const(RawExpr.Var(sym)))
                        .fix()
                )
            )
        }

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
        inline fun <reified A : Any> deserialize(
            raw: RawExpr,
            atomDeserializer: ExprDeserializer = ExprDeserializer.Empty,
            noinline resolveType: (String) -> KType? = { null }
        ): A? {
            val type = typeOf<A>()
            return deserializeRoot(type, raw, atomDeserializer, resolveType)
        }

        /**
         * Deserializer for deserializing a value from a RawExpr for the [LambdaAlg] DSL
         * using open recursion to that deserializers for other algebras can be built
         * on top of this one.
         *
         * Assumption: [A] must be the same type as [type]. May cause strange issues otherwise
         *  due to type erasure.
         */
        fun <A : Any> deserializeRoot(
            type: KType,
            raw: RawExpr,
            atomDeserializer: ExprDeserializer = ExprDeserializer.Empty,
            resolveType: (String) -> KType? = { null }
        ): A? {
            return when (raw) {
                is RawExpr.App -> {
                    println("App")
                    type.patternMatchFunType(
                        onMatch = object : FunMatcher<A?> {
                            override fun <X : Any, Y : Any> match(
                                inClass: KClass<X>,
                                outClass: KClass<Y>,
                                inType: KType,
                                outType: KType
                            ): A? {
                                println("Matches")
                                return atomDeserializer.deserialize<(X) -> Y>(type, raw.f, atomDeserializer)?.let { f ->
                                    atomDeserializer.deserialize<X>(inType, raw.x, atomDeserializer)?.let { x ->
                                        f(x) as A
                                    }
                                }
                            }
                        },
                        onMiss = run {
                            println("Missed")
                            null
                        }
                    )
                }
                is RawExpr.Lam -> {
                    println("Lam")
                    type.patternMatchFunType(
                        onMatch = object : FunMatcher<A?> {
                            override fun <X : Any, Y : Any> match(
                                inClass: KClass<X>,
                                outClass: KClass<Y>,
                                inType: KType,
                                outType: KType
                            ): A? {
                                return { x: X ->
                                    deserializeLambdaBody<Y, X>(
                                        type,
                                        raw.body,
                                        atomDeserializer,
                                        resolveType,
                                        raw.label,
                                        x
                                    )
                                        ?: throw IllegalArgumentException("Could not parse argument of type $type")
                                } as A
                            }
                        },
                        onMiss = null
                    )
                }
                is RawExpr.AppOp -> {
                    println("AppOp")
                    atomDeserializer
                        .deserialize(type, raw, RootDeserializer)
                }
                is RawExpr.Const -> {
                    atomDeserializer.deserialize(type, raw, atomDeserializer)
                }
                is RawExpr.Var -> {
                    // Error: Unbound variable.
                    println("Unbound variable")
                    null
                }
                is RawExpr.CustomOp -> {
                    atomDeserializer.deserialize(type, raw, atomDeserializer)
                }
            }
        }

        private fun <A : Any, X> deserializeLambdaBody(
            type: KType,
            raw: RawExpr,
            atomDeserializer: ExprDeserializer = ExprDeserializer.Empty,
            resolveType: (String) -> KType?,
            varLabel: String,
            value: X
        ): A? = deserializeRoot(type, raw, atomDeserializer, resolveType)
            ?: when {
                raw is RawExpr.Var && raw.label == varLabel ->
                    value as A
                else -> null
            }
    }

    object RootDeserializer : ExprDeserializer {
        override fun <A : Any> deserialize(type: KType, raw: RawExpr, rec: ExprDeserializer): A? {
            return deserializeRoot(type, raw, rec)
        }
    }
}

/**
 * Interface for a function that can deserialize a raw expression into
 *  a specified class.
 */
interface ExprDeserializer {
    /**
     * Deserialize the raw expression [raw] to the type [type].
     *
     * Assumption: [A] must be the same type as [type].
     */
    fun <A : Any> deserialize(type: KType, raw: RawExpr, rec: ExprDeserializer): A?

    object Empty : ExprDeserializer {
        override fun <A : Any> deserialize(type: KType, raw: RawExpr, rec: ExprDeserializer): A? {
            return null
        }
    }
}