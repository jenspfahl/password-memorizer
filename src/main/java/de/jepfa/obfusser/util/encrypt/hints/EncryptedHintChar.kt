package de.jepfa.obfusser.util.encrypt.hints

class EncryptedHintChar(hint: Char, roundTrips: Int) : HintChar(hint) {

    var roundTrips: Int = 0
        private set

    /**
     * @return format is char + roundtrip (one digit), e.g. "x2"
     */
    val hintStoreString: String
        get() = hint.toString() + roundTrips

    init {
        this.roundTrips = roundTrips
    }

    fun doNext(): Boolean {
        roundTrips++
        return isGoNext && roundTrips < MAX_ROUND_TRIPS
    }

    fun apply(hintChar: HintChar?) {
        hintChar?.let {
            hint = hintChar.hint
            isGoNext = hintChar.isGoNext
        }
    }

    override fun toString(): String {
        val sb = StringBuffer("EncryptedHintChar{")
        sb.append("hint='").append(hint).append('\'')
        sb.append(", goNext=").append(isGoNext)
        sb.append(", roundTrips=").append(roundTrips)
        sb.append('}')
        return sb.toString()
    }

    companion object {

        private val MAX_ROUND_TRIPS = 3


        fun ofDecrypted(c: Char): EncryptedHintChar {
            return EncryptedHintChar(c, 0)
        }

        /**
         *
         * @param encHintData format must be char + roundtrip (one digit), e.g. "x2"
         * @return
         */
        fun ofEncrypted(encHintData: String): EncryptedHintChar {
            val chars = encHintData.toCharArray()
            val encHint = chars[0]
            val roundTrips = Integer.parseInt(chars[1].toString())
            return EncryptedHintChar(encHint, roundTrips)
        }
    }

}
