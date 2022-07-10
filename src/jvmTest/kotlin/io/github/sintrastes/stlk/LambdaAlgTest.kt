package io.github.sintrastes.stlk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LambdaAlgTest : FunSpec({
    test("Test integer arithmetic interpreter") {
        fun <F> IntArithAlg<F>.example(): Apply<F, (Int) -> Int> {
            return func { x ->
                x * int(5) + int(6)
            }
        }

        val test = IntArithAlg.Interpreter
            .example()
            .fix()

        test(2) shouldBe 16
    }
})
