package net.teamfruit.signpic.entry;

/**
 * ガベージコレクション時に呼び出されるインターフェース。
 */
public interface ICollectable {
    /**
     * リソースを解放する。
     * GCによる廃棄時に呼び出される。
     */
    void onCollect();

    /**
     * GC対象かどうかを判定する。
     *
     * @return GC対象の場合true
     */
    default boolean shouldCollect() {
        return false;
    }
}
