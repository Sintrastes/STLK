package io.github.sintrastes.stlk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LambdaAlgTest : FunSpec({
    test("Test integer arithmetic interpreter") {
        val test = IntArithAlg.Interpreter
            .example()
            .fix()

        test(2) shouldBe 16
    }

    test("Deserializer test") {
        val raw = LambdaAlg.Serializer.lambdaExample()
            .fix()

        println("Serialized: ${Json.encodeToString(raw)}")

        val example = LambdaAlg.deserialize<(Int) -> Int>(raw)!!

        println("$example")

        example(2) shouldBe 2
    }
})

fun <F> LambdaAlg<F>.lambdaExample(): Apply<F, (Int) -> Int> {
    return func { x -> x }
}

fun <F> IntArithAlg<F>.example(): Apply<F, (Int) -> Int> {
    return func { x ->
        x * int(5) + int(6)
    }
}
