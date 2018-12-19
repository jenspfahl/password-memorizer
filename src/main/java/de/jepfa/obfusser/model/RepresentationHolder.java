package de.jepfa.obfusser.model;

import android.os.Build;

public class RepresentationHolder {

    public static final Representation BLOCKS = new Representation(
            '\u2584', // '▄'
            '\u2588', // '█'
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? '\u2B24' : '\u25CF'),  // '⬤' / '●'
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? '\u2580' : '\u25A0'), // '▀' / '■'
            '\u25A0', // '■'
            '\u25EF'); // '◯'

    public static final Representation VIKING = new Representation(
            '\u16B4', // ''
            '\u16A1', // ''
            '\u16CA',
            '\u16C9',
            '\u16DC', // ''
            '\u25EF'); // '◯'

    public static final Representation HATCHING = new Representation(
            '\u2591', // ''
            '\u2592', // ''
            '\u2593',
            '\u2594',
            '\u2596', // ''
            '\u25EF'); // '◯'

    public static final Representation BRAILLE = new Representation(
            '\u2860', // ''
            '\u28F8', // ''
            '\u28F6',
            '\u2819',
            '\u28FF', // ''
            '\u25EF'); // '◯'

}
