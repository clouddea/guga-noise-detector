# 咕嗄声音检测 · GugaNoiseDetector

安卓环境噪声分贝检测 App（Jetpack Compose + Material 3）。

打开即用麦克风实时测量环境声压级，圆形仪表 + 波形图 + 四统计值（最小 / 平均 / 中位 / 最大），
并显示当前定位。检测记录保存在本机，可查看历史列表与详情。角色「咕嗄」（穿企鹅连体衣的 Q 版女孩）
的表情会随分贝等级切换：安静→睡觉，正常→开心，嘈杂→震惊，震耳→生气。

## 构建

本机没有把 `java` 加进 PATH，而 Gradle wrapper 需要 JDK，构建前先指定 Android Studio 自带的 JBR：

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

其他常用任务：

```bash
./gradlew :app:compileDebugKotlin     # 只做语法/依赖检查（最快）
./gradlew :app:testDebugUnitTest      # DbMath 纯函数单测
```

Windows 下若 Git Bash 因路径空格出问题，可改用 `cmd //c gradlew.bat :app:assembleDebug`。

### 打正式包（release，已签名）

release 包用 `keystore.properties` 里配置的密钥签名，该文件与 `keystore/` 目录**都不入库**（见 `.gitignore`）：

```bash
./gradlew :app:assembleRelease
```

产物：`app/build/outputs/apk/release/app-release.apk`

> ⚠️ `keystore/guga-release.jks` 和 `keystore.properties` 里的密码**务必自行备份**。
> 丢失后无法再给同一个 App 发布更新（签名不一致，用户必须卸载重装）。

## 宣传页（GitHub Pages）

`docs/` 目录是 GitHub Pages 的发布源：

```
docs/
├── index.html                          # 宣传页
├── .nojekyll                           # 关掉 Jekyll 处理
├── assets/                             # 角色图 + 四档真机截图
└── guga-noise-detector-v1.0.0.apk      # 下载用的安装包
```

访问地址：https://clouddea.github.io/guga-noise-detector/

发新版本时：重新 `assembleRelease`，把 APK 复制进 `docs/`（改好文件名），同步更新 `index.html` 里的下载链接和版本号。

## 技术要点

- **AGP 9.3.1 / Kotlin 2.2.10 / Compose BOM 2026.02.01**，compileSdk & targetSdk 37，minSdk 24，Java 17。
- **零新增插件**：导航是手写状态机（不加 navigation-compose）；持久化用 `org.json` + `filesDir`
  单文件（不加 Room/DataStore/kotlinx.serialization）；定位用框架 `LocationManager` + `Geocoder`
  （不加 play-services-location）；权限用 `ActivityResultContracts`（不加 accompanist）。
  只加了 `lifecycle-runtime-compose` 与 `lifecycle-viewmodel-compose`。
- **音频链路**：`AudioRecord`（48 kHz 优先，回退 44.1 kHz）→ RMS → `20*log10(rms) + 94` 近似 dB。
  指针用 EMA 平滑，统计用原始采样，平均值按能量平均（Leq 定义）而非算术平均。
- **主题**：马卡龙低饱和配色，`dynamicColor` 已关闭（否则 Android 12+ 会用壁纸取色覆盖整套配色）；
  数字字体 Nunito（可变字体，单文件），中文自动回退系统 CJK 字体。

## ⚠️ 关于读数

手机麦克风**未做声学校准**，dB 是按「满量程 ≈ 94 dB SPL」这一通用约定做的近似换算，
误差可达 ±10 dB。**仅供日常参考，不能用于法定噪声测量或职业健康评估**（App 内已明确标注）。

## ⚠️ 关于角色素材

角色贴纸取自 `gugugagapenguin.com`，站方标注为 AI 生成、**个人使用免费、商用需查授权**；
「咕咕嘎嗄」本身源自《明日方舟：终末地》女管理员的二创，**版权存在争议**。
若要上架或商用，请替换为原创形象或先取得授权。

## 设计稿

UI 设计稿在 `design-demos/` 下（HTML 高保真原型，可双击打开）：
`C-kanahei-pastel.html` 是最终采用的方向，`A-purecss-art.html` / `B-duolingo-candy.html` 是另外两个探索方向。
