# STLK

_**S**imply **T**yped **L**ambda Caclulus (for) **K**otlin_ is an extensbile implementation of the Simply Typed Lambda Calculus in Kotlin implemented with [object algebras](https://www.cs.utexas.edu/~wcook/Drafts/2012/ecoop2012.pdf), and support for [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

# What is it?

The Simply Typed Lambda Calculus (STLC) can be seen as a small, statically typed, _total_ functional programming language. Essentially all it provides is operations for building annonmyous functions (lambda abstraction), and applying them (function application). It being _total_ means that it has the property that _all functions written in the language will eventually terminate_. In other words, there are no infinite loops! The STLC is definitely _not_ a Turing Complete language, and that's a good thing!

The STLC on it's own is not incredibly useful -- but if you add to it a fixed set of operations/functions, what you get is a _domain-specific language_ that can be executed in a sandboxed environment, as well as be easily serialized/deserialized. Thus, STLK makes building a plugin architecture based on safe, sandboxed DSLs easy, no matter the Kotlin target used! Not to mention -- no mucking around with classloaders, dynamic libs and the like. 

**NOTE**: STLK is currently in an experimental stage of development. The documentation here is just a peek at what the library might look like when more fully fleshed out to help me sketch out some ideas and a vision for the project.

# What can I do with it?

What can't you do with it? The possibilities are endless! 

## Serialize and deserialize functions. 

Ever wish you could serialize mathematical functions like `{ x -> x * 2 + 3 }` without a lot of ceremony? Say no more:

```kotlin
fun <F> AirthAlg<F>.someFunction(): Apply<F, (Int) -> Int> =
    func { x -> x + 42 }
    
Json.encodeToString (
    ArithAlg.Serializer.someFunction()
)
```

## Not limited to pure functions

TODO: No need to limit yourself to the pure functions though. Object algebras are just simple, lightweight, modular interpreters built by implementing Kotlin interfaces -- so interpret them however you want!

```kotlin
interface TerminalAlg<F> {
    fun readLine(): Apply<F, String>
    fun printLine(message: String): Apply<F, Unit> 
}
```

## Mix and Match

Thanks to the flexibility of the object algebra encoding, you can mix and match "algebras" (kind of like modules for building up a domain specific language) at will with interface subtyping!

```kotlin
interface MyDSLAlg<F>: ArithAlg<F>, StrManipAlg<F>, ConditionalAlg<F>, LocationServiceAlg<F>, GeomAlg<F> {
    fun Apply<F, User>.getUserLocation(): Pair<Double, Double>
    fun Apply<F, String>.notifyUser()
}

fun <F> MyDSLAlg<F>.pluggableLogic(): Apply<F, Unit> {
    // From MySDLAlg
    val userLocation: Pair<Double, Double> = getUserLocation()
    
    // From LocationServiceAlg
    val userHome: Geometry = getUserHomeBubble() 
    
    // From GeomAlg
    val userIsHome = userLocation.isIn(userHome)
    
    // From ConditonalAlg
    cond(
        if_ = userIsHome,
        // From StrManipAlg and MyDSLAlg
        then = str("You are home").notifyUser(),
        else = str("You are not home").notifyUser()
    )
}
```

## GraphQL... Who needs it?

```kotlin
interface MyApiAlg<F>: ListOperationsAlg<F> {
    fun users(): Apply<F, List<User>>
    fun User.name(): Apply<F, String>
    fun User.age(): Apply<F, Int>
}

fun <F> MyApiAlg<F>.userAgesQuery(): Apply<F, List<Int>> {
    users.map { user ->
        user.age()
    }
}

// Encode and decode complex queries easily.
val encoded = Json.encodeToString(
    MyApiAlg.Serializer.userAgesQuery()
)

val rawExpr = Json.decodeFromString<RawExpr>(encoded)
val parsed: Either<DecodingError, MyApiQuery<List<Int>> 
    = MyApiAlg.parseRawExpr<MyApi, List<Int>>(rawExpr) 
```
# How does it work?

## Object Algebras

The idea behind the basic object algebra pattern is instead of defining a class with multiple variants as a sealed class such as:

```kotlin
sealed class MyClass {
    data class VariantOne(val x: Int): MyClass()
    data class VariantTwo(val y: String): MyClass()
}
```

you instead define an _interface_ which corresponds to how one would build a generic interpreter of such a class consisting of multiple variants. We call these interfaces for this type of interpreter _algebras_, which customarialy have an `Alg` suffix.

```kotlin
interface MyClassAlg<A> {
    fun variantOne(x: Int): A
    fun variantTwo(y: String): A
}
```

"Instances" of this "class" are not constructed directly, but are viewed as generic functions using such an interpreter. For instance:

```kotlin
fun <A> MyClassAlg<A>.someMyClass(): A = 
    variantOne(42)
    
fun <A> MyClassAlg<A>.someOtherMyClass(): A =
    variantTwo("Hello, object algebras")
```

Why bother taking this roundabout way of thinking of classes with variants? Extensibility. Interfaces are incredibly easy and ergonomic to compose in object-oriented languages. If we wanted to write some code using (say) a third variant holding on to a `Double` to our existing `MyClass` without changing `MyClass` itself, this would not be very straightforward or ergonomic. However, using our "object algebras" approach, we can simply do:

```kotlin
interface MyNewClassAlg<A> : MyClassAlg<A> {
    fun variantThree(z: Double): A
}
```

We can also just as easily model recursive classes with variants -- for instance, abstract syntax trees for a simple expression language:

```kotlin
interface IntExprAlg<A> {
    fun intLit(x: Int): A
    
    operator fun A.plus(other: A): A
    operator fun A.times(other: A): A
    operator fun A.minus(other: A): A
}
```

## Higher-kinded Types

The issue with the above approach we explored for building a small _expression language_ for integer arithmetic is that it is limitied to integers. What if we also wanted to add strings to our language? What about booleans and conditionals? One thing we could try is adding multiple generic parameters -- giving what might be called _polyadic object algebras_, borrowing some more mathematical terminology, which might look something like:

```kotlin
interface MultiTypeAlg<A, B> {
    fun int(x: Int): A
    fun bool(x: Bool): B
    
    operator fun A.plus(other: A): A
    inline fun B.and(other: B): B
    ...
}
```

However, this approach quickly becomes infeasible as the number of types grow. Furthermore, it might even be impossible if (for instance) we wanted to bring type-constructors like `List` into the mix, and allow for functions like `map` and `fold` to be included in our DSL.

Essentially what we really need here is a _type-level function_ so we can still control the result type of our interpreters, while still allowing for that result type to vary rather than being constant. For instance, if we call our type-level function `F`, that would make the above example:

```kotlin
interface MultiTypeAlg<F> {
    fun int(x: Int): F<Int>
    fun bool(x: Bool): F<Bool>
    
    operator fun F<Int>.plus(other: F<Int>): F<Int>
    inline fun F<Bool>.and(other: F<Bool>): F<Bool>
    ...
}
```

If `F<X> = X`, we get just a standard interpreter for a language with multiple possible types. If `F<X>` is just some fixed class for all `X`, we can build the same kind of "alternative" interpreters we saw before, like "interpreting" expressionbs into strings.
