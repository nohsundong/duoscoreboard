# Duo Scoreboard (듀오 스코어보드)

100% 오프라인으로 동작하는 2인용 점수판 안드로이드 앱. Kotlin + Jetpack Compose, Room, DataStore.

**권한 선언 0건.** 인터넷 접근 불가, 광고/애널리틱스/트래킹 SDK 없음, 개인정보 수집 코드 없음.

---

## 1. 프로젝트 구조

```
OfflineDuoScoreboard/
├─ settings.gradle.kts            루트 설정 (google/mavenCentral 저장소만)
├─ build.gradle.kts               플러그인 선언
├─ gradle/libs.versions.toml      버전 카탈로그 (모든 의존성 버전 한 곳에서 관리)
├─ gradle.properties
├─ local.properties               sdk.dir (VCS 제외)
├─ gradlew / gradlew.bat          Gradle 8.14.3 래퍼
└─ app/
   ├─ build.gradle.kts            compileSdk/targetSdk 36, minSdk 24, R8 + 리소스 축소
   ├─ proguard-rules.pro
   └─ src/
      ├─ main/
      │  ├─ AndroidManifest.xml   uses-permission 없음 + 병합 방지 tools:node="remove"
      │  ├─ java/com/myowndime/duoscoreboard/
      │  │  ├─ MainActivity.kt              edge-to-edge, 테마/화면유지 적용
      │  │  ├─ DuoScoreboardApp.kt          Application, AppContainer 보유
      │  │  ├─ di/AppContainer.kt           수동 DI (DI 프레임워크 미사용)
      │  │  ├─ domain/
      │  │  │  ├─ model/       Player, AppSettings, MatchRecord, MatchResult
      │  │  │  └─ repository/  SettingsRepository, MatchRepository (인터페이스)
      │  │  ├─ data/
      │  │  │  ├─ local/       Room: AppDatabase, MatchRecordDao, MatchRecordEntity
      │  │  │  └─ repository/  RoomMatchRepository, DataStoreSettingsRepository
      │  │  └─ ui/
      │  │     ├─ AppViewModelProvider.kt   ViewModel 팩토리
      │  │     ├─ navigation/               NavHost (scoreboard / history / settings)
      │  │     ├─ theme/                    라이트·다크 색상, 타이포, 플레이어 팔레트
      │  │     ├─ common/                   ConfirmDialog, KeepScreenOn
      │  │     ├─ scoreboard/               ScoreboardScreen/Route/ViewModel, PlayerPanel
      │  │     ├─ history/                  HistoryScreen/Route/ViewModel
      │  │     └─ settings/                 SettingsScreen/Route/ViewModel
      │  └─ res/
      │     ├─ values/ values-ko/ values-night/    문자열(en/ko), 색상, 테마
      │     ├─ drawable/ mipmap-*/                 적응형 아이콘 + 레거시 PNG
      │     └─ xml/                                backup_rules, data_extraction_rules
      ├─ test/          JVM + Robolectric 테스트 32개
      └─ androidTest/   실기기용 계측 테스트
```

## 2. 기능

