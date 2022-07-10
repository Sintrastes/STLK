package io.github.sintrastes.stlk

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Generic, untyped representation of a DSL expression
 *  that can be serialized via kotlinx.serialization.
 */
@Serializable
sealed class RawExpr {
    /**
     * [RawExpr] of a bound variable (from a lambda expression).
     */
    @Serializable
    data class Var(val label: String): RawExpr()

    /**
     * [RawExpr] representing the application of a lambda expression applied to
     *  another expression.
     */
    @Serializable
    data class App(val f: RawExpr, val x: RawExpr): RawExpr()

    /**
     * [RawExpr] representing the application of a custom (user-defined)
     *  function applied to a list of expressions passed as arguments.
     */
    @Serializable
    data class AppOp(val f: RawExpr.CustomOp, val args: List<RawExpr>): RawExpr()

    /**
     * [RawExpr] representing a reference to a custom (user-defined)
     *  function or operator.
     */
    @Serializable
    data class CustomOp(val identifier: String): RawExpr()

    /**
     * Representation of a literal expression. Must have a registered
     *  contextual serializer in order to be properly serializable.
     */
    @Serializable
    data class Const(val value: @Contextual Any): RawExpr()
}