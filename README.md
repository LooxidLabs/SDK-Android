# Android-LinkBandSDK

LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 위한 Android SDK입니다.

> 데모 앱을 사용해보고 싶다면 아래 링크를 참고하세요:  

🔗 https://github.com/LooxidLabs/Android-LinkBandDemoApp.git  

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


## 요구사항

- Android Studio Arctic Fox 이상
- Java 17 이상
- Android 14.0+ (API 34) 이상의 실제 디바이스
- Bluetooth 기능 지원 디바이스


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

---

## LinkBand SDK 함수 설명

LinkBand SDK를 사용하기 위한 핵심 함수들을 카테고리별로 정리했습니다. 각 함수의 용도와 사용 시점을 명확하게 설명합니다.

### 1. 기본 연결 관리

#### 📡 블루투스 스캔
- **`sdk.startScan()`**
  - 용도: LinkBand 디바이스 검색 시작
  - 사용 시점: 연결할 디바이스를 찾고 싶을 때
  - 결과: `scannedDevices`에 발견된 디바이스 목록 업데이트

- **`sdk.stopScan()`**
  - 용도: 디바이스 검색 중지
  - 사용 시점: 원하는 디바이스를 찾았거나 스캔을 멈추고 싶을 때

- **`sdk.isScanning`**
  - 용도: 현재 스캔 중인지 확인
  - 타입: `StateFlow<Boolean>`
  - 사용 시점: UI에서 스캔 상태를 표시할 때

#### 🔗 디바이스 연결
- **`sdk.connectToDevice(device: BluetoothDevice)`**
  - 용도: 특정 LinkBand 디바이스에 연결
  - 사용 시점: 스캔으로 찾은 디바이스에 연결하고 싶을 때
  - 파라미터: `device` - 연결할 블루투스 디바이스 객체

- **`sdk.disconnect()`**
  - 용도: 현재 연결된 디바이스와의 연결 해제
  - 사용 시점: 연결을 끊고 싶을 때

- **`sdk.isConnected`**
  - 용도: 디바이스 연결 상태 확인
  - 타입: `StateFlow<Boolean>`
  - 사용 시점: UI에서 연결 상태를 표시할 때

#### 🔄 자동 재연결
- **`sdk.enableAutoReconnect()`**
  - 용도: 자동 재연결 기능 활성화
  - 사용 시점: 연결이 끊어져도 자동으로 다시 연결하고 싶을 때

- **`sdk.disableAutoReconnect()`**
  - 용도: 자동 재연결 기능 비활성화
  - 사용 시점: 수동으로만 연결을 관리하고 싶을 때

### 2. 센서 데이터 수집

#### 🎯 센서 선택
- **`sdk.selectSensor(sensor: SensorType)`**
  - 용도: 사용할 센서 선택
  - 파라미터: `SensorType.EEG`, `SensorType.PPG`, `SensorType.ACC` 중 선택
  - 사용 시점: 데이터를 수집하고 싶은 센서를 지정할 때

- **`sdk.deselectSensor(sensor: SensorType)`**
  - 용도: 선택된 센서 해제
  - 사용 시점: 특정 센서의 데이터 수집을 중단하고 싶을 때

#### ▶️ 센서 활성화
- **`sdk.startSelectedSensors()`**
  - 용도: 선택된 센서들의 데이터 수집 시작
  - 사용 시점: 실시간 센서 데이터를 받기 시작하고 싶을 때
  - 주의: 센서를 먼저 선택한 후 호출해야 함

- **`sdk.stopSelectedSensors()`**
  - 용도: 센서 데이터 수집 중지
  - 사용 시점: 데이터 수집을 멈추고 싶을 때

#### 📊 센서 데이터 수신
- **`sdk.eegData`**
  - 용도: EEG(뇌파) 데이터 수신
  - 타입: `StateFlow<List<EegData>>`
  - 포함 정보: 타임스탬프, 채널1/2 전압값(µV), 전극 접촉 상태