| 요구사항 | 구현 |
|---|---|
| 점수 +1 / -1 | 패널 탭 = +1, 롱프레스 = -1, 아래로 스와이프 = -1, 위로 스와이프 = +1, 화면의 +/- 버튼(56dp) |
| 이름·색상 커스터마이즈 | 설정 화면에서 이름(최대 16자)·8색 팔레트 선택, DataStore에 로컬 저장 |
| 세트 관리 | 상단바 ★ 버튼 → 확인 다이얼로그 → 앞선 쪽이 세트 획득, 점수는 0:0으로 리셋. 동점이면 거부하고 안내 메시지. 설정에서 목표 점수(11/15/21/25)를 정하면 **2점 차**로 도달 시 자동으로 세트가 넘어감(듀스 지원). 세트는 사용 중일 때만 화면에 표시 |
| 점수 초기화 | 더보기(⋮) → 점수·세트 초기화 → 확인 다이얼로그 |
| 경기 기록 | 상단바 체크 버튼 → 확인 다이얼로그 → Room DB 저장(승자/세트/점수/시각), 기록 화면에서 조회·개별 삭제·전체 삭제(각각 확인 다이얼로그). 승자는 세트 수로 먼저 판정하고, 세트가 같으면 점수로 판정 |
| 점수 리셋 | 하단 버튼 → 확인 다이얼로그 → 점수·세트·좌우 배치 초기화 |
| 좌우교대 | 하단 버튼. 두 패널(과 세트 현황 바)의 위치만 바꾸고 점수·세트·이름은 각자에게 유지. 리셋/경기 저장 시 원래 배치로 복귀 |
| 점수 설정 | 하단 버튼 → 두 플레이어의 점수를 직접 입력(0~999, 3자리 제한). 세트는 건드리지 않고 자동 세트 종료도 일으키지 않음 |
| 000 표시 | 설정 스위치. 점수를 007처럼 세 자리로 표시(최대 999) |
| 공유 | 더보기(⋮) → 공유(현재 점수), 경기 기록 각 항목의 공유 버튼(점수+일시). 시스템 공유 시트에 텍스트만 전달하므로 앱은 네트워크를 쓰지 않음 |
| 음성 점수 안내 | 설정 스위치(기본 꺼짐). 기기에 설치된 TTS 엔진으로 "플레이어 1, 3점" 낭독. 권한 대신 `<queries>` 선언만 사용 |
| 가로/세로 | `BoxWithConstraints`로 세로=상하 2분할, 가로=좌우 2분할. `screenOrientation="fullUser"` |
| 큰 폰트 | 패널 크기에 비례해 점수 글자 크기 자동 계산(자릿수에 따라 축소) |
| 다크/라이트 | 시스템/라이트/다크 3택, DataStore 저장, `values-night` 대응 |

접근성: 모든 아이콘 버튼에 `contentDescription`, 패널에 TalkBack 커스텀 액션(점수 올리기/내리기), 터치 타깃 48dp 이상, 팔레트 색상은 대비를 고려해 텍스트 색을 자동 반전.

## 3. 실행 방법

### Android Studio
1. Android Studio에서 `C:\Users\tbell\Documents\OfflineDuoScoreboard` 를 **Open**.
2. Gradle Sync 후 `app` 실행 구성으로 Run.
3. Android Studio가 쓰는 JDK는 17~21이어야 합니다(이 PC의 기본 JDK 26으로는 AGP가 동작하지 않음).
   File > Settings > Build Tools > Gradle > Gradle JDK 에서 `tools\jdk\jdk-21.0.12+8` 지정.

### 명령줄
JDK 21과 SDK 경로를 지정한 뒤 래퍼를 실행합니다.

```bash
cd /c/Users/tbell/Documents/OfflineDuoScoreboard && JAVA_HOME="$PWD/tools/jdk/jdk-21.0.12+8" ./gradlew assembleDebug
```

PowerShell:

```bash
$env:JAVA_HOME="C:\Users\tbell\Documents\OfflineDuoScoreboard\tools\jdk\jdk-21.0.12+8"; .\gradlew.bat assembleDebug
```

주요 태스크

| 태스크 | 내용 |
|---|---|
| `assembleDebug` | 디버그 APK |
| `testDebugUnitTest` | 유닛 + Robolectric UI 테스트 32개 |
| `lintDebug` / `lintRelease` | 린트 (경고도 에러로 처리) |
| `assembleRelease` | R8 축소 릴리스 APK (약 1.5MB) |
| `bundleRelease` | 스토어 업로드용 AAB (약 3.6MB) |
| `connectedDebugAndroidTest` | 실기기/에뮬레이터 계측 테스트 |

## 4. 검증 결과

