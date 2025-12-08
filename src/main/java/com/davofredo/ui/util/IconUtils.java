package com.davofredo.ui.util;

import java.awt.*;
import java.io.InputStream;

public class IconUtils {
    public static final String OPEN_ICON = String.valueOf((char) Integer.parseInt("e930", 16));
    private static Font ICON_FONT;

    private IconUtils() {}

    public static Font getIconFont() {
        if (ICON_FONT == null) {
            loadIconFont();
        }
        return ICON_FONT;
    }

    private static void loadIconFont() {
        try {
            // Load font as InputStream (from resources)
            InputStream is = IconUtils.class.getResourceAsStream("/fonts/icons.ttf"); // Route relative to classpath
            ICON_FONT = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //Register the font to make it available in the application
            ge.registerFont(ICON_FONT);
        } catch (Exception e) {
            e.printStackTrace();
            ICON_FONT = new Font("Arial", Font.PLAIN, 12);
        }
    }

}
