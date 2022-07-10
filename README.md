# STLK

_**S**imply **T**yped **L**ambda Caclulus (for) **K**otlin_ is an extensbile implementation of the Simply Typed Lambda Calculus in Kotlin implemented with [object algebras](https://www.cs.utexas.edu/~wcook/Drafts/2012/ecoop2012.pdf), and support for [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

# What is it?

The Simply Typed Lambda Calculus (STLC) can be seen as a small, statically typed, _total_ functional programming language. Essentially all it provides is operations for building annonmyous functions (lambda abstraction), and applying them (function application). It being _total_ means that it has the property that _all functions written in the language will eventually terminate_. In other words, there are no infinite loops! The STLC is definitely _not_ a Turing Complete language, and that's a good thing!

The STLC on it's own is not incredibly useful -- but if you add to it a fixed set of operations/functions, what you get is a _domain-specific language_ that can be executed in a sandboxed environment, as well as be easily serialized/deserialized. Thus, STLK makes building a plugin architecture based on safe, sandboxed DSLs easy, no matter the Kotlin target used! Not to mention -- no mucking around with classloaders, dynamic libs and the like. 

**NOTE**: STLK is currently in an experimental stage of development. The documentation here is just a peek at what the library might look like when more fully fleshed out to help me sketch out some ideas and a vision for the project.

# What can I do with it?

What can't you do with it? The possibilities are endless! 

# Serialize and deserialize functions. 

Ever wish you could serialize mathematical functions like `{ x -> x * 2 + 3 }` without a lot of ceremony? Say no more:

```kotlin
fun <F> AirthAlg<F>.someFunction(): Apply<F, (Int) -> Int> =
    func("x") { x -> x + 42 }
    
Json.encodeToString (
    ArithAlg.Serializer.someFunction()
)
```

# Not limited to pure functions

TODO: No need to limit yourself to the pure functions though. Object algebras are just simple, lightweight, modular interpreters built by implementing Kotlin interfaces -- so interpret them however you want!

```kotlin
interface TerminalAlg<F> {
    fun readLine(): Apply<F, String>
    fun printLine(message: String): Apply<F, Unit> 
}
```

# Mix and Match

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

# GraphQL... Who needs it?

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