- 빌드: `clean build` 성공 (AGP 8.13.2 / Gradle 8.14.3 / Kotlin 2.2.21 / JDK 21)
- 테스트: 103개 전부 통과
  - `ScoreboardViewModelTest` (19) 점수 증감·하한 0·상한 999·초기화·저장·빈 경기 거부·음성 이벤트·세트 획득·동점 거부·목표 점수 자동 세트·듀스·감점 시 세트 미부여
  - `MatchRecordTest` (5) 승자 판정
  - `MatchRecordDaoTest` (5) Room 저장/정렬/개별 삭제/전체 삭제/세트 왕복
  - `MatchRecordMigrationTest` (1) **v1 스키마를 그대로 재현해 v1→v2 마이그레이션 후 기록 보존 검증**
  - `ScoreboardScreenTest` (14) 탭·스와이프·버튼·확인 다이얼로그·가로모드·다크테마·세트 표시·공유 텍스트·더보기 메뉴
  - `HistoryScreenTest` (5) 빈 상태·승자 표시·공유 텍스트·삭제 확인
  - `MainActivitySmokeTest` (4) 실제 Activity + Room + DataStore 기동, 화면 전환
  - `OfflineGuaranteeTest` (2) **병합된 매니페스트에 권한 0건 검증**
- 린트: 에러 0, 경고 0 (`warningsAsErrors = true`)
- 릴리스 APK `aapt2 dump badging` 결과: `uses-permission` 항목 없음

### 실기기 검증 (Galaxy A33 5G / SM-A336N / Android 16)

- `installDebug` 설치 후 실행 — 크래시 없음, 한국어 리소스·다크테마 정상 적용
- 탭 +1, +/- 버튼, 경기 종료 확인 다이얼로그, Room 저장 → 경기 기록 조회, 설정 화면까지 수동 확인
- 세로/가로 전환 시 레이아웃 전환 및 점수 유지 확인
- `connectedDebugAndroidTest` 3개 통과 (설치된 패키지의 권한 0건 검증 포함)
- `dumpsys package` 결과 요청 권한 없음

## 5. 오프라인·개인정보 보증 방식

