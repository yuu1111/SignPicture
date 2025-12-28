package net.teamfruit.signpic.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ダウンロードリトライ回数が上限を超えた場合にスローされる例外。
 */
public class RetryCountOverException extends Exception {
    public RetryCountOverException() {
    }

    public RetryCountOverException(final @NotNull String message) {
        super(message);
    }

    public RetryCountOverException(final @NotNull Throwable cause) {
        super(cause);
    }

    public RetryCountOverException(final @NotNull String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
