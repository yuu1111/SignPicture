package net.teamfruit.signpic.state;

/**
 * Represents the current state of a content loading process.
 */
public enum StateType {
    /** Initial state, not yet started */
    INIT,
    /** Waiting in queue */
    WAITING,
    /** Currently downloading */
    DOWNLOADING,
    /** Currently loading (parsing image, etc.) */
    LOADING,
    /** Successfully loaded and ready */
    LOADED,
    /** Failed to load due to an error */
    ERROR,
    /** Loading was cancelled */
    CANCELLED
}
