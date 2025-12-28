package net.teamfruit.signpic.state;

import net.teamfruit.signpic.LoadCanceledException;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentBlockedException;
import net.teamfruit.signpic.content.ContentCapacityOverException;
import net.teamfruit.signpic.content.RetryCountOverException;
import net.teamfruit.signpic.image.InvaildImageException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * コンテンツアイテムの現在の状態を表すクラス。
 * ロード進捗、エラー情報、状態タイプを保持する。
 */
public class State {
    /** 状態名 */
    private @NotNull String name = "";

    /** 進捗情報 */
    private @NotNull Progress progress = new Progress();

    /** 現在の状態タイプ */
    private @NotNull StateType type = StateType.INIT;

    /** エラーメッセージ */
    private @NotNull String message = "";

    /** 任意メタデータ */
    private final @NotNull Map<String, Object> map = new HashMap<>();

    /**
     * 状態名を設定する。
     *
     * @param name 状態名
     * @return このオブジェクト
     */
    public @NotNull State setName(final @NotNull String name) {
        this.name = name;
        return this;
    }

    /**
     * 状態名を取得する。
     *
     * @return 状態名
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * 進捗情報を設定する。
     *
     * @param progress 進捗情報
     * @return このオブジェクト
     */
    public @NotNull State setProgress(final @NotNull Progress progress) {
        this.progress = progress;
        return this;
    }

    /**
     * 進捗情報を取得する。
     *
     * @return 進捗情報
     */
    public @NotNull Progress getProgress() {
        return this.progress;
    }

    /**
     * エラーメッセージを設定する。
     *
     * @param message メッセージ
     * @return このオブジェクト
     */
    public @NotNull State setMessage(final @NotNull String message) {
        this.message = message;
        return this;
    }

    /**
     * 任意メタデータを取得する。
     *
     * @return メタデータマップ
     */
    public @NotNull Map<String, Object> getMeta() {
        return this.map;
    }

    /**
     * エラーを設定する (setErrorMessageへのエイリアス)。
     *
     * @param throwable 例外
     * @return このオブジェクト
     */
    public @NotNull State setError(final @Nullable Throwable throwable) {
        return setErrorMessage(throwable);
    }

    /**
     * 例外に基づいてエラーメッセージを設定する。
     * レガシー実装に忠実な例外型別メッセージ設定。
     *
     * @param throwable 例外
     * @return このオブジェクト
     */
    public @NotNull State setErrorMessage(final @Nullable Throwable throwable) {
        if (throwable != null) {
            setType(StateType.ERROR);
            try {
                throw throwable;
            } catch (final URISyntaxException e) {
                setMessage("signpic.advmsg.invalidurl");
            } catch (final LoadCanceledException e) {
                setMessage("signpic.advmsg.loadstopped");
            } catch (final RetryCountOverException e) {
                setMessage("signpic.advmsg.retryover");
            } catch (final ContentCapacityOverException e) {
                setMessage("signpic.advmsg.capacityover");
            } catch (final ContentBlockedException e) {
                setMessage("signpic.advmsg.blocked");
            } catch (final InvaildImageException e) {
                setMessage("signpic.advmsg.invalidimage");
            } catch (final IOException e) {
                setMessage("signpic.advmsg.ioerror");
            } catch (final Throwable e) {
                setMessage("signpic.advmsg.unknown");
            }
            SignPicture.LOGGER.debug(getMessage(), throwable);
        }
        return this;
    }

    /**
     * エラーメッセージを取得する。
     *
     * @return メッセージ
     */
    public @NotNull String getMessage() {
        return this.message;
    }

    /**
     * 状態タイプを設定する。
     *
     * @param type 状態タイプ
     * @return このオブジェクト
     */
    public @NotNull State setType(final @NotNull StateType type) {
        this.type = type;
        return this;
    }

    /**
     * 状態タイプを取得する。
     *
     * @return 状態タイプ
     */
    public @NotNull StateType getType() {
        return this.type;
    }

    /**
     * 状態メッセージを取得する (i18n対応)。
     *
     * @return 状態メッセージ
     */
    public @NotNull String getStateMessage() {
        return this.type.msg + " (" + (int) (this.progress.getProgress() * 100) + "%)";
    }

    /**
     * ロード中かどうかを判定する。
     *
     * @return ダウンロード中またはロード中の場合true
     */
    public boolean isLoading() {
        return type == StateType.DOWNLOADING || type == StateType.LOADING;
    }

    /**
     * 完了状態かどうかを判定する。
     *
     * @return 利用可能の場合true
     */
    public boolean isComplete() {
        return type == StateType.AVAILABLE;
    }

    /**
     * 失敗状態かどうかを判定する。
     *
     * @return エラーの場合true
     */
    public boolean isFailed() {
        return type == StateType.ERROR;
    }

    /**
     * 状態を初期状態にリセットする。
     */
    public void reset() {
        this.type = StateType.INIT;
        this.progress = new Progress();
        this.message = "";
        this.map.clear();
    }
}
