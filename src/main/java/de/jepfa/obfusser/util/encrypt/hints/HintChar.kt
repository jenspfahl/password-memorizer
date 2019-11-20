package de.jepfa.obfusser.util.encrypt.hints

open class HintChar {

    var hint: Char = ' '
        protected set
    var isGoNext = false
        protected set


    constructor(hint: Char, goNext: Boolean) {
        this.hint = hint
        this.isGoNext = goNext
    }

    constructor(hint: Char) {
        this.hint = hint
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || !o.javaClass.isAssignableFrom(HintChar::class.java)) return false // allow supertypes

        val that = o as HintChar?

        return hint == that!!.hint
    }

    override fun hashCode(): Int {
        return hint.toInt()
    }

    override fun toString(): String {
        val sb = StringBuffer("HintChar{")
        sb.append("hint='").append(hint).append('\'')
        sb.append(", goNext=").append(isGoNext)
        sb.append('}')
        return sb.toString()
    }

}