- **`sdk.ppgData`**
  - 용도: PPG(맥파) 데이터 수신
  - 타입: `StateFlow<List<PpgData>>`
  - 포함 정보: 타임스탬프, 적색광(red), 적외선(ir) 신호값

- **`sdk.accData`**
  - 용도: 가속도계 데이터 수신
  - 타입: `StateFlow<List<AccData>>`
  - 포함 정보: 타임스탬프, X/Y/Z축 가속도 값

- **`sdk.batteryData`**
  - 용도: 배터리 상태 정보 수신
  - 타입: `StateFlow<BatteryData?>`
  - 포함 정보: 배터리 레벨(0-100%)

### 3. 데이터 기록

#### 💾 CSV 파일 저장
- **`sdk.startRecording()`**
  - 용도: 센서 데이터를 CSV 파일로 저장 시작
  - 사용 시점: 데이터를 파일로 기록하고 싶을 때
  - 저장 위치: `/Download/LinkBand/` 폴더

- **`sdk.stopRecording()`**
  - 용도: CSV 파일 저장 중지
  - 사용 시점: 기록을 멈추고 싶을 때

- **`sdk.isRecording`**
  - 용도: 현재 기록 중인지 확인
  - 타입: `StateFlow<Boolean>`
  - 사용 시점: UI에서 기록 상태를 표시할 때

### 4. 고급 기능 (배치 데이터 수집)

#### ⚙️ 수집 모드 설정
- **`sdk.setCollectionMode(mode: CollectionMode)`**
  - 용도: 데이터 수집 방식 변경
  - 파라미터: `SAMPLE_COUNT`(샘플 수), `SECONDS`(초), `MINUTES`(분)
  - 사용 시점: 배치 단위로 데이터를 수집하고 싶을 때

#### 📈 센서별 배치 설정
- **`sdk.updateSensorSampleCount(sensor, count, text)`**
  - 용도: 센서별 목표 샘플 수 설정
  - 사용 시점: 특정 개수만큼 데이터를 모아서 처리하고 싶을 때

- **`sdk.updateSensorSeconds(sensor, seconds, text)`**
  - 용도: 센서별 수집 시간(초) 설정
  - 사용 시점: 일정 시간 동안의 데이터를 모아서 처리하고 싶을 때

- **`sdk.updateSensorMinutes(sensor, minutes, text)`**
  - 용도: 센서별 수집 시간(분) 설정
  - 사용 시점: 장시간 데이터를 모아서 처리하고 싶을 때

#### 📦 배치 데이터 수신
- **`sdk.eegBatchData`**
  - 용도: 설정된 조건에 따라 모아진 EEG 데이터 수신
  - 타입: `StateFlow<List<EegData>>`

- **`sdk.ppgBatchData`**
  - 용도: 설정된 조건에 따라 모아진 PPG 데이터 수신
  - 타입: `StateFlow<List<PpgData>>`

- **`sdk.accBatchData`**
  - 용도: 설정된 조건에 따라 모아진 ACC 데이터 수신
  - 타입: `StateFlow<List<AccData>>`

### 5. 상태 정보

#### ℹ️ 실시간 상태 확인
- **`sdk.scannedDevices`**
  - 용도: 스캔으로 발견된 디바이스 목록
  - 타입: `StateFlow<List<BluetoothDevice>>`

- **`sdk.connectedDeviceName`**
  - 용도: 현재 연결된 디바이스 이름
  - 타입: `StateFlow<String?>`

- **`sdk.selectedSensors`**
  - 용도: 현재 선택된 센서 목록
  - 타입: `StateFlow<Set<SensorType>>`

- **`sdk.isReceivingData`**
  - 용도: 현재 센서 데이터를 수신 중인지 확인
  - 타입: `StateFlow<Boolean>`

- **`sdk.isAutoReconnectEnabled`**
  - 용도: 자동 재연결 기능 활성화 상태
  - 타입: `StateFlow<Boolean>`

---
## 🔧 설치 및 사용

### 안드로이드 스튜디오 프로젝트 생성

**프로젝트 생성:**

> New Project -> Empty activity

**설정:**

