# Trime 项目 OpenCC 使用情况简报

## 核心结论

**OpenCC 的 C++ 代码未被编译进项目，但数据文件被打包到 APK 中，实际未被代码加载使用。**

---

## 详细说明

### 1. OpenCC 数据文件（被打包但未使用）

- **路径**：`app/src/main/jni/OpenCC/data/`
- **内容**：
  - 配置文件：`s2t.json`, `t2s.json`, `s2tw.json` 等 14 个 JSON
  - 词典文件：`STCharacters.txt`, `TWVariants.txt`, `HKVariants.txt` 等
- **流程**：Gradle 插件 `OpenCCDataPlugin` 会将这些文件复制到 `assets/shared/opencc/`
- **状态**：打包到 APK，但 **无任何代码加载使用**

### 2. OpenCC C++ 代码（未被编译）

- **路径**：`app/src/main/jni/OpenCC/` 和 `app/src/main/jni/librime/deps/opencc/`
- **主 CMakeLists.txt**：`add_subdirectory(OpenCC)` 已注释/不存在
- **状态**：未被编译进 native 库

### 3. Kotlin 代码引用（仅显示信息）

- `Const.kt`：定义 `OPENCC_URL` 常量（仅字符串）
- `AboutFragment.kt`：显示 `BuildConfig.OPENCC_VERSION`（仅关于页面）
- `NativeAppConventionPlugin.kt`：获取 OpenCC Git 版本号

---

## 移除步骤

1. **删除数据目录**：`app/src/main/jni/OpenCC/` 整个目录

2. **删除 Gradle 插件**：
   - `build-logic/convention/src/main/kotlin/OpenCCDataPlugin.kt`
   - `app/build.gradle.kts` 中移除 `id("com.osfans.trime.opencc-data")`

3. **删除版本号相关**：
   - 修改 `NativeAppConventionPlugin.kt` 移除 `openccVersion`
   - 删除 `AboutFragment.kt` 中的 OpenCC 版本显示
   - 删除 `Const.kt` 中的 `OPENCC_URL`

4. **可选清理**：
   - `app/src/main/jni/librime/deps/opencc/` 整个目录
   - `app/src/main/jni/cmake/Opencc.cmake`（如存在）
   - `app/src/main/jni/librime_jni/opencc.cc`（如存在）
