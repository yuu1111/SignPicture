package net.teamfruit.signpic;

import java.io.IOException;

/**
 * 読み込みがキャンセルされた場合にスローされる例外。
 */
public class LoadCanceledException extends IOException {
    public LoadCanceledException() {
        super("Load was cancelled");
    }

    public LoadCanceledException(String message) {
        super(message);
    }
}