1. `AndroidManifest.xml` 에 `<uses-permission>` 을 하나도 선언하지 않음.
2. 라이브러리가 권한을 병합해 넣지 못하도록 `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, androidx.core 의 `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` 에 `tools:node="remove"` 를 명시.
3. 의존성은 AndroidX(Compose/Room/DataStore/Navigation/Lifecycle)와 코루틴뿐. 광고·분석·크래시 리포팅 SDK 없음. DI 프레임워크도 쓰지 않아 의존성 표면을 최소화.
4. 수집·저장 데이터는 사용자가 직접 입력한 플레이어 이름, 선택한 색상, 경기 점수와 시각뿐이며 전부 앱 샌드박스에 저장. 광고 ID·기기 ID·위치·연락처 등 식별자에 접근하는 코드 없음.
5. `allowBackup="false"` + 백업 규칙으로 클라우드 백업 경유 유출 차단(기기 간 직접 전송만 허용).
6. `OfflineGuaranteeTest` 가 빌드마다 권한 0건을 자동 검증 → 나중에 라이브러리를 추가해 권한이 딸려 들어오면 즉시 빌드 실패.

## 6. 스토어 등록 전 최종 체크리스트

### 필수 (등록 전 반드시)
- [ ] **패키지명 확정** — 현재 `com.myowndime.duoscoreboard`. 한 번 올리면 변경 불가.
- [ ] **업로드 키스토어 생성** (아래 명령을 직접 실행하고 비밀번호는 안전한 곳에 보관):

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

- [ ] `app/build.gradle.kts` 에 `signingConfigs` 추가하고 키 정보는 `local.properties` 또는 환경변수로 주입(절대 커밋 금지).
- [ ] `versionCode` / `versionName` 확인 후 `bundleRelease` 로 AAB 생성 → Play Console 업로드.
- [ ] 스토어 그래픽 준비: 앱 아이콘 512×512 PNG, 피처 그래픽 1024×500, 스크린샷 최소 2장(폰), 가로모드 지원을 알리려면 태블릿 스크린샷도 권장.

### 정책 관련 (현재 상태 기준 통과 가능)
- [x] **권한** — 0건. Play Console "앱 권한" 섹션에 표시될 항목 없음.
- [x] **targetSdk 36 (Android 16)** — 2026-08-31부터 신규 앱에 요구되는 수준. (참고: SDK Manager 기준 현재 최신 플랫폼도 36)
- [x] **개인정보처리방침** — 개인정보/식별자를 수집·전송하지 않으므로 데이터 안전 섹션에서 "데이터를 수집하지 않음"으로 신고 가능. 다만 Play Console은 **모든 앱에 개인정보처리방침 URL을 요구**하므로, "본 앱은 어떠한 개인정보도 수집하지 않습니다"라는 한 페이지 정적 문서 URL은 준비해야 합니다(앱 내 설정 화면의 개인정보 안내 문구를 그대로 써도 됨).
- [x] **광고 없음** — 광고 포함 여부 "아니요"로 신고.
- [x] **데이터 안전(Data safety) 양식** — 수집/공유 모두 "없음". 기기 내 저장만 함.
- [x] **접근성** — contentDescription, 커스텀 액션, 48dp 터치 타깃, 색 대비 자동 조정 적용.
- [ ] **콘텐츠 등급 설문** — 전체 이용가로 응답 가능(폭력/성인 콘텐츠·사용자 간 소통·위치 공유 없음).
- [ ] **광고 ID 선언** — Play Console에서 "광고 ID 사용 안 함" 선택(AD_ID 권한 제거됨).
- [ ] **원스토어** — 동일 AAB/APK 사용 가능. 원스토어도 targetSdk 최신 요건과 개인정보처리방침 URL을 요구하므로 위와 동일하게 준비.

### 선택 개선
- [x] 실기기에서 `connectedDebugAndroidTest` 실행 — SM-A336N(Android 16)에서 3개 통과.
- [ ] 태블릿·폴더블 레이아웃 스크린샷 확인.
- [ ] `versionCode` 자동 증가 스크립트.

## 7. 알아둘 점

- `tools/` 폴더에는 이 프로젝트 빌드를 위해 내려받은 JDK 21과 Gradle 8.14.3이 들어 있으며 `.gitignore` 처리되어 있습니다. Android SDK는 표준 위치 `%LOCALAPPDATA%\Android\Sdk` 에 설치되어 있어 Android Studio와 그대로 공유됩니다.
- 시스템 기본 JDK는 26이라 AGP와 호환되지 않습니다. 명령줄 빌드 시 위의 `JAVA_HOME` 지정이 필요합니다.
- 계측 테스트(`androidTest`)는 기기나 에뮬레이터가 있어야 실행됩니다. 나머지 32개 테스트는 JVM에서 전부 실행됩니다.
- 앱은 edge-to-edge로 그리므로 화면 하단 안내문에 `safeDrawing` 인셋을 직접 적용합니다. 하단 바에 요소를 추가할 때 같은 처리를 해 주지 않으면 제스처 바에 가려집니다.
- **DB 스키마는 v2입니다.** 세트 컬럼은 `AppDatabase.MIGRATION_1_2`가 `ALTER TABLE ADD COLUMN`으로 추가하므로 기존 사용자의 경기 기록이 보존됩니다. 엔티티를 바꿀 때는 반드시 버전을 올리고 마이그레이션을 추가하세요 — `app/schemas/`의 JSON이 마이그레이션 테스트의 입력으로 쓰이므로 커밋해야 합니다.
- `connectedAndroidTest`를 실행하면 **AGP가 끝난 뒤 앱을 언인스톨**합니다. 기기에 저장해 둔 경기 기록도 함께 사라지니, 실기기 데이터를 유지한 채 확인하려면 계측 테스트 대신 `installDebug`만 쓰세요.
- `room-testing`은 `MigrationTestHelper`가 kotlinx-serialization 1.8을 요구하지만 navigation-compose가 런타임을 1.7.3으로 `strictly` 고정해 계측 테스트에서 `AbstractMethodError`가 납니다. 그래서 마이그레이션 테스트는 export된 스키마 JSON을 직접 재현하는 방식으로 작성했고 추가 의존성이 없습니다.
