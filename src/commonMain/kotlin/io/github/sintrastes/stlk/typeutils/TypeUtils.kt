package io.github.sintrastes.stlk.typeutils

import kotlin.reflect.*

/** Helper function to pattern match on the type of the passed `KType`
 * against the pattern `(A) -> B`. */
fun <R> KType.patternMatchFunType(
    onMatch: FunMatcher<R>,
    onMiss: R
): R {
    val clazz = (this.classifier as? KClass<*>)
    val isFun = clazz is Function1<*, *>

    return if (isFun) {
        val args = this.arguments
        val arg1 = this.arguments[0].type
        val arg2 = this.arguments[1].type

        onMatch.match(
            arg1?.classifier as KClass<*>,
            arg2?.classifier as KClass<*>,
            arg1, arg2
        )
    } else {
        onMiss
    }
}

interface FunMatcher<R> {
    fun <A : Any, B : Any> match(
        inClass: KClass<A>,
        outClass: KClass<B>,
        inType: KType,
        outType: KType
    ): R
}