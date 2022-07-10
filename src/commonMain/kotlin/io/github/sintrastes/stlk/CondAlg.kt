package io.github.sintrastes.stlk

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
}