# レガシー実装への整合計画

現在の実装をレガシー (_legacy/) の設計思想に寄せるための修正項目。

## 完了済み

- [x] ImageLoader.java - ImageReader方式、GIF対応、InvaildImageException
- [x] InvaildImageException.java - 新規作成
- [x] LoadCanceledException.java - 新規作成
- [x] ContentDownloader.java - 日本語コメント、ContentCapacityOverException
- [x] State/Progress/StateType.java - 日本語コメント
- [x] SignPicScreen.java - プロパティ編集UI (W/H/X/Y/Z/R)
- [x] SignPicProperties.java - toSignText()メソッド追加

## アーキテクチャの主要な違い

### 1. Entry管理

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| GC機構 | EntrySlot (ティックベース) | lastAccessTime (ミリ秒) | 現在のシンプル方式を維持 |
| EntryId | ItemEntryId/SignEntryId/PreviewEntryId | 統一EntryId | 現在のシンプル方式を維持 |
| NBT対応 | ItemStackからURL抽出 | なし | 将来実装 (Item対応時) |

### 2. Content管理

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| スロット | ContentSlot | なし | 現在のシンプル方式を維持 |
| メタデータ | ContentMeta (JSON) | なし | 必要になったら追加 |
| URL圧縮 | プロトコルプレフィックス ($=https) | フルURL保存 | 現在の方式を維持 |
| 読込パイプライン | LoadQueue/DivisionQueue | 直接スケジューリング | 現在のシンプル方式を維持 |
| リトライ | RetryCountOverException | なし | 検討 |
| ブロック | ContentBlockedException | なし | 不要 |

### 3. 属性(Property)管理

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| フレームワーク | Attrs/AttrReaders/AttrWriters | SignPicProperties | 現在のシンプル方式を維持 |
| アニメーション補間 | PropReaderAnimation | なし | 将来検討 |
| テクスチャパラメータ | TextureFloat/Boolean/Blend | なし | レンダラーで対応 |
| キーフレーム | 時間ベース補間 | なし | 将来検討 |

### 4. レンダリング

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| レンダラー | TileEntity/Block/Item/Book/Chat | SignBlockEntityのみ | 段階的に追加 |
| OpenGL | 直接操作 | RenderSystem/PoseStack | Minecraft標準API使用 |
| ブレンド/ミップマップ | 詳細制御 | 基本設定のみ | 必要に応じて追加 |
| 状態表示 | StateRender | render()内で直接 | 現在の方式を維持 |

**将来対応予定:**
- Item描画 (看板アイテム)
- Book描画 (書物)
- Chat描画 (チャット内画像)

### 5. 設定(Config)

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| フレームワーク | Forge Configuration | シンプルJavaクラス | Cloth Config統合予定 |
| リスナー | ConfigListener | なし | 不要 |
| リロード | IReloadableConfig | なし | Cloth Configで対応 |
| 設定項目 | 60以上 | 約30 | 必要なもののみ維持 |

**削除された設定:**
- signpicDir, signTooltip (不要)
- contentLoadTick, contentSyncTick (簡略化)
- informationJoinBeta, informationTryNew (不要)

### 6. 画像処理

| 項目 | レガシー | 現在 | 対応方針 |
|------|---------|------|----------|
| Imageクラス | RemoteImage/ResourceImage/DynamicImageTexture | ContentTexture | 現在の方式を維持 |
| リソース画像 | ResourceImageTexture | なし | 将来検討 (MODリソース画像) |
| テクスチャパラメータ | OpenGL直接設定 | NativeImage/DynamicTexture | Minecraft標準API使用 |

### 7. ライフサイクルインターフェース

**レガシーで削除されたインターフェース:**
- `IInitable` - 初期化処理
- `IAsyncProcessable` - 非同期処理
- `IDivisionProcessable` - 分割処理
- `ICollectable` - GC処理
- `ILoadCancelable` - キャンセル処理
- `ITickEntry` - ティック処理

**対応方針:** これらの複雑なインターフェースは不要。現在のシンプルな設計を維持。

## 意図的な簡略化

以下の項目はレガシーより意図的にシンプル化:

1. **EntrySlot/ContentSlot削除** - 時間ベースGCで十分
2. **LoadQueue/DivisionQueue削除** - 直接スケジューリングで十分
3. **複雑な属性フレームワーク削除** - SignPicPropertiesで十分
4. **マルチレンダラー統一** - 段階的に追加
5. **複雑なライフサイクル削除** - シンプルなメソッドで十分

## 今後の実装予定

### 高優先度
- [ ] Cloth Config統合 (設定画面)
- [ ] エラーメッセージI18n対応

### 中優先度
- [ ] タスク進捗表示 (GUI)
- [ ] Item描画対応
- [ ] リトライ機構

### 低優先度
- [ ] Book描画対応
- [ ] Chat描画対応
- [ ] リソース画像対応
- [ ] アニメーション補間

## 残りのコード整合タスク

### 日本語コメント化

| ファイル | 状態 |
|---------|------|
| Entry.java | 未対応 |
| EntryId.java | 未対応 |
| EntryManager.java | 未対応 |
| Content.java | 未対応 |
| ContentManager.java | 未対応 |
| ContentTexture.java | 未対応 |
| Communicator.java | 未対応 |
| SignPictureRenderer.java | 未対応 |
| SignPicConfig.java | 未対応 |
| SignRendererMixin.java | 未対応 |
