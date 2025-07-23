# Android-LinkBandSDK

LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 위한 Android SDK입니다.

## 주요 기능

### 📡 Bluetooth 연결
- LinkBand 디바이스 자동 스캔 및 연결
- 자동 재연결 기능
- 연결 상태 실시간 모니터링

### 📊 센서 데이터 수집

#### EEG (뇌전도)
- 2채널 원시 신호(raw data)
- 전압 변환값 (µV 단위)
- 전극 접촉 상태 정보

#### PPG (광전 용적 맥파)
- 적외선(IR) 및 적색(RED) 신호

#### ACC (가속도계)
- 3축(x, y, z) 원시값
- 움직임 모드 전환 기능 지원

#### 배터리
- 잔량 모니터링 기능

### 📈 배치 데이터 수집
- 샘플 수 기반 수집
- 시간 기반 수집 (초/분)
- 센서별 개별 설정
- 실시간 모니터링

### 💾 데이터 관리
- CSV 형식으로 센서 데이터 저장

## 기술 스택

- **언어**: Kotlin
- **최소 지원 버전**: Android API 34 (Android 14.0)+
- **Java 버전**: 17

## 프로젝트 구조

```
Android-LinkBandSDK/
├── src/main/java/com/example/linkbandsdk/
│   ├── LinkBandSdk.kt           # 메인 SDK 클래스
│   ├── BleManager.kt            # Bluetooth Low Energy 관리
│   ├── SensorData.kt            # 센서 데이터 모델
│   ├── SensorDataParser.kt      # 센서 데이터 파싱
│   ├── SensorConfiguration.kt   # 센서 설정 관리
│   └── TimeBatchManager.kt      # 배치 데이터 수집 관리
├── build.gradle.kts             # 빌드 설정
├── consumer-rules.pro           # ProGuard 규칙
└── proguard-rules.pro           # ProGuard 규칙
```

## 설치 및 사용

### gradle.properties 설정

기본 파일에 아래 코드 추가:

```properties
# Java 17 for Android Gradle Plugin
org.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# LinkBand SDK settings
sdkGroupId = io.github.looxidlabs
sdkArtifactId = SDK-Android
sdkVersion = 1.0.0
```

### 의존성 추가

app 폴더 안의 build.gradle.kts 파일에 다음 의존성을 추가하세요:

```gradle
dependencies {
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // LinkBand SDK from Maven Central
    implementation("io.github.jackorea:linkband-sdk:1.0.0")
}
```

### 권한 설정

AndroidManifest.xml에 다음 권한을 추가하세요:

```xml
<!-- BLE 권한 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Android 12+ 권한 -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- 외부 저장소 권한 (내장 저장공간 접근용) -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- BLE 기능 필요 -->
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
```

`<application>` 태그 안에 다음 FileProvider를 추가하세요:

```xml
<!-- FileProvider for sharing files -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### file_paths.xml 생성 및 설정

#### 아래 경로에 안에 file_paths.xml 생성:
***../app/src/main/res/xml***

#### 하단의 설정 복사 후 붙여넣기:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="external_files" path="." />
    <external-path name="downloads" path="Download/" />
</paths> 
```

### 기본 사용법

```kotlin
// SDK 초기화
val linkBandSdk = LinkBandSdk()

// Bluetooth 스캔 시작
linkBandSdk.startScan { devices ->
    // 발견된 디바이스 목록 처리
}

// 디바이스 연결
linkBandSdk.connect(deviceAddress) { isConnected ->
    if (isConnected) {
        // 연결 성공
        startDataCollection()
    }
}

// 센서 데이터 수집 시작
fun startDataCollection() {
    linkBandSdk.startDataCollection(
        eegEnabled = true,
        ppgEnabled = true,
        accEnabled = true
    ) { sensorData ->
        // 실시간 센서 데이터 처리
    }
}
```


## API 참조

### LinkBandSdk

메인 SDK 클래스로 Bluetooth 연결 및 센서 데이터 수집을 관리합니다.

#### 주요 메서드

- `startScan(callback: (List<BluetoothDevice>) -> Unit)`: Bluetooth 디바이스 스캔 시작
- `connect(address: String, callback: (Boolean) -> Unit)`: 디바이스 연결
- `disconnect()`: 디바이스 연결 해제
- `startDataCollection(config: SensorConfiguration, callback: (SensorData) -> Unit)`: 센서 데이터 수집 시작
- `stopDataCollection()`: 센서 데이터 수집 중지

### BleManager

Bluetooth Low Energy 연결을 관리하는 클래스입니다.

### SensorData

센서 데이터를 담는 데이터 클래스입니다.

#### 속성

- `eegData`: EEG 센서 데이터
- `ppgData`: PPG 센서 데이터  
- `accData`: 가속도계 데이터
- `batteryLevel`: 배터리 잔량
- `timestamp`: 데이터 수집 시간

### SensorConfiguration

센서 설정을 관리하는 클래스입니다.

#### 설정 옵션

- `eegEnabled`: EEG 센서 활성화 여부
- `ppgEnabled`: PPG 센서 활성화 여부
- `accEnabled`: 가속도계 활성화 여부
- `batchSize`: 배치 데이터 수집 크기
- `collectionInterval`: 데이터 수집 간격

## 요구사항

- Android Studio Arctic Fox 이상
- Java 17 이상
- Android 14.0 (API 34) 이상의 실제 디바이스
- Bluetooth 기능 지원 디바이스

---

Copyright ⓒ 룩시드랩스 All rights reserved.
