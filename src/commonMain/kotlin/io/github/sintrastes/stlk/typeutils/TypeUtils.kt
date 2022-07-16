package io.github.sintrastes.stlk.typeutils

import kotlin.reflect.*

/** Helper function to pattern match on the type of the passed `KType`
 * against the pattern `(A) -> B`. */
fun <R> KType.patternMatchFunType(
    onMatch: FunMatcher<R>,
    onMiss: R
): R {
    val clazz = (this.classifier as? KClass<*>)
    val isFun = clazz!! == Function1::class

    return if (isFun) {
        val args = this.arguments
        val arg1 = args[0].type
        val arg2 = args[1].type

        onMatch.match(
            arg1?.classifier as KClass<*>,
            arg2?.classifier as KClass<*>,
            arg1, arg2
        )
    } else {
        onMiss
    }
}

/** Helper function to pattern match on the type of the passed `KType`
 * against the pattern `List<A>`. */
fun <R> KType.patternMatchListType(
    onMatch: ListMatcher<R>,
    onMiss: R
): R {
    val clazz = (this.classifier as? KClass<*>)
    val isList = clazz!! == List::class

    return if (isList) {
        val args = this.arguments
        val arg1 = args[0].type

        onMatch.match(
            arg1?.classifier as KClass<*>,
            arg1
        )
    } else {
        onMiss
    }
}

fun <R> KType.visitType(
    typeVisitor: TypeVisitor<R>
): R = typeVisitor
    .visitType(this.classifier as KClass<*>)

interface TypeVisitor<R> {
    fun <T : Any> visitType(type: KClass<T>): R
}

interface FunMatcher<R> {
    fun <A : Any, B : Any> match(
        inClass: KClass<A>,
        outClass: KClass<B>,
        inType: KType,
        outType: KType
    ): R
}

interface ListMatcher<R> {
    fun <A : Any> match(
        elementClass: KClass<A>,
        elementType: KType,
    ): R
}