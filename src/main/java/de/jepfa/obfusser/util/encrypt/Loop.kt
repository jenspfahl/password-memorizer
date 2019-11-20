package de.jepfa.obfusser.util.encrypt

import java.util.ArrayList
import java.util.HashMap

/**
 * Loops through a data set. Imagine a wheel of fortune, where the data stands for all possible
 * options to point on. No matter how many turns the wheel does and no matter in which direction,
 * it always lands on one of the items in the given data.
 *
 * @param <T>
 *
 * @author Jens Pfahl
</T> */
class Loop<T>(data: List<T>) {

    private val data: List<T>
    private val index = HashMap<T, Int>()

    init {
        this.data = ArrayList(data)
        for (i in data.indices) {
            index[data[i]] = i
        }
    }

    /**
     * Checks if the given object is part of the data set of this loop.
     * @param t
     * @return
     */
    fun applies(t: T): Boolean {
        return data.contains(t)
    }


    /**
     * Loops forwards through the data set.
     * @param from the object to loop from
     * @param count
     * @return
     * @throws IllegalStateException if the given from-object is not part of this loop.
     * @see .applies
     */
    @Throws(IllegalStateException::class)
    fun forwards(from: T, count: Int): T {
        if (!index.containsKey(from)) {
            throw IllegalStateException("Not part of this loop: $from")
        }

        if (count < 0) {
            return backwards(from, -count)
        }
        val pos = index[from]!!
        val index = (pos + count) % data.size
        return data[index]
    }

    /**
     * Loops backwards through the data set.
     * @param from the object to loop from
     * @param count
     * @return
     * @throws IllegalStateException if the given from-object is not part of this loop.
     * @see .applies
     */
    @Throws(IllegalStateException::class)
    fun backwards(from: T, count: Int): T {
        var count = count
        if (!index.containsKey(from)) {
            throw IllegalStateException("Not part of this loop: $from")
        }

        if (count < 0) {
            return forwards(from, -count)
        }
        count = count % data.size
        val pos = index[from]!!
        val index = (pos + (data.size - count)) % data.size
        return data[index]
    }
}
