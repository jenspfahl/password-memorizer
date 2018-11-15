package de.jepfa.obfusser.model;

import org.junit.Assert;
import org.junit.Test;

public class ObfusCharTest {

    @Test
    public void test() {
        for (int i = 0; i < 256; i++) {
            char c = (char) i;

            System.out.println(i + " " + c  + " " +
                    Character.isDefined(c) + " " +
                    Character.isDigit(c) + " " +
                    Character.isLetter(c) + " " +
                    Character.isLowerCase(c) + " " +
                    Character.isUpperCase(c));

            Assert.assertEquals(Character.isAlphabetic(c), Character.isLetter(c));
            Assert.assertEquals(Character.isLetter(c), Character.isLowerCase(c) ^ Character.isUpperCase(c));
        }
    }
}
