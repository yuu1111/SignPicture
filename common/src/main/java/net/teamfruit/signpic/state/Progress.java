package net.teamfruit.signpic.state;

/**
 * ロード操作の進捗を追跡するクラス。
 */
public class Progress {
    /** 完了した作業量 */
    public volatile long done;

    /** 総作業量 (-1は不明を示す) */
    public volatile long overall = -1;

    /**
     * デフォルトコンストラクタ。
     */
    public Progress() {}

    /**
     * 初期値を指定してProgressを構築する。
     *
     * @param done 完了した作業量
     * @param overall 総作業量
     */
    public Progress(long done, long overall) {
        this.done = done;
        this.overall = overall;
    }

    /**
     * 進捗を0.0から1.0の比率で取得する。
     *
     * @return 進捗率、総量が不明な場合は-1
     */
    public float getProgress() {
        if (overall <= 0) {
            return -1f;
        }
        return (float) done / overall;
    }

    /**
     * 進捗を0から100のパーセンテージで取得する。
     *
     * @return パーセンテージ、総量が不明な場合は-1
     */
    public int getPercentage() {
        float progress = getProgress();
        if (progress < 0) {
            return -1;
        }
        return (int) (progress * 100);
    }

    /**
     * 完了状態かどうかを判定する。
     *
     * @return 作業が完了している場合true
     */
    public boolean isComplete() {
        return overall > 0 && done >= overall;
    }

    /**
     * 進捗の文字列表現を返す。
     *
     * @return "完了/総量" 形式、または総量が不明な場合は完了量のみ
     */
    @Override
    public String toString() {
        if (overall <= 0) {
            return String.valueOf(done);
        }
        return done + "/" + overall;
    }
}
