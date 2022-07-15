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

    test("Functon application test") {
        val raw = IntArithAlg.Serializer.example6()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<(Int) -> Int>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example(2) shouldBe 7
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

    test("Functon application arithmetic deserializer test") {
        val raw = IntArithAlg.Serializer.example5()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<(Int) -> ((Int) -> Int)>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example(2)(6) shouldBe 36
    }

    test("Higher-order arithmetic deserializer test") {
        val raw = IntArithAlg.Serializer.example4()
            .fix()

        println("Serialized: $raw")

        val example = LambdaAlg.deserialize<((Int) -> Int) -> Int>(
            raw,
            IntArithAlg.Deserializer
        )!!

        println("$example")

        example { it * it + 2 } shouldBe -5
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

fun <F> IntArithAlg<F>.example4(): Apply<F, ((Int) -> Int) -> Int> {
    return func { f ->
        f(int(2)) - f(int(3))
    }
}

fun <F> IntArithAlg<F>.example5(): Apply<F, (Int) -> ((Int) -> Int)> {
    return func { x ->
        func { y ->
            val f = func<Int, Int> { it + int(5) }
            f(y * (int(5) * x + y * x - int(15)) + int(2) - int(13))
        }
    }
}

fun <F> IntArithAlg<F>.example6(): Apply<F, (Int) -> Int> {
    return func { x ->
        val f = func<Int, Int> { it + int(5) }
        f(x)
    }
}
