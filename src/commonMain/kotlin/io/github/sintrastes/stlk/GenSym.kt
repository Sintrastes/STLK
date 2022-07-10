package io.github.sintrastes.stlk

/** Interface capable of generating unique symbols. */
interface GenSym {
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
                TODO("Not yet implemented")
            }
        }
    }
}