> Minimum SDK -> API 34 ("UpsideDownCake"; Android 14.0)  

> Build configuration language -> Kotlin DSL (build.gradle.kts) [Recommended]

### gradle.properties 설정

**목적**: Java 17 환경 설정 및 SDK 메타데이터 정의

#### JDK 17 설치 및 설정

1. **JDK 17 설치** (macOS 기준)
   ```bash
   # Homebrew를 사용한 설치
   brew install openjdk@17
   ```

2. **Java Home 위치 확인**
   ```bash
   # Java 17 설치 경로 확인
   /usr/libexec/java_home -v 17
   ```

3. **gradle.properties 파일 업데이트**
   > 위 명령어로 확인된 경로를 사용하여 기본 파일에 아래 코드 추가  

   > ⚠️ **주의사항** : sdkVersion은 반드시 최신 버전으로 적용해야 합니다.  
   최신 버전은 아래 링크에서 확인할 수 있습니다.  
   🔗 https://central.sonatype.com/search?q=io.github.looxidlabs 

```properties
# Java 17 for Android Gradle Plugin
# 사용자마다 다른 java.home 위치를 확인하고 업데이트
org.gradle.java.home=/usr/libexec/java_home -v 17 명령어로 확인된 경로

# LinkBand SDK settings
sdkGroupId = io.github.looxidlabs
sdkArtifactId = SDK-Android
sdkVersion = 1.0.1 #⚠️ 최신 버전 적용
```

**예시** (Homebrew 설치 시):
```properties
# Java 17 for Android Gradle Plugin
org.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# LinkBand SDK settings
sdkGroupId = io.github.looxidlabs
sdkArtifactId = SDK-Android
sdkVersion = 1.0.1 #⚠️ 최신 버전 적용
```

### build.gradle.kts 의존성 추가

**목적**: Jetpack Compose, 권한 관리, 코루틴, LinkBand SDK 라이브러리 추가

> **app** 폴더 안의 build.gradle.kts 파일에 다음 의존성을 추가하세요:

> ⚠️ **주의사항** : gradle.properties 설정과 마찬가지로 최신 버전의 SDK를 적용해야 합니다. 

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
    implementation("io.github.looxidlabs:SDK-Android:1.0.1") #⚠️ 최신 버전 적용
}
```

### AndroidManifest.xml 권한 설정

**목적**: 블루투스 통신, 위치 접근, 파일 저장을 위한 필수 권한 및 FileProvider 설정

`<manifest>` 태그 안에 다음 권한을 추가하세요:

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

**목적**: FileProvider를 통한 안전한 파일 공유 경로 정의

#### 하단의 경로에 file_paths.xml 생성:
> ***../yourProjectName/app/src/main/res/xml***

#### file_paths.xml파일에 하단의 설정 복사 후 붙여넣기:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="external_files" path="." />
    <external-path name="downloads" path="Download/" />
</paths> 
```
---
## 📄 샘플 코드 파일

### ⚠️ 주의사항

샘플 코드를 사용할 때는 다음 사항을 확인해주세요:

1. **패키지명 및 import 경로 수정**: 
   - 모든 파일의 **package**선언과 **import**문에 있는 경로는 샘플 기준 **(com.example.yourProjectName)** 으로 되어 있으므로, 실제 프로젝트명에 맞게 변경해야 정상적으로 빌드됩니다.

   - 예를 들어, 프로젝트명이 **happyProject**이라면,  
   **com.example.yourProjectName** → **com.example.happyProject**으로 수정해야 합니다.

2. **파일 위치 지정**:
   - 파일들을 적절한 패키지 구조에 맞게 생성해야 합니다
   - 하단의 경로에 **LinkBand-App.kt** 생성  
   
> ***../yourProjectName/app/src/main/java/com/example/yourProjectName/ui***

### 1. MainActivity.kt
**역할**: 앱의 진입점, 권한 관리, Navigation Compose 기반 다중 화면 관리

