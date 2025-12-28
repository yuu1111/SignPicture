# レガシー実装への整合計画

現在の実装をレガシー (_legacy/) の設計思想に寄せるための修正項目。

## 1. 画像読込層 (ImageLoader.java)

**現在の問題**: `NativeImage.read()`優先でImageIOフォールバックは後付け

**レガシー方式に寄せる**:
```java
// レガシー: ImageIOLoader.java
ImageInputStream imagestream = ImageIO.createImageInputStream(stream);
Iterator<ImageReader> iter = ImageIO.getImageReaders(imagestream);
if (!iter.hasNext())
    throw new InvaildImageException();  // 専用例外
ImageReader reader = iter.next();

if (reader.getFormatName() == "gif")
    textures = loadGif();  // GIF専用処理
else
    textures = loadImage(reader, imagestream);
```

**修正内容**:
1. `ImageIO.getImageReaders()`でフォーマット検出を先行
2. `InvaildImageException`を追加 (typo含め再現)
3. GIF判定とloadGif()の追加
4. `InputFactory`パターンは不要 (Pathで十分)

## 2. HTTP通信層 (ContentDownloader.java)

**現在の問題**: Content-Typeチェックは追加したが、MIMEタイプ保存なし

**レガシー方式**:
```java
// レガシー: ContentDownload.java
cachemeta.setMime(ContentType.getOrDefault(entity).getMimeType());
```

**修正内容**:
1. MIMEタイプを`Content`またはキャッシュメタに保存
2. 日本語コメントに統一

## 3. 専用例外クラス

**レガシーの例外クラス** (再現すべき):
- `InvaildImageException` - 画像形式無効 (typoはレガシーのまま)
- `LoadCanceledException` - 読込キャンセル
- `ContentCapacityOverException` - サイズ超過 (現在: `ContentTooLargeException`)

**修正内容**:
```java
// 追加: common/src/main/java/net/teamfruit/signpic/image/
public class InvaildImageException extends IOException {
    public InvaildImageException() {
        super("Image not of any known type");
    }
}

// 追加: common/src/main/java/net/teamfruit/signpic/
public class LoadCanceledException extends IOException {
    public LoadCanceledException() {
        super("Load was cancelled");
    }
}
```

## 4. State層のI18n対応

**レガシー方式**: `State.setErrorMessage(Throwable)`で例外種類に応じた翻訳

**現在**: 例外をそのまま保持するのみ

**修正方針**: 現在のシンプルな実装を維持しつつ、UIレイヤーで翻訳対応
(State内でI18nは過剰結合のため避ける)

## 5. コメント日本語化

全ファイルのJavadocおよびコメントを日本語に統一。

## 修正対象ファイル一覧

| ファイル | 修正内容 |
|---------|---------|
| `image/ImageLoader.java` | ImageReader方式、GIF対応、InvaildImageException |
| `image/InvaildImageException.java` | 新規作成 |
| `http/ContentDownloader.java` | 日本語コメント、ContentCapacityOverExceptionに改名 |
| `LoadCanceledException.java` | 新規作成 |
| `content/Content.java` | MIMEタイプフィールド追加 |
| `state/State.java` | 日本語コメント |

## 優先順位

1. **高**: ImageLoader.java - ImageReader方式 + InvaildImageException
2. **高**: 例外クラス追加
3. **中**: ContentDownloader.java - 日本語コメント
4. **低**: MIMEタイプ保存 (後で必要になったら)

---

## GUI実装のレガシー整合

### レガシーGUI構成

```
レガシー (bnnwidgetベース):
GuiMain.java (592行) - メインエディタ
├── SignEditor (WPanel)
│   ├── GuiSize - サイズ編集 (W/H)
│   ├── GuiOffset - オフセット編集 (X/Y/Z)
│   ├── GuiRotation - 回転編集 (Quaternion)
│   └── MainTextField - URL入力
├── RightPanel - ボタン群 (SEE/PREVIEW/FILE/OPTION/PLACE)
├── GuiSettings - 設定パネル
└── OverlayFrame - オーバーレイ

GuiImage.java (362行) - 画像表示
├── 3D変換 (OpenGL matrix操作)
├── アニメーションフレーム管理
└── ロード状態表示

GuiTask.java (301行) - タスク表示
├── バックグラウンドタスク一覧
├── プログレスバー
└── キャンセル機能
```

### 現在のGUI構成

```
現在 (Minecraft Screen API):
SignPicScreen.java (354行) - 単一画面
├── EditBox urlField - URL入力
├── Button群 - Load/Copy/Apply/Open/Clear/Reload/Close
├── renderPreview() - 画像プレビュー (128x128)
└── 状態テキスト表示
```

### 主要な違い

| 機能 | レガシー | 現在 |
|------|---------|------|
| サイズ/オフセット/回転編集 | GuiSize/GuiOffset/GuiRotation | **未実装** |
| プロパティ文字列生成 | AttrWriters | SignPicProperties.toSignText() |
| タスク表示 | GuiTask | **未実装** |
| 設定画面 | GuiSettings | **未実装** |
| 画像3D変換プレビュー | GuiImage (OpenGL matrix) | 単純2D表示 |
| アニメーション | VMotion/Easings | animationTime変数 |

### レガシーに寄せる修正方針

bnnwidgetを使わずに、レガシーの機能をMinecraft標準APIで再現:

1. **プロパティ編集パネル追加**
   - サイズ入力 (幅/高さ)
   - オフセット入力 (X/Y/Z)
   - 回転入力 (角度)
   - 「生成」ボタンでURL+プロパティ文字列を結合

2. **タスク表示追加**
   - 画面下部にダウンロード進捗表示
   - Communicatorのタスクキュー監視

3. **コンパクトな単一画面維持**
   - レガシーのマルチパネル構成は過剰
   - 現在のシンプル設計を維持しつつ機能追加

### 優先順位 (GUI)

1. **高**: SignPicScreen.java 日本語コメント化
2. **中**: プロパティ編集UI追加 (サイズ/オフセット/回転)
3. **低**: タスク進捗表示 (後で実装可)
4. **低**: 設定画面 (Cloth Config統合で別途対応)
