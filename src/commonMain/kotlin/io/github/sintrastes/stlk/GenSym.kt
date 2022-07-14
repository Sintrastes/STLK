package io.github.sintrastes.stlk

/** Interface capable of generating unique symbols. */
interface GenSym {
    /** Generate a unique symbol with the given prefix. */
    fun genSym(prefix: String): String

    companion object {
        /**
         * Default implementation of the GenSym interface.
         *
         * Increments a counter starting from 0 for each prefix.
         */
        val default = object: GenSym {
            val latestSymbol = mutableMapOf<String, Int>()

            override fun genSym(prefix: String): String {
                val count = latestSymbol[prefix] ?: 0
                latestSymbol[prefix] = count + 1
                return "$prefix$count"
            }
        }
    }
}