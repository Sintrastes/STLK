package io.github.sintrastes.stlk

import arrow.core.computations.nullable
import kotlin.random.Random
import kotlin.reflect.KType

interface ListOpsAlg<F> {
    fun <A, B> Apply<F, List<A>>.map(f: (Apply<F, A>) -> Apply<F, B>): Apply<F, List<B>>
    fun <A, B> Apply<F, List<A>>.fold(initial: Apply<F, B>, f: (Apply<F, B>, Apply<F, A>) -> Apply<F, B>): Apply<F, B>

    object Interpreter : ListOpsAlg<IdOf> {
        override fun <A, B> Apply<IdOf, List<A>>.map(
            f: (Apply<IdOf, A>) -> Apply<IdOf, B>
        ): Apply<IdOf, List<B>> = Id(
            fix().map { f(Id(it)).fix() }
        )

        override fun <A, B> Apply<IdOf, List<A>>.fold(
            initial: Apply<IdOf, B>,
            f: (Apply<IdOf, B>, Apply<IdOf, A>) -> Apply<IdOf, B>
        ): Apply<IdOf, B> = Id(
            fix().fold(initial.fix()) { x, y ->
                f(Id(x), Id(y)).fix()
            }
        )
    }

    object Serializer : ListOpsAlg<ConstOf<RawExpr>> {
        override fun <A, B> Apply<ConstOf<RawExpr>, List<A>>.map(
            f: (Apply<ConstOf<RawExpr>, A>) -> Apply<ConstOf<RawExpr>, B>
        ): Apply<ConstOf<RawExpr>, List<B>> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("map"),
                listOf(f(Const(RawExpr.Var("x${Random.nextBytes(5)}"))).fix())
            )
        )

        override fun <A, B> Apply<ConstOf<RawExpr>, List<A>>.fold(
            initial: Apply<ConstOf<RawExpr>, B>,
            f: (Apply<ConstOf<RawExpr>, B>, Apply<ConstOf<RawExpr>, A>) -> Apply<ConstOf<RawExpr>, B>
        ): Apply<ConstOf<RawExpr>, B> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("fold"),
                listOf(
                    initial.fix(),
                    f(
                        Const(RawExpr.Var("x${Random.nextBytes(5)}")),
                        Const(RawExpr.Var("x${Random.nextBytes(5)}"))
                    ).fix()
                )
            )
        )
    }

    object Deserializer : ExprDeserializer {
        override fun <A : Any> deserialize(type: KType, raw: RawExpr, rec: ExprDeserializer): A? {
            return when {
                raw is RawExpr.CustomOp -> {
                    TODO()
                }
                raw is RawExpr.AppOp && raw.args.size == 2 -> nullable.eager {
                    TODO()
                }
                raw is RawExpr.AppOp && raw.args.size == 3 -> nullable.eager {
                    TODO()
                }
                else -> null
            }
        }
    }
}