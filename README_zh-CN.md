<br>
<br>
<br>
<br>
<p align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="YiPuzzles 图标" width="96" height="96" onerror="this.style.display='none'"/>
</p>
<h1 align="center">YiPuzzles</h1>
<h3 align="center">是你熟悉的，简单却令人上瘾的 2048。</h3>

<p align="center">一款简洁、轻量、隐私至上的 2048 滑块拼图游戏，基于 Material You 与现代化的 Android 工程实践打造。</p>
<p align="center">Made with ❤️ by <a href="https://github.com/lingyicute">lingyicute</a>.</p>
<br>
<br>
<p align="center">
  <a href="README.md">🇺🇸 English</a> • [🇨🇳 中文] •
  <a href="https://github.com/lingyicute/YiPuzzles/releases">📦 下载 APK</a> •
  <a href="https://github.com/lingyicute/YiPuzzles/issues">🐛 报告问题</a>
</p>

<p align="center">
  <a href="https://github.com/lingyicute/YiPuzzles/releases"><img src="https://img.shields.io/github/v/release/lingyicute/YiPuzzles?color=blue&label=Release" alt="Latest Release"></a>
  <a href="https://developer.android.com/about/versions/lollipop"><img src="https://img.shields.io/badge/Android-5.0%2B%20(API%2021%2B)-success" alt="Android Version"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-orange.svg" alt="License: GPL-3.0"></a>
  <a href="https://github.com/lingyicute/YiPuzzles"><img src="https://img.shields.io/badge/Ads%20%26%20Trackers-Zero-brightgreen" alt="No Ads No Tracking"></a>
  <a href="https://github.com/lingyicute/YiPuzzles"><img src="https://img.shields.io/github/stars/lingyicute/YiPuzzles?style=flat&color=yellow" alt="GitHub Stars"></a>
</p>
<br>

## 📖 简介

商店里的大多数 2048 游戏非此即彼：要么塞满了广告、排行榜和账号体系，要么只提供一个 4×4 棋盘。

**YiPuzzles** 选择了一条隐私至上、完全离线的路线：应用**零权限**，内置**四种棋盘大小**与**分棋盘统计**，并采用打磨细致的 **Material You** 界面——配色还可以直接取自你的壁纸。

<br>

## ✨ 特性

- **🧩 经典 2048 玩法**
  - 向任意方向滑动棋盘，数字块合并为下一个 2 的幂。
  - 合成 **2048** 即可获胜——如果胆子够大，还可以继续冲击更高的分数。
  - 支持**撤销一步**与即时**重新开始**。
  - 进度自动保存——随时回到上次的棋盘继续。

- **🎯 四种棋盘大小**
  - 经典的 **4×4**，外加 **5×5**、**6×6**、**7×7**，挑战更长的对局。
  - 主页上的棋盘预览实时应用你当前选择的配色方案。

- **📊 详细的统计数据**
  - 按棋盘大小分别记录：**最大数字**、**最高分**、**各方向滑动次数**、**撤销次数**、**游戏时长**与**单次滑动用时**。

- **🎨 Material You 与精致设计**
  - **动态取色**：从壁纸种子颜色生成和谐的配色方案（Android 12+）。
  - 三种数字块配色：**Material You**、**2048 经典配色**、**深蓝**。
  - 浅色 / 深色 / 跟随系统主题，支持边缘到边缘的全屏显示。
  - 可选的**数字块移动与合并动画**，反馈流畅舒适。

- **🔒 100% 隐私、离线、无广告**
  - **零权限**——应用根本无法触碰自身之外的任何东西。
  - **无广告、无跟踪、无数据统计、无账号、完全不需要联网。**
  - 采用 **GPL-3.0** 许可证。

- **🌍 多语言**
  - 内置**英文**与**简体中文**，另附**教程**与**常见问题**。

- **⚙️ 贴心的玩家选项**
  - 游戏时**保持屏幕常亮**。
  - 纯滑动操作——不会误触，单手即可游玩。

<br>

## 🛠️ 为什么选择 YiPuzzles？（技术内幕）

### 1. 零权限架构
YiPuzzles 在清单文件中**不申请任何权限**。所有游戏数据（棋盘、分数、统计）都保存在本机，游戏 100% 离线运行——设置页里没有任何需要授予的东西，因为代码里就没有需要权限的东西。

### 2. Material You 动态主题
在 Android 12+ 上，YiPuzzles 会从你的壁纸中派生整套配色，让棋盘、数字块与主页预览与设备浑然一体。如果你更喜欢固定的外观，随时可以切回 2048 经典配色或深蓝方案。

### 3. 稳健、有测试保障的游戏引擎
游戏核心（棋盘状态、合并与计分逻辑）保持小而纯粹，并由 **JUnit / Robolectric 单元测试**覆盖棋盘逻辑。**GitHub Actions** 会在每次推送时构建 Release APK，并自动发布**每日构建（nightly）**，保证始终有新鲜的、可复现的下载。

<br>

## 📥 下载与安装

- **[GitHub Releases](https://github.com/lingyicute/YiPuzzles/releases)** —— 稳定版本，外加每次推送自动构建的 **nightly** 版本。

### 系统要求
- **Android 版本**：Android 5.0（Lollipop，API Level 21）及以上。
- **权限**：无。无需授予任何权限——安装即玩。

<br>

## 🔨 从源码构建

### 前置条件
- **JDK 17** 或更高版本
- **Android SDK**（API Level 34）

### 构建命令

1. **克隆仓库**：
   ```bash
   git clone https://github.com/lingyicute/YiPuzzles.git
   cd YiPuzzles
   ```

2. **构建 Debug APK**：
   ```bash
   ./gradlew assembleDebug
   ```

3. **构建 Release APK**：
   ```bash
   ./gradlew assembleRelease
   ```

生成的 APK 位于 `app/build/outputs/apk/`（文件名为 `yipuzzles-<variant>-v<version>.apk`）。

<br>

## 🤗 贡献

欢迎任何形式的贡献！
- **Bug 反馈与功能建议**：请在 [GitHub Issue Tracker](https://github.com/lingyicute/YiPuzzles/issues) 提交 issue。
- **Pull Requests**：提交前请确保代码符合现有风格，且 `./gradlew test` 通过。
- **翻译**：帮助 YiPuzzles 支持更多语言（添加你的 `values-xx` 字符串资源即可）。

<br>

## 📄 许可证

```text
Copyright (C) 2025-2026 lingyicute <li@92li.uk>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
```
