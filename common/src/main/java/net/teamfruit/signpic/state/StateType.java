package net.teamfruit.signpic.state;

import org.jetbrains.annotations.NotNull;

/**
 * コンテンツの状態を表す列挙型。
 */
public enum StateType {
    /** 初期状態 */
    INIT("signpic.state.init", LoadingCircle.INIT, LoadingCircleType.WAIT),
    /** 初期化済み */
    INITALIZED("signpic.state.initalized", LoadingCircle.INIT, LoadingCircleType.WAIT),
    /** ダウンロード中 */
    DOWNLOADING("signpic.state.downloading", LoadingCircle.DOWNLOAD, LoadingCircleType.RUN),
    /** ダウンロード完了 */
    DOWNLOADED("signpic.state.downloaded", LoadingCircle.DOWNLOAD, LoadingCircleType.WAIT),
    /** テクスチャ読み込み中 */
    LOADING("signpic.state.loading", LoadingCircle.CONTENTLOAD, LoadingCircleType.RUN),
    /** テクスチャ読み込み完了 */
    LOADED("signpic.state.loaded", LoadingCircle.CONTENTLOAD, LoadingCircleType.WAIT),
    /** 利用可能 */
    AVAILABLE("signpic.state.available"),
    /** エラー */
    ERROR("signpic.state.error"),
    ;

    /** i18nメッセージキー */
    public final @NotNull String msg;

    /** ローディングサークルの種類 */
    public final @NotNull LoadingCircle circle;

    /** ローディングサークルのスピード */
    public final @NotNull LoadingCircleType speed;

    StateType(final @NotNull String s, final @NotNull LoadingCircle circle, final @NotNull LoadingCircleType speed) {
        this.msg = s;
        this.circle = circle;
        this.speed = speed;
    }

    StateType(final @NotNull String s) {
        this(s, LoadingCircle.DEFAULT, LoadingCircleType.DEFAULT);
    }

    /**
     * ローディングサークルの種類。
     */
    public enum LoadingCircle {
        INIT,
        DOWNLOAD,
        CONTENTLOAD,
        DEFAULT
    }

    /**
     * ローディングサークルのスピード。
     */
    public enum LoadingCircleType {
        WAIT,
        RUN,
        DEFAULT
    }
}
