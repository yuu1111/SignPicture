package net.teamfruit.signpic.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * コンテンツがブロックされた場合にスローされる例外。
 */
public class ContentBlockedException extends Exception {
    public ContentBlockedException() {
    }

    public ContentBlockedException(final @NotNull String message) {
        super(message);
    }

    public ContentBlockedException(final @NotNull Throwable cause) {
        super(cause);
    }

    public ContentBlockedException(final @NotNull String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
