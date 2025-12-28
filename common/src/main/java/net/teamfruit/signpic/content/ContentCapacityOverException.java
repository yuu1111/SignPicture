package net.teamfruit.signpic.content;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * コンテンツサイズが上限を超えた場合にスローされる例外。
 */
public class ContentCapacityOverException extends IOException {
    public ContentCapacityOverException() {
    }

    public ContentCapacityOverException(final @NotNull String message) {
        super(message);
    }

    public ContentCapacityOverException(final @NotNull Throwable cause) {
        super(cause);
    }

    public ContentCapacityOverException(final @NotNull String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    public ContentCapacityOverException(long actual, long max) {
        super("Content size exceeded: " + actual + " bytes (max: " + max + ")");
    }
}
