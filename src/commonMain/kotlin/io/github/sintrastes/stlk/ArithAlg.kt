package io.github.sintrastes.stlk

/** Object algebra for arithmetic operations on integers. */
interface IntArithAlg<F>: LambdaAlg<F> {
    fun int(x: Int): Apply<F, Int>
    operator fun Apply<F, Int>.plus(other: Apply<F, Int>): Apply<F, Int>
    operator fun Apply<F, Int>.times(other: Apply<F, Int>): Apply<F, Int>
    operator fun Apply<F, Int>.minus(other: Apply<F, Int>): Apply<F, Int>

    object Interpreter: LambdaAlg<IdOf> by LambdaAlg.Interpreter, IntArithAlg<IdOf> {
        override fun int(x: Int): Apply<IdOf, Int> = Id(x)

        override fun Apply<IdOf, Int>.minus(other: Apply<IdOf, Int>)
                = Id(fix() - other.fix())

        override fun Apply<IdOf, Int>.times(other: Apply<IdOf, Int>)
                = Id(fix() * other.fix())

        override fun Apply<IdOf, Int>.plus(other: Apply<IdOf, Int>)
                = Id(fix() + other.fix())
    }

    object Serializer: LambdaAlg<ConstOf<RawExpr>> by LambdaAlg.Serializer, IntArithAlg<ConstOf<RawExpr>> {
        override fun int(x: Int): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.Const(x)
        )

        override fun Apply<ConstOf<RawExpr>, Int>.minus(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("minus"),
                listOf(fix(), other.fix())
            )
        )

        override fun Apply<ConstOf<RawExpr>, Int>.times(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("times"),
                listOf(fix(), other.fix())
            )
        )

        override fun Apply<ConstOf<RawExpr>, Int>.plus(other: Apply<ConstOf<RawExpr>, Int>): Apply<ConstOf<RawExpr>, Int> = Const(
            RawExpr.AppOp(
                RawExpr.CustomOp("plus"),
                listOf(fix(), other.fix())
            )
        )
    }
}