**주요 기능**:
- 블루투스 및 위치 권한 요청
- Navigation Compose를 통한 화면 전환 (scan, data, files, csvViewer)
- LinkBandSdk 인스턴스 관리 및 생명주기 관리
- 각 화면에 필요한 상태 및 콜백 전달
- 권한이 없는 경우 권한 요청 UI 표시

**화면 구성**:
- **ScanScreen**: 블루투스 디바이스 스캔 및 연결 화면
- **DataScreen**: 센서 데이터 표시 및 제어 화면
- **FileListScreen**: CSV 파일 목록 표시 화면
- **CsvViewerScreen**: CSV 파일 내용 뷰어 화면

## 기본 설정 - 코드 예시

```kotlin
package com.example.newtest

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newtest.ui.LinkBandScannerScreen
import com.example.newtest.ui.LinkBandDataScreen
import com.example.newtest.ui.theme.NewTestTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.looxidlabs.sdkandroid.*

class MainActivity : ComponentActivity() {
    private lateinit var sdk: LinkBandSdk
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk = LinkBandSdk(this)
        enableEdgeToEdge()
        setContent {
            NewTestTheme {
                val navController = rememberNavController()
                
                // BLE 권한 요청
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } else {
                    listOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
                
                val permissionState = rememberMultiplePermissionsState(permissions)
                
                LaunchedEffect(permissionState.allPermissionsGranted) {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
                
                if (permissionState.allPermissionsGranted) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "scan",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("scan") {
                                LinkBandScannerScreen(
                                    sdk = sdk,
                                    onDataScreenClick = {
                                        navController.navigate("data") {
                                            popUpTo("scan") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable("data") {
                                LinkBandDataScreen(
                                    sdk = sdk
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::sdk.isInitialized) {
            sdk.cleanup()
        }
    }
}
```


### 2. LinkBand-App.kt
**역할**: Jetpack Compose UI 컴포넌트

**주요 기능**:
- LinkBandScannerScreen: 디바이스 스캔 및 연결 UI
- LinkBandDataScreen: 센서 데이터 표시 및 제어 UI
- 공통 컴포넌트: DeviceItem, SensorDataCard, ReceivingIndicator
- 개별 센서 시작 상태 관리 (isEegStarted, isPpgStarted, isAccStarted)
- 가속도계 모드 설정 (RAW/MOTION)
- 배치 데이터 수집 모드 설정
- 파일 관리 및 CSV 뷰어 기능

