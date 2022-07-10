package io.github.sintrastes.stlk

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class RawExpr {
    @Serializable
    data class Var(val label: String): RawExpr()
    @Serializable
    data class App(val f: RawExpr, val x: RawExpr): RawExpr()
    @Serializable
    data class AppOp(val f: RawExpr, val args: List<RawExpr>): RawExpr()
    @Serializable
    data class Const(val value: @Contextual Any): RawExpr()
}