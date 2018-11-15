package de.jepfa.obfusser.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loops through a data set. Imagine a wheel of fortune, where the data stands for all possible
 * options to point on. No matter how many turns the wheel does and no matter in which direction,
 * it always lands on one of the items in the given data.
 *
 * @param <T>
 *
 * @author Jens Pfahl
 */
public class Loop<T> {

    private List<T> data;
    private Map<T, Integer> index = new HashMap<>();

    public Loop(List<T> data) {
        this.data = new ArrayList<>(data);
        for (int i = 0; i < data.size(); i++) {
            index.put(data.get(i), i);
        }
    }

    /**
     * Checks if the given object is part of the data set of this loop.
     * @param t
     * @return
     */
    public boolean applies(T t) {
        return data.contains(t);
    }


    /**
     * Loops forwards through the data set.
     * @param from the object to loop from
     * @param count
     * @return
     * @throws IllegalStateException if the given from-object is not part of this loop.
     * @see #applies(Object)
     */
    public T forwards(T from, int count) throws IllegalStateException {
        if (!index.containsKey(from)) {
            throw new IllegalStateException("Not part of this loop: " + from);
        }

        if (count < 0) {
            return backwards(from, -count);
        }
        int pos = index.get(from);
        int index = (pos + count) % data.size();
        return data.get(index);
    }

    /**
     * Loops backwards through the data set.
     * @param from the object to loop from
     * @param count
     * @return
     * @throws IllegalStateException if the given from-object is not part of this loop.
     * @see #applies(Object)
     */
    public T backwards(T from, int count) throws IllegalStateException {
        if (!index.containsKey(from)) {
            throw new IllegalStateException("Not part of this loop: " + from);
        }

        if (count < 0) {
            return forwards(from, -count);
        }
        count = count % data.size();
        int pos = index.get(from);
        int index = (pos + (data.size() - count)) % data.size();
        return data.get(index);
    }
}
