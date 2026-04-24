@echo off
set JAVA_HOME=%USERPROFILE%\AppData\Local\Programs\Eclipse Temurin\JDK17\jdk-17.0.18+8
set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%

echo yyyyyyyyy | "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
echo Installing SDK components...
echo y | "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0"
