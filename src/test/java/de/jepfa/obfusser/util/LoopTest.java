package de.jepfa.obfusser.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class LoopTest {

    @Test
    public void loopApplies() {
        Loop<String> loop = new Loop<>(Arrays.asList(new String[]{"one", "two", "three", "four"}));

        Assert.assertFalse(loop.applies("zero"));
        Assert.assertTrue(loop.applies("one"));
    }

    @Test(expected = IllegalStateException.class)
    public void loopForwardUnknownFrom() {
        Loop<String> loop = new Loop<>(Arrays.asList(new String[]{"one", "two", "three", "four"}));

        Assert.assertEquals("two", loop.forwards("zero", 1));
    }

    @Test
    public void loopForward() {
        Loop<String> loop = new Loop<>(Arrays.asList(new String[]{"one", "two", "three", "four"}));

        Assert.assertEquals("two", loop.forwards("one", 1));
        Assert.assertEquals("four", loop.forwards("two", 2));
        Assert.assertEquals("three", loop.forwards("four", 3));
        Assert.assertEquals("three", loop.forwards("three", 8));

        Assert.assertEquals("two", loop.forwards("three", -1));
        Assert.assertEquals("four", loop.forwards("one", -1));
        Assert.assertEquals("three", loop.forwards("one", -10));
    }

    @Test(expected = IllegalStateException.class)
    public void loopBackwardUnknownFrom() {
        Loop<String> loop = new Loop<>(Arrays.asList(new String[]{"one", "two", "three", "four"}));

        Assert.assertEquals("two", loop.backwards("zero", 1));
    }

    @Test
    public void loopBackward() {
        Loop<String> loop = new Loop<>(Arrays.asList(new String[]{"one", "two", "three", "four"}));

        Assert.assertEquals("four", loop.backwards("one", 1));
        Assert.assertEquals("four", loop.backwards("two", 2));
        Assert.assertEquals("one", loop.backwards("four", 3));
        Assert.assertEquals("three", loop.backwards("three", 8));

        Assert.assertEquals("four", loop.backwards("three", -1));
        Assert.assertEquals("two", loop.backwards("one", -1));
        Assert.assertEquals("three", loop.backwards("one", -10));
    }
}
