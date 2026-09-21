# Cocktail Lab V3：只用手机生成 APK

## 最简单的方法

1. 在手机浏览器打开 https://github.com/ 并登录。
2. 新建一个公开或私有仓库，例如 `CocktailLab`。
3. 上传本项目中的全部文件和文件夹，特别是 `.github/workflows/build-apk.yml`。
4. 提交到 `main` 分支。
5. 打开仓库的 **Actions**，选择 **Build Cocktail Lab APK**。
6. 点击 **Run workflow**（如果 push 已自动触发，也可以直接打开正在运行的任务）。
7. 等待任务显示绿色成功。
8. 打开这次 workflow 的运行记录，在底部 **Artifacts** 下载 `CocktailLab-APK`。
9. 解压后得到 `CocktailLab.apk`，直接在小米手机上安装。

## 注意

- 这是 debug APK，适合个人安装和测试。
- 以后如果正式发布或长期更新，建议生成签名 Release APK，并妥善保存签名密钥。
- GitHub Actions 会使用 Java 17、Gradle 8.11.1 自动构建，不需要手机安装 Android Studio。
