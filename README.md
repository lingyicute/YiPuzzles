<br>
<br>
<br>
<br>
<p align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="YiPuzzles Logo" width="96" height="96" onerror="this.style.display='none'"/>
</p>
<h1 align="center">YiPuzzles</h1>
<h3 align="center">Just another 2048, simple yet addictive.</h3>

<p align="center">A clean, lightweight, and privacy-first 2048 puzzle game, crafted with Material You and modern Android engineering.</p>
<p align="center">Made with ❤️ by <a href="https://github.com/lingyicute">lingyicute</a>.</p>
<br>
<br>
<p align="center">
  [🇺🇸 English] • <a href="README_zh-CN.md">🇨🇳 中文</a> •
  <a href="https://2048.92li.uk">🌐 Official Website</a> •
  <a href="https://github.com/lingyicute/YiPuzzles/releases">📦 Download APK</a> •
  <a href="https://github.com/lingyicute/YiPuzzles/issues">🐛 Report Bug</a>
</p>

<p align="center">
  <a href="https://github.com/lingyicute/YiPuzzles/releases"><img src="https://img.shields.io/github/v/release/lingyicute/YiPuzzles?color=blue&label=Release" alt="Latest Release"></a>
  <a href="https://developer.android.com/about/versions/lollipop"><img src="https://img.shields.io/badge/Android-5.0%2B%20(API%2021%2B)-success" alt="Android Version"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-orange.svg" alt="License: GPL-3.0"></a>
  <a href="https://github.com/lingyicute/YiPuzzles"><img src="https://img.shields.io/badge/Ads%20%26%20Trackers-Zero-brightgreen" alt="No Ads No Tracking"></a>
  <a href="https://github.com/lingyicute/YiPuzzles"><img src="https://img.shields.io/github/stars/lingyicute/YiPuzzles?style=flat&color=yellow" alt="GitHub Stars"></a>
</p>
<br>

## 📖 Overview

Most 2048 games in the stores fall into one of two camps: cluttered with advertisements, leaderboards, and accounts, or locked to a single 4×4 grid.

**YiPuzzles** takes a privacy-first, fully offline approach to the classic. It declares **zero permissions**, ships with **four board sizes** and **per-board statistics**, and wraps it all in a polished **Material You** interface that can take its colors straight from your wallpaper.

<br>

## ✨ Features

- **🧩 Classic 2048 Gameplay**
  - Swipe the board in any direction; tiles slide and merge into the next power of two.
  - Reach **2048** — then keep playing for higher scores if you dare.
  - **One-step Undo** and instant **Restart**.
  - Progress is saved automatically — resume any board right where you left off.

- **🎯 Four Board Sizes**
  - The classic **4×4**, plus **5×5**, **6×6**, and **7×7** for longer, more challenging runs.
  - The home screen shows live, themed board previews that match the palette you actually play with.

- **📊 Detailed Statistics**
  - Tracked separately for every board size: **highest number**, **best score**, **swipe counts by direction**, **undo usage**, **playing time**, and **time per swipe**.

- **🎨 Material You & Polished Design**
  - **Dynamic Theming**: generates a harmonious palette from your wallpaper seed colors (Android 12+).
  - Three tile palettes: **Material You**, **Original 2048**, and **Deep Blue**.
  - Day / Night mode (System / Light / Dark) with edge-to-edge display.
  - Optional **tile move & merge animations** for smooth, satisfying feedback.

- **🔒 100% Privacy, Offline & Ad-Free**
  - **Zero permissions** — the app literally cannot touch anything outside itself.
  - **No ads, no tracking, no analytics, no accounts, no network access at all.**
  - Licensed under **GPL-3.0**.

- **🌍 Localized**
  - Ships with **English** and **简体中文**, plus a built-in **tutorial** and **FAQ**.

- **⚙️ Player-Friendly Options**
  - **Keep screen on** while playing.
  - Pure swipe controls — no accidental taps, one-hand playable.

<br>

## 🛠️ Why YiPuzzles? (Under the Hood)

### 1. Zero-Permission Architecture
YiPuzzles declares **no permissions at all** in its manifest. All game state (boards, scores, statistics) is stored locally on the device, the game runs 100% offline, and there is nothing to grant in the settings because there is nothing to audit in the code.

### 2. Material You Dynamic Theming
On Android 12+, YiPuzzles derives its color scheme from your wallpaper, so the board, tiles, and home-screen previews all blend with the rest of your device. Prefer a fixed look? Fall back to the original 2048 palette or the deep blue scheme at any time.

### 3. Robust, Test-Backed Game Engine
The game core (board state, merges, and score calculation) is kept small and pure, with **JUnit / Robolectric unit tests** covering the board logic. **GitHub Actions** builds the release APK on every push and automatically publishes a **nightly build**, so there is always a fresh, reproducible download.

<br>

## 📥 Download & Installation

- **[GitHub Releases](https://github.com/lingyicute/YiPuzzles/releases)** — stable releases plus an automatically built **nightly** on every push.

### System Requirements
- **Android Version**: Android 5.0 (Lollipop, API Level 21) or above.
- **Permissions**: None. There is nothing to grant — install and play.

<br>

## 🔨 Building from Source

### Prerequisites
- **JDK 17** or newer
- **Android SDK** (API Level 34)

### Build Commands

1. **Clone the repository**:
   ```bash
   git clone https://github.com/lingyicute/YiPuzzles.git
   cd YiPuzzles
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

The compiled APKs will be located under `app/build/outputs/apk/` (named `yipuzzles-<variant>-v<version>.apk`).

<br>

## 🤗 Contributing

Contributions are always welcome!
- **Bug Reports & Feature Requests**: Submit an issue on the [GitHub Issue Tracker](https://github.com/lingyicute/YiPuzzles/issues).
- **Pull Requests**: Ensure the code follows the existing style and that `./gradlew test` passes before submitting.
- **Translations**: Help localize YiPuzzles into more languages (add your `values-xx` string resources).

<br>

## 📄 License

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
