# 应用使用管理器

一个帮助用户管理应用使用时间和提供及时提醒的 Android 应用。

## 功能特点

### 应用管理
- 显示已安装应用列表
- 为每个应用配置独立的使用规则
- 支持启用/禁用应用监控

### 提醒功能
- 支持多种提醒类型：
  - 文本提醒
  - 图片提醒
  - 音频提醒
  - 视频提醒
- 应用启动时显示初始提醒
- 使用时长达到限制时显示超时提醒

### 使用监控
- 实时监控应用使用状态
- 统计应用使用时长
- 自定义使用时间限制
- 自定义超时提醒消息

## 技术架构

### 使用的技术栈
- Kotlin
- Jetpack Compose UI
- Room 数据库
- Kotlin Coroutines & Flow
- Android Service
- UsageStatsManager API

### 主要组件
- `MainActivity`: 应用主入口，处理权限和导航
- `UsageMonitorService`: 后台监控服务
- `ReminderManager`: 提醒管理器
- `AppSettingsDialog`: 应用设置界面
- `AppListScreen`: 应用列表界面

### 数据存储
- 使用 Room 数据库存储应用设置
- 使用内部存储保存媒体文件

## 项目结构

\## 功能特点

\### 应用管理

\- 显示已安装应用列表

\- 为每个应用配置独立的使用规则

\- 支持启用/禁用应用监控

\### 提醒功能

\- 支持多种提醒类型：

 \- 文本提醒

 \- 图片提醒

 \- 音频提醒

 \- 视频提醒

\- 应用启动时显示初始提醒

\- 使用时长达到限制时显示超时提醒

\### 使用监控

\- 实时监控应用使用状态

\- 统计应用使用时长

\- 自定义使用时间限制

\- 自定义超时提醒消息

\## 技术架构

\### 使用的技术栈

一个帮助用户管理应用使用时间和提供及时提醒的 Android 应用。

\- Kotlin

\- Jetpack Compose UI

\- Room 数据库

\- Kotlin Coroutines & Flow

\- Android Service

\- UsageStatsManager API

\### 主要组件

\- `MainActivity`: 应用主入口，处理权限和导航

\- `UsageMonitorService`: 后台监控服务

\- `ReminderManager`: 提醒管理器

\- `AppSettingsDialog`: 应用设置界面

\- `AppListScreen`: 应用列表界面

\### 数据存储

\- 使用 Room 数据库存储应用设置

\- 使用内部存储保存媒体文件

\## 项目结构

\# 应用使用管理器

app/

├── src/

│ ├── main/

│ │ ├── java/com/example/app/

│ │ │ ├── data/ # 数据层

│ │ │ ├── domain/ # 领域模型

│ │ │ ├── service/ # 后台服务

│ │ │ ├── ui/ # UI 组件

│ │ │ │ ├── components/ # 可复用组件

│ │ │ │ ├── screens/ # 页面

│ │ │ │ └── viewmodels/ # ViewModel

│ │ │ └── utils/ # 工具类

│ │ └── res/ # 资源文件

│ └── androidTest/ # 测试

└── build.gradle

## 使用说明

### 权限要求
- `PACKAGE_USAGE_STATS`: 用于监控应用使用情况
- `FOREGROUND_SERVICE`: 用于运行后台监控服务
- 存储权限：用于保存媒体文件

### 基本使用流程
1. 首次启动时授予必要权限
2. 在应用列表中选择需要管理的应用
3. 点击设置图标配置提醒规则：
   - 选择提醒类型
   - 设置提醒内容
   - 设置使用时间限制
   - 设置超时提醒消息
4. 应用会自动在后台监控使用情况
5. 根据设置显示相应提醒

## 开发说明

### 编译要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Kotlin 1.9.0 或更高版本
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)

### 构建步骤
1. Clone 项目代码
2. 在 Android Studio 中打开项目
3. 等待 Gradle 同步完成
4. 运行项目到设备或模拟器

## 注意事项
- 需要在系统设置中手动授予使用情况访问权限
- 某些设备可能需要在系统设置中允许应用自启动
- 媒体文件会保存在应用的内部存储中

## 未来计划
- [ ] 添加使用统计图表
- [ ] 支持更多提醒类型
- [ ] 优化文件存储机制
- [ ] 添加数据导出功能
- [ ] 支持定时提醒