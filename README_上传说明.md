# 并行任务排程器 v2.7_mimo · GitHub 自动打包 APK

这个压缩包已经把你修改好的 `v2.7_mimo.html` 放进了 `www/index.html`，并配置好了 Capacitor 和 GitHub Actions。

## 上传到 GitHub 的方法

1. 解压这个 zip。
2. 打开你的 GitHub 仓库。
3. 点击 `Add file` → `Upload files`。
4. 把解压后的所有文件和文件夹一起拖进去，包括：
   - `www`
   - `.github`
   - `package.json`
   - `capacitor.config.json`
   - `README_上传说明.md`
5. 点击 `Commit changes`。
6. 进入仓库顶部的 `Actions`。
7. 等待 `Build Android APK` 跑完。
8. 打开这次运行记录，在页面底部 `Artifacts` 下载：
   - `parallel-task-scheduler-v2.7-mimo-debug-apk`
9. 解压 artifact，里面的 `.apk` 就是安卓测试版安装包。

## 注意

- 这是 debug APK，适合自己测试安装。
- 如果手机提示“未知来源应用”，需要在手机系统设置里允许安装。
- 如果以后只改 HTML 功能，通常只需要替换 `www/index.html`，然后提交，GitHub 会重新生成 APK。

## 本版确认

本包使用 `v2.7_mimo.html` 作为 `www/index.html`。时间轴页面点击任务图块弹出详情浮窗、在浮窗中同步修改任务名称/类型/持续时间/启动时间/等待时间的功能已保留。
