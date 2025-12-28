package net.teamfruit.signpic.attr;

import org.jetbrains.annotations.Nullable;

/**
 * Parsed properties for a SignPicture entry.
 * Properties are specified after the URL hash (e.g., URL#w1h1r90)
 */
public class SignPicProperties {
    // Size properties
    private float width = -1;  // -1 means auto
    private float height = -1;

    // Offset properties
    private float offsetX = 0;
    private float offsetY = 0;
    private float offsetZ = 0;

    // Rotation properties
    private float rotationX = 0;
    private float rotationY = 0;
    private float rotationZ = 0;

    // Animation properties
    private boolean animated = true;

    public static SignPicProperties parse(@Nullable String props) {
        SignPicProperties result = new SignPicProperties();
        if (props == null || props.isEmpty()) {
            return result;
        }

        // Parse property string (e.g., "w1h2x0.5y0.5r90")
        StringBuilder currentValue = new StringBuilder();
        char currentKey = 0;

        for (int i = 0; i <= props.length(); i++) {
            char c = i < props.length() ? props.charAt(i) : 0;

            if (Character.isLetter(c) || i == props.length()) {
                // End of previous value, parse it
                if (currentKey != 0 && currentValue.length() > 0) {
                    try {
                        float value = Float.parseFloat(currentValue.toString());
                        result.applyProperty(currentKey, value);
                    } catch (NumberFormatException ignored) {
                        // Invalid value, skip
                    }
                }
                currentKey = c;
                currentValue = new StringBuilder();
            } else if (Character.isDigit(c) || c == '.' || c == '-') {
                currentValue.append(c);
            }
        }

        return result;
    }

    private void applyProperty(char key, float value) {
        switch (Character.toLowerCase(key)) {
            case 'w' -> width = value;
            case 'h' -> height = value;
            case 'x' -> offsetX = value;
            case 'y' -> offsetY = value;
            case 'z' -> offsetZ = value;
            case 'r' -> rotationZ = value;
            case 'p' -> rotationX = value;  // pitch
            case 'a' -> animated = value != 0;
        }
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public boolean isAnimated() {
        return animated;
    }

    public boolean hasCustomSize() {
        return width > 0 || height > 0;
    }

    public boolean hasOffset() {
        return offsetX != 0 || offsetY != 0 || offsetZ != 0;
    }

    public boolean hasRotation() {
        return rotationX != 0 || rotationY != 0 || rotationZ != 0;
    }
}
