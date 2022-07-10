package io.github.sintrastes.stlk

import kotlin.native.concurrent.ThreadLocal

/** An object algebra for describing console I/O. */
interface ConsoleAlg<F>: LambdaAlg<F> {
    fun readLn(): Apply<F, String>
    fun printLn(text: Apply<F, String>): Apply<F, Unit>

    // Note: In order to simulate a "free-monad"-type interpreter,
    // here we use a naming scheme for results, counting the number of invocations.
    // To get this to work, we probably also need to keep track of "unused" results.
    // and then it will all need to be post-processed to convert everything into
    // "monadic" (i.e. expression-based) form.
    object Serializer: LambdaAlg<ConstOf<RawExpr>> by LambdaAlg.Serializer, ConsoleAlg<ConstOf<RawExpr>> {
        // Counter to keep track of separate function invocations.
        private var invocation: Int = 0

        override fun readLn(): Apply<ConstOf<RawExpr>, String> {
            val res = Const<RawExpr, String>(
                RawExpr.Var("_result_readLn_$invocation")
            )
            invocation++
            return res
        }

        override fun printLn(text: Apply<ConstOf<RawExpr>, String>): Apply<ConstOf<RawExpr>, Unit> {
            return Const(
                RawExpr.AppOp(
                    RawExpr.CustomOp("printLn"),
                    listOf(text.fix())
                )
            )
        }
    }

    object Interpreter : ConsoleAlg<IdOf> {
        override fun readLn(): Apply<IdOf, String> {
            TODO("Not yet implemented")
        }

        override fun <X, Y> Apply<IdOf, (X) -> Y>.invoke(x: Apply<IdOf, X>): Apply<IdOf, Y> {
            TODO("Not yet implemented")
        }

        override fun <X, Y> func(xexpr: (Apply<IdOf, X>) -> Apply<IdOf, Y>): Apply<IdOf, (X) -> Y> {
            TODO("Not yet implemented")
        }

        override fun printLn(text: Apply<IdOf, String>): Apply<IdOf, Unit> {
            TODO("Not yet implemented")
        }
    }
}

fun <F> ConsoleAlg<F>.echoExample(): Apply<F, Unit> = run {
    val result = readLn()
    printLn(result)
}