**샘플 코드**:
```kotlin
/**
 * LinkBand-App.kt - LinkBand 통합 애플리케이션 UI 컴포넌트
 * 
 * 이 파일은 LinkBand 디바이스와의 블루투스 연결, 센서 데이터 수신 및 표시,
 * CSV 기록 기능을 제공하는 Jetpack Compose UI 컴포넌트들을 포함합니다.
 * 
 * 주요 컴포넌트:
 * - LinkBandScannerScreen: 블루투스 디바이스 스캔 및 연결 화면
 * - LinkBandDataScreen: 센서 데이터 표시 및 제어 화면
 * - DeviceItem: 개별 디바이스 연결 관리 컴포넌트
 * - SensorDataCard: 센서 데이터 표시 카드 컴포넌트
 * - ReceivingIndicator: 데이터 수신 상태 표시 컴포넌트
 */
package com.example.newtest.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import io.github.looxidlabs.sdkandroid.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import android.util.Log

/**
 * LinkBand 스캐너 화면
 * 
 * 블루투스 디바이스를 스캔하고 LinkBand 디바이스와 연결을 관리하는 화면입니다.
 * 
 * @param sdk LinkBandSdk 인스턴스 - 블루투스 연결 및 디바이스 관리
 * @param onDataScreenClick 데이터 화면으로 이동하는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBandScannerScreen(
    sdk: LinkBandSdk,
    onDataScreenClick: () -> Unit = {}
) {
    // UI 상태 관리를 위한 StateFlow 값들
    val scannedDevices by sdk.scannedDevices.collectAsState(initial = emptyList())
    val isScanning by sdk.isScanning.collectAsState(initial = false)
    val isConnected by sdk.isConnected.collectAsState(initial = false)
    val connectedDeviceName by sdk.connectedDeviceName.collectAsState(initial = null)
    val isAutoReconnectEnabled by sdk.isAutoReconnectEnabled.collectAsState(initial = false)
    
    // 연결 상태가 변경되면 자동으로 데이터 표시 페이지로 이동
    LaunchedEffect(isConnected) {
        if (isConnected) {
            onDataScreenClick()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 화면 제목
        Text(
            text = "LinkBand 블루투스 스캐너",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 자동 재연결 설정 카드
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "자동 재연결",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isAutoReconnectEnabled) 
                            "연결이 끊어지면 자동으로 재연결됩니다" 
                        else 
                            "수동으로 재연결해야 합니다",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 자동 재연결 토글 스위치
                Switch(
                    checked = isAutoReconnectEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            sdk.enableAutoReconnect()
                        } else {
                            sdk.disableAutoReconnect()
                        }
                    }
                )
            }
        }
        
        // 블루투스 스캔 시작/중지 버튼
        Button(
            onClick = if (isScanning) sdk::stopScan else sdk::startScan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isScanning) "스캔 중지" else "스캔 시작")
        }
        
        // 스캔 상태 표시
        if (isScanning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Text("디바이스를 검색 중...")
            }
        }
        
        // 발견된 디바이스 목록 카드
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "발견된 디바이스 (${scannedDevices.size}개)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 디바이스가 없을 때 안내 메시지
                if (scannedDevices.isEmpty()) {
                    Text(
                        text = "디바이스를 찾을 수 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // 디바이스 목록을 LazyColumn으로 표시
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scannedDevices) { device ->
                            DeviceItem(
                                device = device,
                                isConnected = isConnected,
                                onConnect = { sdk.connectToDevice(device) },
                                onDisconnect = { sdk.disconnect() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * LinkBand 데이터 화면
 * 
 * 연결된 LinkBand 디바이스로부터 수신된 센서 데이터를 표시하고
 * 센서 제어 및 CSV 기록 기능을 제공하는 화면입니다.
 * 
 * @param sdk LinkBandSdk 인스턴스 - 센서 데이터 및 제어 관리
 * @param onDisconnect 연결 해제 시 호출되는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBandDataScreen(
    sdk: LinkBandSdk,
    onDisconnect: () -> Unit = {}
) {
    // UI 상태 관리를 위한 StateFlow 값들
    val isConnected by sdk.isConnected.collectAsState(initial = false)
    val selectedSensors by sdk.selectedSensors.collectAsState(initial = emptySet())
    val isReceivingData by sdk.isReceivingData.collectAsState(initial = false)
    val eegData by sdk.eegData.collectAsState(initial = emptyList())
    val ppgData by sdk.ppgData.collectAsState(initial = emptyList())
    val accData by sdk.accData.collectAsState(initial = emptyList())
    val batteryData by sdk.batteryData.collectAsState(initial = null)
    val isRecording by sdk.isRecording.collectAsState(initial = false)
    val connectedDeviceName by sdk.connectedDeviceName.collectAsState(initial = null)
    val isEegStarted by sdk.isEegStarted.collectAsState(initial = false)
    val isPpgStarted by sdk.isPpgStarted.collectAsState(initial = false)
    val isAccStarted by sdk.isAccStarted.collectAsState(initial = false)
    
    // 수집 시작 시점의 선택된 센서 스냅샷 (UI 표시용)
    var startedSensors by remember { mutableStateOf<Set<SensorType>>(emptySet()) }
    
    // 센서 활성화 요청 상태 (버튼 클릭 시점)
    var activationRequested by remember { mutableStateOf(false) }
    
    // 수집 시작/중지 시점에 스냅샷 갱신
    LaunchedEffect(isReceivingData) {
        if (isReceivingData) {
            startedSensors = selectedSensors.toSet()
            activationRequested = false
        } else {
            startedSensors = emptySet()
            activationRequested = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text("LinkBand 데이터")
            }
        )
        
        // 메인 콘텐츠 영역
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 연결 상태 표시 카드
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (isConnected && connectedDeviceName != null) {
                                "$connectedDeviceName 연결됨"
                            } else {
                                "연결되지 않음"
                            },
                            fontWeight = FontWeight.Medium
                        )
                        if (isConnected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "샘플링 레이트 \n EEG 250Hz \n PPG 50Hz \n ACC 25Hz",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 배터리 정보 표시 카드
            batteryData?.let { battery ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                battery.level > 50 -> MaterialTheme.colorScheme.primaryContainer
                                battery.level > 20 -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "배터리",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${battery.level}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            
            // 센서 선택 및 제어 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "센서 선택",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // 센서 선택 체크박스들 (EEG, PPG, ACC)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // EEG 센서 선택
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedSensors.contains(SensorType.EEG),
                                    onCheckedChange = { checked ->
                                        if (checked) sdk.selectSensor(SensorType.EEG) 
                                        else sdk.deselectSensor(SensorType.EEG)
                                    }
                                )
                                Column {
                                    Text("EEG")
                                    // 수신 중이지만 시작되지 않은 센서에 대한 표시
                                    if ((activationRequested || isReceivingData) && selectedSensors.contains(SensorType.EEG) && !startedSensors.contains(SensorType.EEG)) {
                                        ReceivingIndicator()
                                    }
                                }
                            }
                            
                            // PPG 센서 선택
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedSensors.contains(SensorType.PPG),
                                    onCheckedChange = { checked ->
                                        if (checked) sdk.selectSensor(SensorType.PPG) 
                                        else sdk.deselectSensor(SensorType.PPG)
                                    }
                                )
                                Column {
                                    Text("PPG")
                                    if ((activationRequested || isReceivingData) && selectedSensors.contains(SensorType.PPG) && !startedSensors.contains(SensorType.PPG)) {
                                        ReceivingIndicator()
                                    }
                                }
                            }
                            
                            // ACC 센서 선택
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedSensors.contains(SensorType.ACC),
                                    onCheckedChange = { checked ->
                                        if (checked) sdk.selectSensor(SensorType.ACC) 
                                        else sdk.deselectSensor(SensorType.ACC)
                                    }
                                )
                                Column {
                                    Text("ACC")
                                    if ((activationRequested || isReceivingData) && selectedSensors.contains(SensorType.ACC) && !startedSensors.contains(SensorType.ACC)) {
                                        ReceivingIndicator()
                                    }
                                }
                            }
                        }
                        
                        // 센서 활성화/비활성화 버튼
                        Button(
                            onClick = {
                                if (isReceivingData) {
                                    sdk.stopSelectedSensors()
                                } else {
                                    activationRequested = true
                                    sdk.startSelectedSensors()
                                }
                            },
                            enabled = selectedSensors.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReceivingData) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isReceivingData) "센서 비활성화" else "센서 활성화")
                        }
                    }
                }
            }
            
            // EEG 데이터 표시 카드 (센서가 활성화된 경우에만)
            if (startedSensors.contains(SensorType.EEG)) {
                item {
                    SensorDataCard(
                        title = "EEG 데이터",
                        content = {
                            if (eegData.isNotEmpty()) {
                                // 최근 3개의 EEG 데이터 표시
                                val latest = eegData.takeLast(3)
                                latest.forEach { data ->
                                    Text(
                                        text = "timestamp: ${data.timestamp.time}, ch1Raw: ${data.ch1Raw}, ch2Raw: ${data.ch2Raw}, ch1uV: ${data.channel1.roundToInt()}µV, ch2uV: ${data.channel2.roundToInt()}µV, leadOff: ${if (data.leadOff) "1" else "0"}",
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "EEG 데이터를 수신하지 못했습니다",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            // PPG 데이터 표시 카드 (센서가 활성화된 경우에만)
            if (startedSensors.contains(SensorType.PPG)) {
                item {
                    SensorDataCard(
                        title = "PPG 데이터",
                        content = {
                            if (ppgData.isNotEmpty()) {
                                // 최근 3개의 PPG 데이터 표시
                                val latest = ppgData.takeLast(3)
                                latest.forEach { data ->
                                    Text(
                                        text = "timestamp: ${data.timestamp.time}, red: ${data.red}, ir: ${data.ir}",
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "PPG 데이터를 수신하지 못했습니다",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            // ACC 데이터 표시 카드 (센서가 활성화된 경우에만)
            if (startedSensors.contains(SensorType.ACC)) {
                item {
                    SensorDataCard(
                        title = "ACC 데이터",
                        content = {
                            if (accData.isNotEmpty()) {
                                // 최근 3개의 ACC 데이터 표시
                                val latest = accData.takeLast(3)
                                latest.forEach { data ->
                                    Text(
                                        text = "timestamp: ${data.timestamp.time}, x: ${data.x}, y: ${data.y}, z: ${data.z}",
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "ACC 데이터를 수신하지 못했습니다",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            // CSV 기록 제어 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CSV 기록 제어",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // CSV 기록 시작/중지 버튼
                        Button(
                            onClick = {
                                if (isRecording) {
                                    sdk.stopRecording()
                                } else {
                                    sdk.startRecording()
                                }
                            },
                            enabled = isConnected && isReceivingData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) 
                                    MaterialTheme.colorScheme.error 
                                else if (isReceivingData)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isRecording) "기록 중지" else "CSV 기록 시작")
                        }
                        
                        // CSV 파일 저장 경로 안내
                        Text(
                            text = "저장 경로 : 내 파일 -> 내장 저장공간 -> Download -> LinkBand",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // 기록 상태 표시
                        Text(
                            text = "기록 상태: ${if (isRecording) "기록 중" else "기록 중지됨"}",
                            fontSize = 14.sp
                        )
                        
                        // 기록 중일 때 안내 메시지
                        if (isRecording) {
                            Text(
                                text = "데이터가 실시간으로 CSV 파일에 저장되고 있습니다",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 연결 해제 버튼
            item {
                Button(
                    onClick = { 
                        sdk.disconnect()
                        onDisconnect()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("연결 해제")
                }
            }
        }
    }
}

/**
 * 공통 컴포저블들
 */

/**
 * 디바이스 아이템 컴포넌트
 * 
 * 스캔된 블루투스 디바이스를 표시하고 연결/해제 기능을 제공합니다.
 * 
 * @param device 블루투스 디바이스 객체
 * @param isConnected 현재 연결 상태
 * @param onConnect 연결 버튼 클릭 시 호출되는 콜백
 * @param onDisconnect 연결 해제 버튼 클릭 시 호출되는 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 디바이스 정보 표시
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name ?: "알 수 없는 디바이스",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = device.address,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // 연결/해제 버튼
            Button(
                onClick = if (isConnected) onDisconnect else onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (isConnected) "연결 해제" else "연결")
            }
        }
    }
}

/**
 * 센서 데이터 카드 컴포넌트
 * 
 * 센서 데이터를 표시하는 재사용 가능한 카드 컴포넌트입니다.
 * 
 * @param title 카드 제목
 * @param content 카드 내용을 구성하는 컴포저블
 */
@Composable
fun SensorDataCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            content()
        }
    }
}

/**
 * 데이터 수신 상태 표시 컴포넌트
 * 
 * 센서에서 데이터를 수신 중일 때 애니메이션 효과와 함께 표시됩니다.
 * "수신중..." 텍스트에 점이 0.5초마다 추가/제거되는 애니메이션을 제공합니다.
 */
@Composable
fun ReceivingIndicator() {
    // 점 개수 상태 관리
    var dotCount by remember { mutableStateOf(1) }
    
    // 애니메이션 효과를 위한 LaunchedEffect
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // 0.5초 대기
            dotCount = (dotCount % 3) + 1 // 1, 2, 3 순환
        }
    }
    
    Text(
        text = "수신중" + ".".repeat(dotCount),
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
} 
```

---

Copyright ⓒ 룩시드랩스 All rights reserved.
