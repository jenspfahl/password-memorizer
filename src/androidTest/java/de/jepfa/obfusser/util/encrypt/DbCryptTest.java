package de.jepfa.obfusser.util.encrypt;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DbCryptTest {
    @Test
    public void test() {

        String value1 = "XXx!Xx!!X00X0";
        String value2 = "xxxxXX";
        String encrypted1 = DbCrypt.aesEncrypt(value1);
        String decrypted1a = DbCrypt.aesDecrypt(encrypted1);

        String encrypted2 = DbCrypt.aesEncrypt(value2);
        String decrypted2a = DbCrypt.aesDecrypt(encrypted2);
        DbCrypt.aesDecrypt(encrypted2);
        DbCrypt.aesDecrypt(encrypted2);


        String decrypted1 = DbCrypt.aesDecrypt(encrypted1);
        String decrypted2 = DbCrypt.aesDecrypt(encrypted2);

        DbCrypt.aesDecrypt(encrypted2);
        DbCrypt.aesDecrypt(encrypted2);
        DbCrypt.aesDecrypt(encrypted1);
        DbCrypt.aesDecrypt(encrypted2);

        Assert.assertEquals(value1, decrypted1);
        Assert.assertEquals(value1, decrypted1a);
        Assert.assertNotEquals(value1, encrypted1);

        Assert.assertEquals(value2, decrypted2);
        Assert.assertEquals(value2, decrypted2a);
        Assert.assertNotEquals(value2, encrypted2);

    }
}
