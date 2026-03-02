# Trime 项目打包分析报告

## 一、项目概述

Trime 是一个 Android 输入法应用，基于 Rime 输入法引擎。本报告详细分析了项目的打包流程、文件分类、处理规则以及最终 APK (PK 文件) 中包含的内容。

---

## 二、核心打包流程

### 2.1 主要构建插件

项目使用 Gradle 构建系统，主要插件包括：

| 插件 | 文件位置 | 功能 |
|------|---------|------|
| AndroidAppConventionPlugin | `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt` | 移除不必要的元数据和基线配置 |
| DataChecksumsPlugin | `build-logic/convention/src/main/kotlin/DataChecksumsPlugin.kt` | 生成数据文件校验和 |
| OpenCCDataPlugin | `build-logic/convention/src/main/kotlin/OpenCCDataPlugin.kt` | 处理 OpenCC 数据文件 |
| NativeAppConventionPlugin | `build-logic/convention/src/main/kotlin/NativeAppConventionPlugin.kt` | Native 库构建配置 |
| NativeBaseConventionPlugin | `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt` | Native 基础配置 |

### 2.2 打包流程顺序

1. **OpenCC 数据处理** → `installOpenCCData` 任务
2. **数据校验和生成** → `generateDataChecksums` 任务
3. **资源合并** → `merge${Variant}Assets` 任务
4. **Native 库编译** → 通过 CMake
5. **最终打包** → Android Gradle Plugin

---

## 三、文件分类与处理

### 3.1 被打包的文件类型

#### 3.1.1 Assets 资源文件 (核心数据)

**路径**: `app/src/main/assets/`

| 文件/目录 | 来源 | 说明 |
|-----------|------|------|
| `shared/` | 多来源 | 核心数据目录 |
| `shared/default.yaml` | `app/data/rime/prelude/` | Rime 默认配置 |
| `shared/essay.txt` | `app/data/rime/essay/` | 词库 |
| `shared/key_bindings.yaml` | `app/data/rime/prelude/` | 按键绑定 |
| `shared/luna_pinyin.dict.yaml` | `app/data/rime/luna-pinyin/` | 拼音词典 |
| `shared/luna_pinyin.schema.yaml` | `app/data/rime/luna-pinyin/` | 拼音方案 |
| `shared/luna_pinyin_simp.schema.yaml` | `app/data/rime/luna-pinyin/` | 简体拼音方案 |
| `shared/pinyin.yaml` | `app/data/rime/luna-pinyin/` | 拼音配置 |
| `shared/punctuation.yaml` | `app/data/rime/prelude/` | 标点符号 |
| `shared/stroke.dict.yaml` | `app/data/rime/stroke/` | 笔画词典 |
| `shared/symbols.yaml` | `app/data/rime/prelude/` | 符号表 |
| `shared/tongwenfeng.trime.yaml` | `app/src/main/assets/shared/` | 同文风格配置 |
| `shared/trime.yaml` | `app/src/main/assets/shared/` | Trime 主配置 |
| `shared/opencc/` | OpenCC 数据 | OpenCC 转换数据 (详见下节) |
| `checksums.json` | 自动生成 | 所有数据文件的 SHA256 校验和 |

#### 3.1.2 OpenCC 数据文件 (已打包但未使用)

**路径**: `app/src/main/assets/shared/opencc/`

**配置文件 (14个 JSON)**:
- `hk2s.json`, `hk2t.json`, `jp2t.json`
- `s2hk.json`, `s2t.json`, `s2tw.json`, `s2twp.json`
- `t2hk.json`, `t2jp.json`, `t2s.json`, `t2tw.json`
- `tw2s.json`, `tw2sp.json`, `tw2t.json`

**词典文件 (12个 TXT)**:
- `STCharacters.txt`, `STPhrases.txt`
- `TSCharacters.txt`, `TSPhrases.txt`
- `TWVariants.txt`, `TWVariantsRevPhrases.txt`
- `HKVariants.txt`, `HKVariantsRevPhrases.txt`
- `JPVariants.txt`
- `JPShinjitaiCharacters.txt`, `JPShinjitaiPhrases.txt`

**来源**: 通过 `OpenCCDataPlugin` 从 `app/src/main/jni/OpenCC/data/` 复制并生成。

**状态**: ⚠️ **已打包但代码中未实际使用** (详见 `opencc_analysis.md`)

#### 3.1.3 Native 库 (.so 文件)

**路径**: `lib/${ABI}/`

| 库名 | 来源 | 说明 |
|------|------|------|
| `librime_jni.so` | `app/src/main/jni/librime_jni/` | Rime JNI 绑定 |
| `libc++_shared.so` | NDK | C++ 标准库 (共享) |

**依赖的静态库 (编译进 librime_jni)**:
- `rime-static` (librime)
- `glog` (日志)
- `yaml-cpp` (YAML 解析)
- `leveldb` (键值存储)
- `snappy` (压缩)
- `marisa` (trie 树)
- `boost` (C++ 库)

**支持的 ABI**: 由 `Versions.supportedAbis` 定义

#### 3.1.4 Android 资源文件

**路径**: `app/src/main/res/`

| 目录 | 内容 |
|------|------|
| `res/drawable/` | 图标和矢量图形 |
| `res/layout/` | Activity 和 Fragment 布局 |
| `res/mipmap-*/` | 应用图标 (各分辨率) |
| `res/values/` | 字符串、颜色、主题、属性 |
| `res/values-night/` | 夜间模式主题 |
| `res/values-zh-rCN/` | 简体中文翻译 |
| `res/xml/` | 输入法方法和文件提供者配置 |

