package io.github.sintrastes.stlk

import arrow.core.computations.nullable
import io.github.sintrastes.stlk.typeutils.ListMatcher
import io.github.sintrastes.stlk.typeutils.patternMatchListType
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
                raw is RawExpr.AppOp && raw.args.size == 2 && raw.f.identifier == "map" ->
                    type.patternMatchListType(
                        onMatch = object : ListMatcher<A?> {
                            override fun <T : Any> match(elementClass: KClass<T>, elementType: KType): A? =
                                nullable.eager {
                                    // TODO: Need to inspect type of either the first argument, or the
                                    // function in order to determine the remaining type variables.

                                    val arg1 = (rec.deserialize<List<A>>(typeOf<List<A>>(), raw.args[0], rec)
                                        ?: LambdaAlg.deserializeRoot(typeOf<List<A>>(), raw.args[0], rec)).bind()

                                    val arg2 = (rec.deserialize<Boolean>(typeOf<Boolean>(), raw.args[1], rec)
                                        ?: LambdaAlg.deserializeRoot(typeOf<Boolean>(), raw.args[1], rec)).bind()

                                    TODO()
                                }
                        },
                        onMiss = null
                    )
                raw is RawExpr.AppOp && raw.args.size == 3 && raw.f.identifier == "fold" -> nullable.eager {
                    TODO()
                }
                else -> null
            }
        }
    }
}