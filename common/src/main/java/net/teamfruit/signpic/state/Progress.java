package net.teamfruit.signpic.state;

/**
 * Tracks progress of a loading operation.
 */
public class Progress {
    /** Amount of work completed */
    public volatile long done;

    /** Total amount of work to do (-1 if unknown) */
    public volatile long overall = -1;

    public Progress() {}

    public Progress(long done, long overall) {
        this.done = done;
        this.overall = overall;
    }

    /**
     * Gets the progress as a ratio between 0.0 and 1.0.
     * Returns -1 if overall is unknown.
     */
    public float getProgress() {
        if (overall <= 0) {
            return -1f;
        }
        return (float) done / overall;
    }

    /**
     * Gets the progress as a percentage between 0 and 100.
     * Returns -1 if overall is unknown.
     */
    public int getPercentage() {
        float progress = getProgress();
        if (progress < 0) {
            return -1;
        }
        return (int) (progress * 100);
    }

    public boolean isComplete() {
        return overall > 0 && done >= overall;
    }

    @Override
    public String toString() {
        if (overall <= 0) {
            return String.valueOf(done);
        }
        return done + "/" + overall;
    }
}
