# SignPicture Architectury Migration Plan

## Overview

SignPictureをArchitectury APIを使用してモダンなマルチローダー・マルチバージョン対応に移行する。

### Target
- **MCバージョン**: 1.16.5, 1.18.2, 1.19.x, 1.20.x, 1.21.x (同時対応)
- **ローダー**: Forge, Fabric, NeoForge, Quilt
- **ライセンス**: LGPL-3.0 (Architecturyと互換性あり)

### Legacy
- 既存の1.7.10〜1.12.2対応コードは別ブランチ(例: `legacy`)で維持

## Multi-Version Strategy

**Stonecutter**を使用して1つのコードベースから複数MCバージョンをビルド:

```
signpic/
├── versions/
│   ├── 1.16.5/          # バージョン固有設定
│   ├── 1.18.2/
│   ├── 1.19.4/
│   ├── 1.20.1/
│   └── 1.21.1/
├── src/main/java/       # 共通ソース (バージョン条件分岐あり)
└── stonecutter.gradle   # Stonecutter設定
```

**条件分岐例**:
```java
//? if >=1.20.1
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;*/
//?}
```

Stonecutterにより:
- 全バージョンを単一ソースで管理
- `./gradlew chiseledBuild`で全バージョンビルド
- バージョン固有コードはコメント条件で切替

## Project Structure (Architectury + Stonecutter)

```
signpic/
├── versions/                           # MCバージョン設定
│   ├── 1.16.5/gradle.properties
│   ├── 1.18.2/gradle.properties
│   ├── 1.19.4/gradle.properties
│   ├── 1.20.1/gradle.properties
│   └── 1.21.1/gradle.properties
├── common/                             # 共通コード (80-90%)
│   └── src/main/java/net/teamfruit/signpic/
│       ├── SignPicture.java
│       ├── SignPictureExpectPlatform.java
│       ├── entry/                      # エントリ管理
│       ├── image/                      # 画像読込
│       ├── http/                       # HTTP通信
│       ├── attr/                       # 属性パース
│       ├── render/                     # レンダリング抽象化
│       ├── gui/                        # GUI抽象化
│       ├── command/                    # コマンド
│       └── mixin/                      # 共通Mixin (バージョン条件分岐)
├── fabric/                             # Fabric実装
│   └── src/main/java/.../
│       └── SignPictureFabric.java
├── forge/                              # Forge実装
│   └── src/main/java/.../
│       └── SignPictureForge.java
├── neoforge/                           # NeoForge実装 (1.20.1+)
│   └── src/main/java/.../
│       └── SignPictureNeoForge.java
├── quilt/                              # Quilt実装
│   └── src/main/java/.../
│       └── SignPictureQuilt.java
├── build.gradle
├── settings.gradle
├── stonecutter.gradle                  # Stonecutter設定
└── gradle.properties
```

## Migration Phases

### Phase 1: Foundation
1. `architectury-migration`ブランチ作成
2. Architectury Loomでプロジェクト構造セットアップ
3. 各プラットフォームサブプロジェクト設定
4. 基本コンパイル確認

### Phase 2: Core Utilities Migration
Minecraft非依存コードの移植:
- `Log.java`, `Reference.java`
- HTTP通信層 (`Communicator`, ダウンロード)
- 画像読込 (`ImageIOLoader`)
- 属性パース

### Phase 3: ASM → Mixin変換 (最重要)

現在のASM変換をMixinに置換:

| ASM Visitor | Mixin Target | 優先度 |
|-------------|-------------|-------|
| TileEntityVisitor | SignBlockEntityMixin | 1 (最簡単) |
| GuiScreenVisitor | ScreenMixin | 2 |
| GuiNewChatVisitor | ChatComponentMixin | 3 (最複雑) |
| GuiScreenBookVisitor | BookScreenMixin | 4 |

**変換前 (ASM)**:
```java
// TileEntityVisitor - getRenderBoundingBox()にINFINITE_EXTENT_AABB返却を注入
```

