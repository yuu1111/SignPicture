# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sign Pictureは、看板に画像を表示できるMinecraft Forgeモッドです。複数のMinecraftバージョン(1.7.10, 1.8.9, 1.9.4, 1.10.2, 1.11.2, 1.12.2)をサポートしています。

## Build Commands

```bash
# 開発環境セットアップ (初回のみ)
gradlew setupDecompWorkspace   # デコンパイルされたソースで開発
gradlew setupDevWorkspace      # 難読化されたソースで開発

# ビルド
gradlew build                  # 全バージョンをビルド (出力: artifacts/)

# IDE設定
gradlew genIntellijRuns        # IntelliJ用 (インポート後に実行)
gradlew eclipse                # Eclipse用
```

## Architecture

マルチバージョン対応のため、コードは以下の階層に分割されています:

### ソースセット構成 (sources/)

- `base/` - バージョン非依存の基本インターフェース (CoreEvent, Reference等)
- `universal/` - メインロジック。全バージョン共通のコード
  - `asm/` - バイトコード変換 (ASM)
  - `attr/` - 画像属性パース (サイズ、回転、アニメーション等)
  - `entry/` - 看板エントリ管理とコンテンツローダー
  - `render/` - 看板レンダリング
  - `http/` - 画像ダウンロード通信
  - `gui/` - GUI (エディタ、オーバーレイ)
  - `handler/` - キー・看板イベントハンドラ

### バージョン固有コード (1.x.x/sources/)

各Minecraftバージョンディレクトリには:
- `bootstrap/` - モッドエントリポイント (SignPicture.java, SignPictureCorePlugin.java)
- `compat/` - バージョン固有の互換性レイヤー (Compat*.java)

### 共通モジュール (common/)

バージョン非依存のブートストラップとUniversalVersionerによるバージョン検出。

## Key Classes

- `CoreHandler` - イベントハンドラの中央集約。全ての主要ハンドラを保持
- `SignPicture` - FMLモッドエントリポイント (@Mod)
- `SignPictureCorePlugin` - コアモッドプラグイン (ASM変換用)
- `CompatEvents` / `CompatProxy` - バージョン間差異を吸収する互換性レイヤー

## Configuration

- `gradle.properties` - バージョン番号、mod ID、CurseForge/GitHub設定
- 各サブプロジェクトの `gradle.properties` - Minecraft/Forge バージョン指定
