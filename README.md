# MoReader 墨阅

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" alt="MoReader Logo"/>
</p>

<p align="center">
  <strong>一款优雅的本地小说阅读器</strong>
</p>

<p align="center">
  <a href="https://github.com/mochen-reader/moreader/releases">
    <img src="https://img.shields.io/github/v/release/mochen-reader/moreader?style=flat-square" alt="GitHub release"/>
  </a>
  <a href="https://github.com/mochen-reader/moreader/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/mochen-reader/moreader?style=flat-square" alt="License"/>
  </a>
  <a href="https://github.com/mochen-reader/moreader/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/mochen-reader/moreader/build.yml?style=flat-square" alt="Build Status"/>
  </a>
</p>

---

## 📖 功能特性

### 核心阅读功能

- **多种格式支持**: TXT、EPUB、MOBI、AZW3、PDF
- **个性化阅读体验**:
  - 多种翻页模式：仿真翻页、覆盖、淡入淡出、上下滚动
  - 多种阅读主题：白色、米黄、护眼绿、灰色、纯黑、自定义
  - 字体大小、行间距、页边距自由调节
  - 自定义字体支持 (.ttf / .otf)
- **书签与笔记**:
  - 多颜色书签分类
  - 文字高亮标注（4种颜色）
  - 段落批注笔记
  - 笔记导出 (TXT / Markdown)
- **全文搜索**: 书内关键词搜索，支持上下文预览

### 书架管理

- **视图切换**: 网格视图 / 列表视图
- **分组管理**: 自定义书籍分组
- **多维排序**: 按书名、导入时间、最近阅读、阅读进度
- **快速搜索**: 按书名/作者模糊匹配
- **批量管理**: 多选删除、移动分组

### 文件导入

- **系统文件选择器**: SAF 标准方式导入
- **文件夹扫描**: 自动发现支持格式的电子书
- **WiFi 传书**: 局域网 HTTP 服务器，电脑浏览器直接上传
- **其他 App 分享**: Intent Filter 支持

### 阅读统计

- 今日阅读时长 / 总阅读时长
- 阅读日历热力图
- 每日阅读目标设定
- 阅读总览：已读书籍数 / 阅读字数

### 系统功能

- **TTS 语音朗读**: 系统引擎，语速/音调可调
- **深色模式**: 跟随系统 Material You 动态配色
- **数据备份**: ZIP 格式导出/恢复
- **定时关闭**: 倒计时自动退出
- **应用锁**: PIN 码/指纹保护

---

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose (100% 纯 Compose，无 XML) |
| 设计规范 | Material Design 3 (Material You) |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 本地数据库 | Room |
| 异步处理 | Kotlin Coroutines + Flow |
| 导航 | Compose Navigation |
| 图片加载 | Coil |
| 构建工具 | Gradle (Kotlin DSL) + Version Catalog |

---

## 📁 项目结构

```
com.mochen.reader/
├── data/                          # 数据层
│   ├── local/
│   │   ├── entity/               # Room 实体类
│   │   ├── dao/                 # Room DAO
│   │   └── database/            # Room 数据库
│   ├── repository/              # Repository 实现
│   └── datastore/               # DataStore 设置
├── domain/                       # 领域层
│   ├── model/                   # 领域模型
│   ├── repository/              # Repository 接口
│   └── usecase/                # 用例
├── presentation/                 # 表现层
│   ├── bookshelf/              # 书架模块
│   ├── reader/                  # 阅读器模块
│   ├── detail/                  # 书籍详情模块
│   ├── statistics/              # 统计模块
│   ├── settings/                # 设置模块
│   ├── components/             # 通用组件
│   ├── theme/                  # 主题系统
│   └── navigation/              # 导航
├── parser/                      # 文件解析
│   ├── BookParser.kt           # 解析器入口
│   ├── TxtParser.kt             # TXT 解析
│   ├── EpubParser.kt            # EPUB 解析
│   └── PdfParser.kt            # PDF 解析
├── service/                     # 系统服务
│   ├── TTSService.kt            # 语音朗读服务
│   └── WifiTransferService.kt   # WiFi 传书服务
├── di/                          # Hilt 模块
└── util/                        # 工具类
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 34
- Gradle 8.4

### 构建项目

```bash
# 克隆项目
git clone https://github.com/mochen-reader/moreader.git
cd moreader

# 同步依赖
./gradlew dependencies

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK (需要签名配置)
./gradlew assembleRelease
```

### 运行项目

1. 用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 选择 `app` 模块
4. 点击 Run 按钮运行

---

## 📱 下载安装

| 版本 | 下载地址 | 更新日期 |
|------|----------|----------|
| Latest | [GitHub Releases](https://github.com/mochen-reader/moreader/releases/latest) | - |

---

## 🧪 功能截图

> 截图待添加

---

## 🤝 贡献指南

欢迎提交 Pull Request 和 Issue！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

---

## 📄 开源协议

本项目基于 [Apache License 2.0](LICENSE) 开源。

---

## 🙏 致谢

- [Epublib](https://github.com/psiegman/epublib) - EPUB 解析库
- [Jsoup](https://jsoup.org/) - HTML 解析库
- [NanoHTTPD](https://github.com/NanoHttpd/NanoHttpd) - 轻量级 HTTP 服务器
- [Coil](https://github.com/coil-kt/coil) - Kotlin 首个图片加载库
- [Android PdfRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) - Android 原生 PDF 渲染

---

<p align="center">
  <strong>Made with ❤️ by MoReader Team</strong>
</p>
