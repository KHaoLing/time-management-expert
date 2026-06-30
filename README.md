# 并行排程 Native Android 版

这是一个“原生 Android”工程，不是 WebView 套壳，也不需要把 HTML 打包进去。

当前实现：

- Java + Android 原生 View
- 自定义 Canvas 时间轴
- 前置任务、持续任务、等待任务三种类型
- 等待任务：前段纯色，等待段渐变到白色并保留边框
- 任务块可拖动
- 简单自动排程：考虑前置关系和人工占用冲突
- 本地保存：SharedPreferences + JSON
- GitHub Actions 在线构建 APK

## 不会编程时怎么在线生成 APK

你不需要安装 Android Studio，也不需要用 Spyder。

### 第 1 步：注册 / 登录 GitHub

打开 GitHub，新建一个仓库，例如：`parallel-scheduler`。

### 第 2 步：上传本工程所有文件

把这个 zip 解压后，进入 `native-parallel-scheduler` 文件夹。
把里面所有文件上传到 GitHub 仓库。

注意：一定要上传隐藏文件夹 `.github`，因为里面有自动构建 APK 的配置。

### 第 3 步：运行在线构建

进入你的 GitHub 仓库：

1. 点顶部 `Actions`
2. 左侧点 `Build Android APK`
3. 点 `Run workflow`
4. 等待构建完成
5. 点进入最新一次运行记录
6. 页面底部 `Artifacts` 下载 `parallel-scheduler-debug-apk`
7. 解压后得到 `app-debug.apk`
8. 发到手机安装

## 本地用 Android Studio 构建

如果以后你安装 Android Studio：

1. Open 打开本文件夹
2. 等 Gradle Sync 完成
3. Build → Build App Bundle(s) / APK(s) → Build APK(s)
4. APK 在 `app/build/outputs/apk/debug/app-debug.apk`

## 代码结构

```text
app/src/main/java/com/example/parallelscheduler/
├── MainActivity.java                  # 页面入口和按钮
├── data/TaskRepository.java           # 本地保存/读取
├── engine/SchedulerEngine.java        # 排程算法
├── model/SchedulerTask.java           # 任务数据
├── model/TaskType.java                # 任务类型
└── ui/SchedulerTimelineView.java      # 原生时间轴绘制和拖动
```

## 后续正规产品化方向

这个工程已经是原生 Android，不是网页壳。后续可以继续加：

- 编辑任务名称和时长
- 可视化设置前置关系
- 冲突检测提示
- 导入 / 导出 JSON
- 多方案保存
- 横屏大时间轴
- 正式签名 release APK
