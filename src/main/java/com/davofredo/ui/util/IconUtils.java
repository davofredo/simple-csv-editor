package com.davofredo.ui.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IconUtils {
    private static Font ICON_FONT;
    private static final Map<String, Character> ICON_MAP = new HashMap<>();

    private IconUtils() {
    }

    public static Font getIconFont() {
        if (ICON_FONT == null) {
            loadIconFont();
            loadIconMap();
        }
        return ICON_FONT;
    }

    public static String getIcon(String iconName) {
        // Ensure initialized
        getIconFont();
        if (ICON_MAP.containsKey(iconName)) {
            return String.valueOf(ICON_MAP.get(iconName));
        }
        return "?"; // Placeholder for missing icon
    }

    private static void loadIconFont() {
        try {
            // Load font as InputStream (from resources)
            InputStream is = IconUtils.class.getResourceAsStream("/fonts/icons.ttf");
            if (is == null) {
                System.err.println("Error: icons.ttf not found!");
                return;
            }
            ICON_FONT = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(ICON_FONT);
        } catch (Exception e) {
            e.printStackTrace();
            ICON_FONT = new Font("Arial", Font.PLAIN, 12);
        }
    }

    private static void loadIconMap() {
        try (InputStream is = IconUtils.class.getResourceAsStream("/fonts/scsv-icons.json")) {
            if (is == null) {
                System.err.println("Error: scsv-icons.json not found!");
                return;
            }
            JSONTokener tokener = new JSONTokener(is);
            JSONObject root = new JSONObject(tokener);
            JSONArray iconSets = root.getJSONArray("iconSets");

            if (iconSets.length() > 0) {
                JSONObject selectionSet = iconSets.getJSONObject(0);
                JSONArray selection = selectionSet.getJSONArray("selection");

                for (int i = 0; i < selection.length(); i++) {
                    JSONObject iconDef = selection.getJSONObject(i);
                    String name = iconDef.getString("name");
                    // The 'code' field might not be present in all entries or strictly rely on
                    // 'id'.
                    // However, looking at the file, 'code' seems to be the decimal unicode value.
                    // IMPORTANT: The json snippet provided earlier shows 'code' property.
                    // Let's check how 'code' comes.
                    if (iconDef.has("code")) {
                        int code = iconDef.getInt("code");
                        ICON_MAP.put(name, (char) code);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
