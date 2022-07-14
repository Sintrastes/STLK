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

    test("Arithmetic deserializer test") {
        val raw = IntArithAlg.Serializer.example()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<(Int) -> Int>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example(2) shouldBe 16
    }

    test("Nested arithmetic deserializer test") {
        val raw = IntArithAlg.Serializer.example2()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<(Int) -> ((Int) -> Int)>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example(2)(6) shouldBe 16
    }

    test("Complex arithmetic deserializer test") {
        val raw = IntArithAlg.Serializer.example3()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<(Int) -> ((Int) -> Int)>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example(2)(6) shouldBe 31
    }
})

fun <F> LambdaAlg<F>.lambdaExample(): Apply<F, (Int) -> Int> {
    return func { x -> x }
}

fun <F> IntArithAlg<F>.example(): Apply<F, (Int) -> Int> {
    return func { x ->
        int(5) * x + int(6)
    }
}

fun <F> IntArithAlg<F>.example2(): Apply<F, (Int) -> ((Int) -> Int)> {
    return func { x ->
        func { y ->
            int(5) * x + y
        }
    }
}

fun <F> IntArithAlg<F>.example3(): Apply<F, (Int) -> ((Int) -> Int)> {
    return func { x ->
        func { y ->
            y * (int(5) * x + y * x - int(15)) + int(2) - int(13)
        }
    }
}

