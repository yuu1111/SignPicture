package net.teamfruit.signpic.state;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the current state of a content item including its loading progress.
 */
public class State {
    private volatile StateType type = StateType.INIT;
    private volatile Progress progress;
    private volatile Throwable error;
    private volatile String message;

    public StateType getType() {
        return type;
    }

    public void setType(StateType type) {
        this.type = type;
    }

    @Nullable
    public Progress getProgress() {
        return progress;
    }

    public void setProgress(@Nullable Progress progress) {
        this.progress = progress;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    public void setError(@Nullable Throwable error) {
        this.error = error;
        if (error != null) {
            this.type = StateType.ERROR;
        }
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    public boolean isLoading() {
        return type == StateType.DOWNLOADING || type == StateType.LOADING;
    }

    public boolean isComplete() {
        return type == StateType.LOADED;
    }

    public boolean isFailed() {
        return type == StateType.ERROR || type == StateType.CANCELLED;
    }

    public void reset() {
        this.type = StateType.INIT;
        this.progress = null;
        this.error = null;
        this.message = null;
    }
}
