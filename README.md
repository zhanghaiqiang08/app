# 灵感速记 (InspirationRecorder)

一个极简的安卓 APP：灵感来了，点一下按钮，时间就记下来了。

## 功能

- **一键记录**：点击底部大按钮，立即把当前时间存入本地 SQLite 数据库
- **列表展示**：所有记录按时间倒序显示（时间 + 备注）
- **添加/编辑备注**：点击某条记录，弹出对话框输入备注
- **删除记录**：长按某条记录，确认后删除
- **数据持久化**：使用 SQLite，卸载前数据不会丢失

## 项目结构

```
InspirationRecorder/
├── settings.gradle
├── build.gradle
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/inspirationrecorder/MainActivity.java
│       └── res/
│           ├── layout/activity_main.xml   （主界面）
│           ├── layout/item_record.xml     （列表项）
│           └── values/（strings.xml、themes.xml）
```

## 如何构建

1. 用 Android Studio 打开项目根目录（或直接导入 `app` 模块）
2. 首次构建时如果提示缺少 Gradle Wrapper，执行一次：
   ```
   gradle wrapper
   ```
   （或让 Android Studio 自动生成）
3. 连接手机或启动模拟器，点击 Run 即可安装

命令行构建：
```
./gradlew assembleDebug
```
生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

## 技术说明

- 纯原生 Java，**零第三方依赖**，离线可编译
- minSdk 24（Android 7.0+），targetSdk 34
- 主题使用系统自带 Material 主题，无需 AppCompat
