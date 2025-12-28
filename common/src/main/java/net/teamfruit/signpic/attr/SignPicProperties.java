package net.teamfruit.signpic.attr;

import org.jetbrains.annotations.Nullable;

/**
 * SignPictureエントリのプロパティを表すクラス。
 * プロパティはURLの後にハッシュ記号で指定される (例: URL#w1h1r90)
 */
public class SignPicProperties {
    /** 幅 (-1は自動) */
    private float width = -1;

    /** 高さ (-1は自動) */
    private float height = -1;

    /** X方向オフセット */
    private float offsetX = 0;

    /** Y方向オフセット */
    private float offsetY = 0;

    /** Z方向オフセット */
    private float offsetZ = 0;

    /** X軸回転 (ピッチ) */
    private float rotationX = 0;

    /** Y軸回転 (ヨー) */
    private float rotationY = 0;

    /** Z軸回転 (ロール) */
    private float rotationZ = 0;

    /** アニメーション有効フラグ */
    private boolean animated = true;

    /**
     * プロパティ文字列をパースする。
     *
     * @param props プロパティ文字列 (例: "w1h2x0.5y0.5r90")
     * @return パースされたプロパティ
     */
    public static SignPicProperties parse(@Nullable String props) {
        SignPicProperties result = new SignPicProperties();
        if (props == null || props.isEmpty()) {
            return result;
        }

        // プロパティ文字列をパース (例: "w1h2x0.5y0.5r90")
        StringBuilder currentValue = new StringBuilder();
        char currentKey = 0;

        for (int i = 0; i <= props.length(); i++) {
            char c = i < props.length() ? props.charAt(i) : 0;

            if (Character.isLetter(c) || i == props.length()) {
                // 前の値をパース
                if (currentKey != 0 && currentValue.length() > 0) {
                    try {
                        float value = Float.parseFloat(currentValue.toString());
                        result.applyProperty(currentKey, value);
                    } catch (NumberFormatException ignored) {
                        // 無効な値はスキップ
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

    /**
     * プロパティキーと値を適用する。
     */
    private void applyProperty(char key, float value) {
        switch (Character.toLowerCase(key)) {
            case 'w' -> width = value;
            case 'h' -> height = value;
            case 'x' -> offsetX = value;
            case 'y' -> offsetY = value;
            case 'z' -> offsetZ = value;
            case 'r' -> rotationZ = value;
            case 'p' -> rotationX = value;  // ピッチ
            case 'a' -> animated = value != 0;
        }
    }

    /**
     * プロパティ文字列を生成する。
     * URLに付加する形式の文字列を返す。
     *
     * @return プロパティ文字列 (例: "w1h2x0.5r90")、プロパティがない場合は空文字列
     */
    public String toPropertyString() {
        StringBuilder sb = new StringBuilder();

        if (width > 0) {
            sb.append("w").append(formatFloat(width));
        }
        if (height > 0) {
            sb.append("h").append(formatFloat(height));
        }
        if (offsetX != 0) {
            sb.append("x").append(formatFloat(offsetX));
        }
        if (offsetY != 0) {
            sb.append("y").append(formatFloat(offsetY));
        }
        if (offsetZ != 0) {
            sb.append("z").append(formatFloat(offsetZ));
        }
        if (rotationX != 0) {
            sb.append("p").append(formatFloat(rotationX));
        }
        if (rotationZ != 0) {
            sb.append("r").append(formatFloat(rotationZ));
        }
        if (!animated) {
            sb.append("a0");
        }

        return sb.toString();
    }

    /**
     * URLとプロパティを結合した看板テキストを生成する。
     *
     * @param url 画像URL
     * @return 看板に書き込むテキスト
     */
    public String toSignText(String url) {
        String props = toPropertyString();
        if (props.isEmpty()) {
            return url;
        }
        return url + "#" + props;
    }

    /**
     * 浮動小数点数を最短形式でフォーマットする。
     */
    private static String formatFloat(float value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    // ゲッター

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

    // セッター

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    public void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    // ユーティリティメソッド

    /**
     * カスタムサイズが設定されているかどうか。
     */
    public boolean hasCustomSize() {
        return width > 0 || height > 0;
    }

    /**
     * オフセットが設定されているかどうか。
     */
    public boolean hasOffset() {
        return offsetX != 0 || offsetY != 0 || offsetZ != 0;
    }

    /**
     * 回転が設定されているかどうか。
     */
    public boolean hasRotation() {
        return rotationX != 0 || rotationY != 0 || rotationZ != 0;
    }
}
