package io.github.sintrastes.stlk

import kotlin.jvm.JvmName

/**
 * Interface for witnessing applications of higher-kinded type
 *  witnesses to values.
 */
interface Apply<F, A>

/** Higher-inded type witness to the type constructor [Id] */
object IdOf

/**
 * Encoding of a type-level identity function.
 */
data class Id<A>(val underlying: A): Apply<IdOf, A>

/** Value-level witness that the types `Apply<IdOf, X>` and
 * `X` are isomorphic. */
@JvmName("fixId")
fun <A> Apply<IdOf, A>.fix(): A {
    return (this as Id<A>).underlying
}

/** Higher-kinded type witness to the type constructor [Const] */
class ConstOf<A>

/**
 * Encoding of a type level "const" function that always returns the
 *  type `A` regardless of the vaue of `X`.
 */
data class Const<A, X>(val underlying: A): Apply<ConstOf<A>, X>

/** Value-level witness that the types `Apply<ConstOf<A>, X>` and
 * `A` are isomorphic. */
@JvmName("fixConst")
fun <A, X> Apply<ConstOf<A>, X>.fix(): A {
    return (this as Const<A,X>).underlying
}