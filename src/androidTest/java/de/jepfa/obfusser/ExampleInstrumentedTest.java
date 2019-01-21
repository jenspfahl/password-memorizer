package de.jepfa.obfusser;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.Pair;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.jepfa.obfusser.util.EncryptUtil;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();


        assertEquals("de.jepfa.obfusser", appContext.getPackageName());


        String value = "ehiojsnälysdcms5673r.,345";
        String key = "1234567890";
        String alias = "0123456789ABCDEF";
        Pair<byte[], byte[]> encrypted = EncryptUtil.encryptText(alias, value);
        System.out.println(encrypted.toString());
        String decrypted = EncryptUtil.decryptData(alias, encrypted);

        Assert.assertEquals(value, decrypted);

    }
}