**変換後 (Mixin)**:
```java
@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {
    @Inject(method = "getRenderBoundingBox", at = @At("HEAD"), cancellable = true)
    private void signpic$getRenderBoundingBox(CallbackInfoReturnable<AABB> cir) {
        cir.setReturnValue(INFINITE_EXTENT_AABB);
    }
}
```

### Phase 4: Event System Migration

CompatEventsをArchitecturyイベントに置換:

| 現在のイベント | Architecturyイベント |
|--------------|---------------------|
| CompatTickEvent.ClientTickEvent | ClientTickEvent.CLIENT_POST |
| CompatRenderWorldLastEvent | ClientRenderWorldEvent / WorldRenderEvents |
| CompatRenderGameOverlayEvent | ClientGuiEvent.RENDER_HUD |
| CompatGuiOpenEvent | ClientGuiEvent.INIT_POST |
| CompatMouseEvent | ClientRawInputEvent.MOUSE_CLICKED_PRE |

### Phase 5: Rendering Migration

**レンダリング抽象化レイヤー作成**:
```java
// common/platform/render/
public interface IPlatformRenderer {
    void pushMatrix();
    void popMatrix();
    void translate(float x, float y, float z);
    void scale(float x, float y, float z);
    void rotate(float angle, float x, float y, float z);
    // ...
}
```

**主要変更点**:
- LWJGL 2 → LWJGL 3
- `GlStateManager` → `RenderSystem`
- 直接行列操作 → `PoseStack`
- `TileEntitySignRenderer` → `BlockEntityRenderer<SignBlockEntity>`

### Phase 6: bnnwidget Dependency

**推奨**: ハイブリッドアプローチ
1. bnnwidgetをSignPictureにインライン化
2. OpenGLブリッジインターフェース作成
3. プラットフォーム固有実装提供

```java
public interface IOpenGLBridge {
    void glPushMatrix();
    void glPopMatrix();
    void glTranslatef(float x, float y, float z);
    // ...
}
```

### Phase 7: GUI System

- Cloth Configで設定画面
- 既存GUIは抽象化レイヤー経由で動作

### Phase 8: Commands

Brigadierベースのコマンドシステム:
```java
// common
public class SignPictureCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("signpic")...);
    }
}
```

### Phase 9: Testing & Polish

1. 全プラットフォームでテスト
2. 全対象MCバージョンでテスト
3. パフォーマンス最適化

## Critical Files

移行時に最も注意が必要なファイル:

| ファイル | 理由 |
|---------|------|
| `sources/universal/.../asm/SignPictureTransformer.java` | Mixinに完全置換 |
| `1.12.2/sources/compat/.../CompatEvents.java` | Architecturyイベントに置換 |
| `sources/universal/.../render/CustomBlockSignRenderer.java` | OpenGL抽象化必要 |
| `sources/universal/.../gui/GuiImage.java` | bnnwidget依存、OpenGL使用 |
| `sources/universal/.../image/DynamicImageTexture.java` | テクスチャAPI抽象化 |

## Build Configuration

**gradle.properties**:
```properties
mod_version=3.0.0
architectury_version=9.1.12
minecraft_version=1.20.1
fabric_loader_version=0.14.21
fabric_api_version=0.86.0+1.20.1
forge_version=47.1.0
neoforge_version=20.1.48
quilt_loader_version=0.19.2
```

## Risk Areas

| リスク | 対策 |
|-------|-----|
| GuiNewChat Mixin複雑 | 早期着手、必要ならチャット画像機能簡略化 |
| bnnwidget統合 | アダプタレイヤー先行開発 |
| バージョン間差異 | Architecturyの抽象化活用 |
| NeoForge API分岐 | 個別ロジック必要時は分離 |

## Dependencies

- Architectury API (LGPL-3.0)
- Stonecutter (マルチバージョン)
- Cloth Config (GUI)
- Fabric API / Forge / NeoForge / Quilt標準ライブラリ
