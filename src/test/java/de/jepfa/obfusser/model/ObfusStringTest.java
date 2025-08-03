package de.jepfa.obfusser.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import de.jepfa.obfusser.util.encrypt.EncryptUtil;

public class ObfusStringTest {

    @Test
    public void testWithShortPattern() {
        ObfusString orig = ObfusString.obfuscate("aB1!");
        ObfusString s = new ObfusString(orig);
        System.out.println(s);

        byte[] key = EncryptUtil.generateSalt();

        s.encrypt(key);
        System.out.println(s);

        s.decrypt(key);
        System.out.println(s);

        Assert.assertEquals(orig, s);
    }

    @Test
    public void testWithNormalPattern() {
        ObfusString orig = ObfusString.obfuscate("aaaBBB111!!!");
        ObfusString s = new ObfusString(orig);
        System.out.println(s);

        byte[] key = EncryptUtil.generateSalt();

        s.encrypt(key);
        System.out.println(s);

        s.decrypt(key);
        System.out.println(s);

        Assert.assertEquals(orig, s);
    }

    @Test
    public void testWithLongPattern() {
        ObfusString orig = ObfusString.obfuscate("aaaaBBBB1111!!!!!");
        ObfusString s = new ObfusString(orig);
        System.out.println(s);

        byte[] key = EncryptUtil.generateSalt();

        s.encrypt(key);
        System.out.println(s);

        s.decrypt(key);
        System.out.println(s);

        Assert.assertEquals(orig, s);
    }

    @Test
    public void testWithManyPatterns() {
        for (int i = 0; i < 1000; i++) {
            int length = 4 + new Random().nextInt(16);
            byte[] key = EncryptUtil.generateRnd(length);
            ObfusString orig = EncryptUtil.keyToObfusString(key);
            ObfusString s = new ObfusString(orig);
            s.encrypt(key);
            System.out.println("orig=" + orig);
            System.out.println("enc =" + s);
            System.out.println();
            s.decrypt(key);

            Assert.assertEquals(orig, s);
        }
    }


    @Test
    public void testTryUnlockPattern() {
        for (int i = 0; i < 1000; i++) {


            ObfusString orig = ObfusString.obfuscate("aaaBBB111!!!");
            ObfusString s = new ObfusString(orig);

            byte[] key = EncryptUtil.generateSalt();

            s.encrypt(key);

            byte[] otherKey = EncryptUtil.generateSalt();
            s.decrypt(otherKey);

            System.out.println("orig    =" + orig);
            System.out.println("dec try =" + s);
            System.out.println();

            if (orig == s) {
                System.out.println("found it" + Arrays.toString(otherKey));
                break;
            }
        }
    }

}
