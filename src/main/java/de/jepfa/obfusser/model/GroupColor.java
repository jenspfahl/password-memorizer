package de.jepfa.obfusser.model;

public enum GroupColor {
    NO_COLOR(0),
    RED("#d20015"),
    LILA("#9210d5"),
    BLUE("#0010ff"),
    GREEN("#59cd00"),
    GOLD("#e09300"),
    LIGHT_BLUE("#0097f9"),
    GREY("#3e586c"),
    ;

    private String colorRGBHex;

    GroupColor(String colorRGBHex) {
        this.colorRGBHex = colorRGBHex;
    }

    GroupColor(int colorInt) {
        this.colorRGBHex = getRGBHexAsString(colorInt);
    }

    public String getColorRGBHex() {
        return colorRGBHex;
    }

    public int getColorInt() {
        return getColorInt(getColorRGBHex());
    }

    public static int getColorInt(String rgbHexAsString) {
        return Integer.parseInt(rgbHexAsString.replaceFirst("#", ""), 16);
    }

    public static String getRGBHexAsString(int colorInt) {
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }

    public static int getAndroidColor(int colorInt) {
        String hexColor = GroupColor.getRGBHexAsString(colorInt);
        return android.graphics.Color.parseColor(hexColor);
    }
}