#### 3.1.5 Kotlin/Java 代码

**路径**: `app/src/main/java/`

编译成 DEX 文件，包含：
- 主应用代码 (`com.osfans.trime.*`)
- 依赖库 (AndroidX, Kotlin 标准库等)

**混淆配置**: `app/proguard-rules.pro`
- `-dontobfuscate`: 不进行混淆
- 保留 JNI 接口类
- 移除 Kotlin null 检查

#### 3.1.6 AndroidManifest.xml

**路径**: `app/src/main/AndroidManifest.xml`

定义应用权限、组件、版本信息等。

---

## 四、被排除的文件和资源

### 4.1 明确排除的资源

在 `app/build.gradle.kts:114-125` 中配置：

```kotlin
packaging {
    resources {
        excludes += setOf(
            "/META-INF/*.version",
            "/META-INF/*.kotlin_module",
            "/META-INF/androidx/**",
            "/DebugProbesKt.bin",
            "/kotlin-tooling-metadata.json",
        )
    }
}
```

### 4.2 移除的元数据

通过 `AndroidAppConventionPlugin` 移除：
- `META-INF/com/android/build/gradle/app-metadata.properties`
- Baseline Profile 文件 (`assets/dexopt/baseline.prof{,m}`)

### 4.3 未被编译的代码

**OpenCC C++ 代码**:
- `app/src/main/jni/OpenCC/` 下的所有 C++ 源文件
- `app/src/main/jni/librime/deps/opencc/` 下的代码
- 原因: CMakeLists.txt 中未添加 `add_subdirectory(OpenCC)`

### 4.4 未被打包的源文件

以下目录仅用于构建，不进入最终 APK：
- `app/src/main/jni/` 下的所有 C/C++ 源文件
- `app/data/` (数据在构建时被复制到 assets)
- `app/src/test/` (测试代码)
- `build-logic/` (构建逻辑)
- `patches/`, `script/`, `doc/` (项目文档和脚本)
- `.git/`, `.github/` (Git 相关)

---

## 五、精简优化建议

### 5.1 高优先级 (可立即删除)

1. **移除 OpenCC 数据文件** (~5-10MB)
   - 删除: `app/src/main/jni/OpenCC/` 整个目录
   - 删除: `build-logic/convention/src/main/kotlin/OpenCCDataPlugin.kt`
   - 移除: `app/build.gradle.kts` 中的 `id("com.osfans.trime.opencc-data")`
   - 清理: `app/src/main/assets/shared/opencc/` (已生成的)

2. **移除 OpenCC 相关代码引用**
   - `NativeAppConventionPlugin.kt`: 移除 `openccVersion` 相关
   - `Const.kt`: 移除 `OPENCC_URL`
   - `AboutFragment.kt`: 移除 OpenCC 版本显示

### 5.2 中优先级 (评估后删除)

1. **检查 Rime 数据文件**
   - 确认是否需要 `stroke.dict.yaml` (笔画输入)
   - 评估 `essay.txt` 大小和必要性

2. **Native 库优化**
   - 确认是否需要所有 ABI 架构
   - 考虑启用 LTO (链接时优化)

### 5.3 低优先级 (长期优化)

1. **ProGuard/R8 优化**
   - 考虑启用混淆 (当前禁用)
   - 进一步优化资源压缩

2. **资源优化**
   - 压缩 PNG 图标
   - 移除未使用的布局和 drawable

---

## 六、最终 APK 结构概览

```
com.osfans.trime.apk
├── AndroidManifest.xml
├── classes.dex (及 classes2.dex, classes3.dex...)
├── resources.arsc
├── res/
│   ├── drawable/
│   ├── layout/
│   ├── mipmap-*/
│   └── values*/
├── assets/
│   ├── checksums.json
│   └── shared/
│       ├── trime.yaml
│       ├── tongwenfeng.trime.yaml
│       ├── default.yaml
│       ├── key_bindings.yaml
│       ├── punctuation.yaml
│       ├── symbols.yaml
│       ├── pinyin.yaml
│       ├── luna_pinyin.schema.yaml
│       ├── luna_pinyin_simp.schema.yaml
│       ├── luna_pinyin.dict.yaml
│       ├── stroke.dict.yaml
│       ├── essay.txt
│       └── opencc/              (⚠️ 可删除)
│           ├── *.json (14个)
│           └── *.txt (12个)
└── lib/
    ├── armeabi-v7a/
    │   ├── librime_jni.so
    │   └── libc++_shared.so
    ├── arm64-v8a/
    │   ├── librime_jni.so
    │   └── libc++_shared.so
    ├── x86/
    │   ├── librime_jni.so
    │   └── libc++_shared.so
    └── x86_64/
        ├── librime_jni.so
        └── libc++_shared.so
```

---

## 七、关键文件位置速查表

| 功能 | 文件位置 |
|------|---------|
| 主构建配置 | `app/build.gradle.kts` |
| ProGuard 规则 | `app/proguard-rules.pro` |
| Native 构建 | `app/src/main/jni/CMakeLists.txt` |
| 数据校验和插件 | `build-logic/convention/src/main/kotlin/DataChecksumsPlugin.kt` |
| OpenCC 数据插件 | `build-logic/convention/src/main/kotlin/OpenCCDataPlugin.kt` |
| 数据管理器 | `app/src/main/java/com/osfans/trime/data/base/DataManager.kt` |
| 当前 checksums | `app/src/main/assets/checksums.json` |
| OpenCC 分析 | `opencc_analysis.md` |

---

**报告生成时间**: 2026-03-02
**项目版本**: 3.3.9 (versionCode: 20260301)
