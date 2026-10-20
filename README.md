# Skycast

一款开源天气 App。Kotlin + Jetpack Compose + Material 3，数据来自 Open-Meteo。

**零 API key、零后端、可离线缓存的界面骨架**——克隆下来用 Android Studio 打开就能跑。

## 功能

- **当前天气**：温度、体感、天气状况、湿度、风速风向、气压、云量
- **24 小时预报**：横向滚动，含降水概率
- **7 天预报**：最高/最低温、天气图标、降水概率
- **空气质量**：欧洲 AQI / 美国 AQI、PM2.5、PM10、臭氧
- **日出日落与紫外线**
- **全球城市搜索**：中文优先，支持按名称模糊匹配
- **自动定位**：优先用最后一次已知位置，失败才请求新定位
- **桌面小组件**：Glance 框架，整块可点击打开 App，每小时后台刷新

## 技术栈

| 层 | 选型 |
|---|---|
| 语言 | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01) + Material 3 |
| 架构 | MVVM + Repository，以 `Flow` 单向驱动 |
| 网络 | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization |
| 存储 | DataStore Preferences |
| 定位 | FusedLocationProviderClient |
| 后台任务 | WorkManager |
| 小组件 | Glance 1.1.1 |
| 依赖注入 | 手动装配（不引 Hilt） |
| AGP / Gradle | 8.7.3 / JDK 17 |

最低 Android 7.0（API 24），目标 API 35。

## 数据来源

全部来自 [Open-Meteo](https://open-meteo.com/)，**免费、无需注册、无需 API key**：

| 用途 | 主机 |
|---|---|
| 预报 | `api.open-meteo.com` |
| 地理编码 | `geocoding-api.open-meteo.com` |
| 空气质量 | `air-quality-api.open-meteo.com` |

数据基于 ECMWF、GFS 等公开气象模型，按 CC-BY 4.0 分发，**允许商业使用**。

> Open-Meteo 免费额度面向非商业用途更宽松；若计划大规模商用，请先阅读其[定价说明](https://open-meteo.com/en/pricing)。

## 项目结构

```
app/src/main/java/com/skycast/weather/
├── SkycastApplication.kt          # Application + 手动依赖容器
├── MainActivity.kt                # 唯一 Activity，承载 Compose
├── data/
│   ├── remote/
│   │   ├── WeatherApi.kt          # Retrofit 接口定义
│   │   ├── NetworkModule.kt       # 三个 Retrofit 实例
│   │   └── dto/                   # 响应模型
│   ├── repository/
│   │   ├── WeatherRepository.kt   # 聚合三个接口
│   │   └── WeatherMapper.kt       # DTO → 领域模型
│   ├── location/LocationProvider.kt
│   └── prefs/SettingsStore.kt     # 已保存地点
├── domain/model/Weather.kt        # 领域模型 + WMO 天气码映射
├── ui/
│   ├── weather/                   # 主界面 + ViewModel
│   └── search/SearchActivity.kt   # 城市搜索
└── widget/                        # 桌面小组件 + 后台刷新
```

### 几个设计取舍

**手动依赖注入而非 Hilt。** 这个体量只有 3 个单例（仓库、设置、定位），Hilt 的注解处理成本大于收益。`AppContainer` 一个类就够。

**以 `savedLocation` 为唯一数据源。** 搜索页写入 DataStore，主界面 ViewModel 通过 `collect` 自动响应——不需要 `startActivityForResult` 或广播。这样任何来源的地点变更都能正确刷新界面。

**Widget 不共享内存。** 小组件跑在独立的 provider 进程，只通过磁盘上的 DataStore 交换数据。`WidgetUpdateWorker` 负责拉取并写入快照。

## 构建

需要 JDK 17 和 Android SDK（API 35）。

```bash
git clone https://github.com/22085/skycast.git
cd skycast
./gradlew assembleDebug        # 产出 app/build/outputs/apk/debug/
```

Release 构建需要签名配置：

```bash
./gradlew assembleRelease      # 需先在 app/build.gradle.kts 配 signingConfigs
```

## 上架前还需要做什么

这个仓库是**功能完整的源码**，不是可以直接上架的成品。提交到应用商店前至少要补：

- [ ] **改包名**：`com.skycast.weather` 改成你自己的域名反写（改 `applicationId` + `namespace` + 目录结构）
- [ ] **换应用名和图标**：`res/values/strings.xml` 的 `app_name`，以及 `mipmap-*` 下的图标（目前是占位图）
- [ ] **生成签名密钥**并妥善保管，配置 `signingConfigs`
- [ ] **补 README 之外的法律文件**：隐私政策（涉及定位，商店强制要求）
- [ ] **加数据缓存**：目前每次冷启动都重新请求，可引入 Room 做离线展示
- [ ] **补单元测试**：`WeatherMapper` 的天气码映射、ViewModel 的状态流转都值得测
- [ ] **校对天气图标**：`res/drawable/` 下多数图标目前是同一个云朵占位，需要按天气状况区分
- [ ] **无障碍**：给所有 `Icon` 补 `contentDescription`
- [ ] **多语言**：目前只有中文，`values-en/` 等目录待补

## 许可

MIT