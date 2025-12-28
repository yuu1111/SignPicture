package net.teamfruit.signpic.image;

import java.io.IOException;

/**
 * 画像形式が無効な場合にスローされる例外。
 * レガシー実装との互換性のため、クラス名のtypo (Invaild) を維持。
 */
public class InvaildImageException extends IOException {
    public InvaildImageException() {
        super("Image not of any known type");
    }

    public InvaildImageException(String message) {
        super(message);
    }
}
