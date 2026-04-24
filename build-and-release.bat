@echo off
chcp 65001 >nul
echo ========================================
echo   MoReader 构建与发布脚本
echo ========================================
echo.

:: 获取用户输入
set /p REPO_URL="请输入你的 GitHub 仓库地址: "

cd /d "%~dp0"

echo.
echo [1/4] 检查 Java 环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请先安装 JDK 17+
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)
java -version

echo.
echo [2/4] 检查 Gradle Wrapper...
if not exist "gradlew.bat" (
    echo [错误] 未找到 gradlew.bat，请确保项目完整
    pause
    exit /b 1
)

echo.
echo [3/4] 正在构建 Debug APK...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo [错误] 构建失败
    pause
    exit /b 1
)

echo.
echo [4/4] APK 构建成功！
echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
echo.

:: 复制 APK 到根目录
copy "app\build\outputs\apk\debug\app-debug.apk" "MoReader-debug.apk"
echo 已复制到: MoReader-debug.apk

echo.
echo ========================================
echo   下一步操作:
echo ========================================
echo 1. 打开 GitHub 仓库页面
echo 2. 点击 Releases -^> Create a new release
echo 3. 点击 "Upload assets" 上传 MoReader-debug.apk
echo 4. 发布 Release
echo.
echo 仓库地址: %REPO_URL%
echo.

pause
