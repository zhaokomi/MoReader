@echo off
chcp 65001 >nul
echo ========================================
echo   MoReader 项目部署脚本
echo ========================================
echo.

:: 检查是否以管理员权限运行
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [提示] 建议以管理员权限运行以安装 Git
    echo.
)

:: 检查 Git 是否已安装
where git >nul 2>&1
if %errorlevel% equ 0 (
    echo [✓] Git 已安装
    git --version
    echo.
    goto :push
)

echo [1/3] 正在下载 Git for Windows...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/git-for-windows/git/releases/download/v2.44.0.windows.1/MinGit-2.44.0-64-bit.zip' -OutFile '%TEMP%\git.zip' -UseBasicParsing}"
if %errorlevel% neq 0 (
    echo [错误] Git 下载失败，请检查网络连接
    pause
    exit /b 1
)

echo [2/3] 正在安装 Git...
powershell -Command "Expand-Archive -Path '%TEMP%\git.zip' -DestinationPath 'C:\Git' -Force"
set PATH=C:\Git\cmd;%PATH%
echo [✓] Git 安装完成

:: 配置 Git
git config --global user.email "bot@moreader.app"
git config --global user.name "MoReader Bot"
git config --global init.defaultBranch main

echo.
:push
echo [3/3] 正在初始化 Git 仓库并推送代码...
echo.

:: 获取用户输入
set /p REPO_URL="请输入你的 GitHub 仓库地址 (例如: https://github.com/username/moreader.git): "

cd /d "%~dp0"

:: 初始化仓库
git init
git add .
git commit -m "✨ Initial commit: MoReader 墨阅 - Android Novel Reader"

:: 添加远程仓库并推送
git remote remove origin >nul 2>&1
git remote add origin "%REPO_URL%"
git branch -M main
git push -u origin main --force

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   [✓] 代码推送成功！
    echo ========================================
) else (
    echo.
    echo [错误] 推送失败，请检查仓库地址是否正确
)

echo.
pause
