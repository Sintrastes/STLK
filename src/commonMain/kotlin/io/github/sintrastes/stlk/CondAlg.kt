package io.github.sintrastes.stlk

import arrow.core.computations.nullable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Object algebra for conditionals and boolean operations. */
interface CondAlg<F> {
    fun <A> cond(
        if_: Apply<F, Boolean>,
        then: Apply<F, A>,
        else_: Apply<F, A>
    ): Apply<F, A>

    operator fun Apply<F, Boolean>.not(): Apply<F, Boolean>

    infix fun Apply<F, Boolean>.and(other: Apply<F, Boolean>): Apply<F, Boolean>

    infix fun Apply<F, Boolean>.or(other: Apply<F, Boolean>): Apply<F, Boolean>

    object Interpreter : CondAlg<IdOf> {
        override fun <A> cond(if_: Apply<IdOf, Boolean>, then: Apply<IdOf, A>, else_: Apply<IdOf, A>): Apply<IdOf, A> {
            return Id(
                if (if_.fix()) {
                    then.fix()
                } else {
                    else_.fix()
                }
            )
        }

        override fun Apply<IdOf, Boolean>.not(): Apply<IdOf, Boolean> = Id(
            !fix()
        )

        override fun Apply<IdOf, Boolean>.and(other: Apply<IdOf, Boolean>): Apply<IdOf, Boolean> = Id(
            fix() && other.fix()
        )

        override fun Apply<IdOf, Boolean>.or(other: Apply<IdOf, Boolean>): Apply<IdOf, Boolean> = Id(
            fix() || other.fix()
        )
    }

    object Serializer : CondAlg<ConstOf<RawExpr>> {
        override fun <A> cond(
            if_: Apply<ConstOf<RawExpr>, Boolean>,
            then: Apply<ConstOf<RawExpr>, A>,
            else_: Apply<ConstOf<RawExpr>, A>
        ): Apply<ConstOf<RawExpr>, A> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("cond"),
                listOf(
                    if_.fix(),
                    then.fix(),
                    else_.fix()
                )
            )
        )

        override fun Apply<ConstOf<RawExpr>, Boolean>.not(): Apply<ConstOf<RawExpr>, Boolean> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("not"),
                listOf(fix())
            )
        )

        override fun Apply<ConstOf<RawExpr>, Boolean>.and(other: Apply<ConstOf<RawExpr>, Boolean>): Apply<ConstOf<RawExpr>, Boolean> =
            Const(
                RawExpr.AppOp(
                    RawExpr.CustomOp("and"),
                    listOf(fix(), other.fix())
                )
            )

        override fun Apply<ConstOf<RawExpr>, Boolean>.or(other: Apply<ConstOf<RawExpr>, Boolean>): Apply<ConstOf<RawExpr>, Boolean> =
            Const(
                RawExpr.AppOp(
                    RawExpr.CustomOp("or"),
                    listOf(fix(), other.fix())
                )
            )
    }

    object Deserializer: ExprDeserializer {
        override fun <A : Any> deserialize(type: KType, raw: RawExpr, rec: ExprDeserializer): A? {
            return when {
                raw is RawExpr.CustomOp -> {
                    when (raw.identifier) {
                        "and" -> ({ x: Boolean, y: Boolean -> x && y}) as? A
                        "or" -> ({ x: Boolean, y: Boolean -> x || y }) as? A
                        "not" -> ({ x: Boolean -> !x }) as? A
                        else -> null
                    }
                }
                raw is RawExpr.AppOp && raw.args.size == 1 && raw.f.identifier == "not" -> nullable.eager {
                    val arg1 = (rec.deserialize<Boolean>(typeOf<Boolean>(), raw.args[0], rec)
                        ?: LambdaAlg.deserializeRoot(typeOf<Boolean>(), raw.args[0], rec)).bind()

                    (!arg1) as? A
                }
                raw is RawExpr.AppOp && raw.args.size == 2 -> nullable.eager {
                    val arg1 = (rec.deserialize<Boolean>(typeOf<Boolean>(), raw.args[0], rec)
                        ?: LambdaAlg.deserializeRoot(typeOf<Boolean>(), raw.args[0], rec)).bind()

                    val arg2 = (rec.deserialize<Boolean>(typeOf<Boolean>(), raw.args[1], rec)
                        ?: LambdaAlg.deserializeRoot(typeOf<Boolean>(), raw.args[1], rec)).bind()

                    when (raw.f.identifier) {
                        "and" -> arg1 && arg2
                        "or" -> arg1 || arg2
                        else -> null
                    } as? A
                }
                raw is RawExpr.AppOp && raw.args.size == 3 && raw.f.identifier == "cond" -> nullable.eager {
                    val arg1 = (rec.deserialize<Boolean>(typeOf<Boolean>(), raw.args[0], rec)
                        ?: LambdaAlg.deserializeRoot(typeOf<Boolean>(), raw.args[0], rec)).bind()

                    val arg2 = (rec.deserialize<A>(type, raw.args[1], rec)
                        ?: LambdaAlg.deserializeRoot(type, raw.args[1], rec)).bind()

                    val arg3 = (rec.deserialize<A>(type, raw.args[2], rec)
                        ?: LambdaAlg.deserializeRoot(type, raw.args[2], rec)).bind()

                    if (arg1) {
                        arg2
                    } else {
                        arg3
                    }
                }
                else -> null
            }
        }
    }
}