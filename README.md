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
   - 하단의 경로에 **MainViewModel.kt** 와 **LinkBand-App.kt** 생성  
   
> ***../yourProjectName/app/src/main/java/com/example/yourProjectName/ui***

### 1. MainActivity.kt
**역할**: 앱의 진입점, 권한 관리, 화면 전환

**주요 기능**:
- 블루투스 및 위치 권한 요청
- 스캐너 화면 ↔ 데이터 화면 전환
- MainViewModel 인스턴스 관리

**샘플 코드**:
```kotlin
/**
 * MainActivity.kt - 기능별 독립 샘플 메인 액티비티
 * 
 * 이 파일은 LinkBand 애플리케이션의 메인 액티비티로, 앱의 진입점 역할을 합니다.
 * 블루투스 및 위치 권한 관리, 화면 전환 로직을 담당하며,
 * Jetpack Compose를 사용하여 UI를 구성합니다.
 * 
 * 주요 기능:
 * - 블루투스 및 위치 권한 요청 및 관리
 * - 스캐너 화면과 데이터 화면 간 전환
 * - MainViewModel 인스턴스 관리
 * - 권한이 없는 경우 권한 요청 UI 표시
 */
package com.example.yourProjectName

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.yourProjectName.ui.*
import com.example.yourProjectName.ui.MainViewModel

/**
 * 메인 액티비티
 * 
 * LinkBand 애플리케이션의 메인 액티비티로, 앱의 전체적인 생명주기를 관리합니다.
 * 블루투스 권한 관리와 화면 전환을 담당하며, MainViewModel을 통해
 * 블루투스 연결 및 센서 데이터 관리를 수행합니다.
 */
class MainActivity : ComponentActivity() {
    // MainViewModel 인스턴스 - 블루투스 연결 및 센서 데이터 관리
    private val viewModel: MainViewModel by viewModels()
    
    /**
     * 액티비티 생성 시 호출되는 메서드
     * 
     * 앱 초기화, 권한 요청, UI 설정을 수행합니다.
     * 
     * @param savedInstanceState 액티비티 상태 저장 데이터
     */
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 버전에 따른 필요한 권한 목록 설정
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) 이상에서 필요한 권한
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,    // 블루투스 스캔 권한
                Manifest.permission.BLUETOOTH_CONNECT, // 블루투스 연결 권한
                Manifest.permission.ACCESS_FINE_LOCATION // 정확한 위치 권한
            )
        } else {
            // Android 11 이하에서 필요한 권한
            listOf(
                Manifest.permission.BLUETOOTH,         // 블루투스 권한
                Manifest.permission.BLUETOOTH_ADMIN,   // 블루투스 관리 권한
                Manifest.permission.ACCESS_FINE_LOCATION // 정확한 위치 권한
            )
        }
        
        // Jetpack Compose UI 설정
        setContent {
            // 다중 권한 상태 관리
            val permissionState = rememberMultiplePermissionsState(permissions)
            
            // 현재 화면 상태 관리 ("scanner" 또는 "data")
            var currentScreen by remember { mutableStateOf("scanner") }
            
            // 권한이 부여되지 않은 경우 자동으로 권한 요청
            LaunchedEffect(permissionState.allPermissionsGranted) {
                if (!permissionState.allPermissionsGranted) {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
            
            // 권한이 모두 부여된 경우 메인 UI 표시
            if (permissionState.allPermissionsGranted) {
                // 현재 화면에 따라 적절한 화면 컴포넌트 표시
                when (currentScreen) {
                    "scanner" -> {
                        // 블루투스 스캐너 화면
                        // 디바이스 연결 시 자동으로 데이터 화면으로 전환
                        LinkBandScannerScreen(
                            viewModel = viewModel,
                            onDataScreenClick = { currentScreen = "data" }
                        )
                    }
                    "data" -> {
                        // 센서 데이터 표시 화면
                        // 연결 해제 시 스캐너 화면으로 돌아감
                        LinkBandDataScreen(
                            viewModel = viewModel,
                            onDisconnect = { currentScreen = "scanner" }
                        )
                    }
                    else -> {
                        // 기본값으로 스캐너 화면 표시
                        LinkBandScannerScreen(
                            viewModel = viewModel,
                            onDataScreenClick = { currentScreen = "data" }
                        )
                    }
                }
            } else {
                // 권한이 부여되지 않은 경우 권한 요청 UI 표시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 권한 필요 안내 텍스트
                    Text(
                        text = "권한이 필요합니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 필요한 권한 설명
                    Text(
                        text = "블루투스 및 위치 권한을 허용해주세요",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 권한 요청 버튼
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() }
                    ) {
                        Text("권한 요청")
                    }
                }
            }
        }
    }
} 
```

### 2. MainViewModel.kt
**역할**: 비즈니스 로직 관리, BleManager 통신

**주요 기능**:
- 블루투스 연결 상태 관리
- 센서 데이터 수신 및 처리
- CSV 기록 제어
- UI 상태 제공

**샘플 코드**:
```kotlin
/**
 * MainViewModel.kt - 기능별 독립 샘플 메인 ViewModel
 * 
 * 이 파일은 LinkBand 애플리케이션의 메인 ViewModel로, 모든 샘플의 공통 기능을 관리합니다.
 * BleManager를 통해 블루투스 연결, 센서 데이터 수신, CSV 기록 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 블루투스 디바이스 스캔 및 연결 관리
 * - 센서 데이터 수신 및 상태 관리
 * - CSV 파일 기록 제어
 * - 자동 재연결 기능
 * - 연결 상태 및 데이터 상태 모니터링
 */
package com.example.yourProjectName.ui

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.looxidlabs.sdkandroid.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 메인 ViewModel - 모든 샘플의 공통 기능 관리
 * 
 * LinkBand 디바이스와의 모든 상호작용을 관리하는 중앙 ViewModel입니다.
 * BleManager를 통해 실제 블루투스 통신을 처리하고, UI에 필요한 상태를 제공합니다.
 * 
 * @param application Android Application 인스턴스
 */
class MainViewModel(application: android.app.Application) : AndroidViewModel(application) {
    // BleManager 인스턴스 - 실제 블루투스 통신 처리
    private val bleManager = BleManager(application)
    
    // ===== 공통 상태들 =====
    
    /**
     * 블루투스 연결 상태
     * true: 연결됨, false: 연결 해제됨
     */
    val isConnected: StateFlow<Boolean> = bleManager.isConnected
    
    /**
     * 연결된 디바이스 이름
     * 연결되지 않은 경우 null
     */
    val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
    
    /**
     * 센서 데이터 수신 상태
     * true: 데이터 수신 중, false: 데이터 수신 안됨
     */
    val isReceivingData: StateFlow<Boolean> = bleManager.isReceivingData
    
    /**
     * 배터리 데이터
     * 디바이스에서 수신된 배터리 정보
     */
    val batteryData: StateFlow<BatteryData?> = bleManager.batteryData
    
    // ===== 블루투스 스캔 관련 상태 =====
    
    /**
     * 스캔된 디바이스 목록
     * 블루투스 스캔으로 발견된 디바이스들의 리스트
     */
    val scannedDevices: StateFlow<List<android.bluetooth.BluetoothDevice>> = bleManager.scannedDevices
    
    /**
     * 스캔 진행 상태
     * true: 스캔 중, false: 스캔 중지됨
     */
    val isScanning: StateFlow<Boolean> = bleManager.isScanning
    
    // ===== 센서 관련 상태 =====
    
    /**
     * 선택된 센서 목록
     * 사용자가 선택한 센서들의 Set (EEG, PPG, ACC)
     */
    val selectedSensors: StateFlow<Set<SensorType>> = bleManager.selectedSensors
    
    /**
     * EEG 데이터 리스트
     * 수신된 EEG 센서 데이터들의 리스트
     */
    val eegData: StateFlow<List<EegData>> = bleManager.eegData
    
    /**
     * PPG 데이터 리스트
     * 수신된 PPG 센서 데이터들의 리스트
     */
    val ppgData: StateFlow<List<PpgData>> = bleManager.ppgData
    
    /**
     * ACC 데이터 리스트
     * 수신된 ACC 센서 데이터들의 리스트
     */
    val accData: StateFlow<List<AccData>> = bleManager.accData
    
    // ===== CSV 기록 관련 상태 =====
    
    /**
     * CSV 기록 상태
     * true: 기록 중, false: 기록 중지됨
     */
    val isRecording: StateFlow<Boolean> = bleManager.isRecording
    
    // ===== 연결 관리 상태 =====
    
    /**
     * 자동 재연결 활성화 상태
     * true: 자동 재연결 활성화, false: 자동 재연결 비활성화
     */
    val isAutoReconnectEnabled: StateFlow<Boolean> = bleManager.isAutoReconnectEnabled
    
    // ===== 블루투스 스캔 기능 =====
    
    /**
     * 블루투스 디바이스 스캔 시작
     * 
     * 주변의 블루투스 디바이스를 검색합니다.
     * 스캔 결과는 scannedDevices StateFlow를 통해 UI에 전달됩니다.
     */
    fun startScan() {
        viewModelScope.launch {
            bleManager.startScan()
        }
    }
    
    /**
     * 블루투스 디바이스 스캔 중지
     * 
     * 진행 중인 블루투스 스캔을 중지합니다.
     * 배터리 절약을 위해 스캔이 완료되면 자동으로 호출됩니다.
     */
    fun stopScan() {
        viewModelScope.launch {
            bleManager.stopScan()
        }
    }
    
    // ===== 디바이스 연결 기능 =====
    
    /**
     * 특정 디바이스에 연결
     * 
     * @param device 연결할 블루투스 디바이스 객체
     * 
     * 선택된 디바이스와 블루투스 연결을 시도합니다.
     * 연결 성공 시 isConnected가 true로 변경됩니다.
     */
    fun connectToDevice(device: android.bluetooth.BluetoothDevice) {
        viewModelScope.launch {
            bleManager.connectToDevice(device)
        }
    }
    
    /**
     * 현재 연결된 디바이스 연결 해제
     * 
     * 현재 연결된 LinkBand 디바이스와의 연결을 해제합니다.
     * 연결 해제 시 isConnected가 false로 변경됩니다.
     */
    fun disconnect() {
        viewModelScope.launch {
            bleManager.disconnect()
        }
    }
    
    // ===== 자동 재연결 기능 =====
    
    /**
     * 자동 재연결 기능 활성화
     * 
     * 연결이 끊어졌을 때 자동으로 재연결을 시도하도록 설정합니다.
     * 네트워크 불안정이나 일시적인 연결 문제를 자동으로 해결합니다.
     */
    fun enableAutoReconnect() {
        viewModelScope.launch {
            bleManager.enableAutoReconnect()
        }
    }
    
    /**
     * 자동 재연결 기능 비활성화
     * 
     * 자동 재연결 기능을 비활성화합니다.
     * 연결이 끊어지면 수동으로 재연결해야 합니다.
     */
    fun disableAutoReconnect() {
        viewModelScope.launch {
            bleManager.disableAutoReconnect()
        }
    }
    
    // ===== 센서 선택 기능 =====
    
    /**
     * 센서 선택
     * 
     * @param sensor 선택할 센서 타입 (EEG, PPG, ACC)
     * 
     * 특정 센서를 활성화할 센서 목록에 추가합니다.
     * 선택된 센서는 selectedSensors StateFlow에 반영됩니다.
     */
    fun selectSensor(sensor: SensorType) {
        viewModelScope.launch {
            bleManager.selectSensor(sensor)
        }
    }
    
    /**
     * 센서 선택 해제
     * 
     * @param sensor 선택 해제할 센서 타입 (EEG, PPG, ACC)
     * 
     * 특정 센서를 활성화할 센서 목록에서 제거합니다.
     * 선택 해제된 센서는 selectedSensors StateFlow에서 제거됩니다.
     */
    fun deselectSensor(sensor: SensorType) {
        viewModelScope.launch {
            bleManager.deselectSensor(sensor)
        }
    }
    
    // ===== 센서 활성화/비활성화 =====
    
    /**
     * 선택된 센서들 활성화
     * 
     * selectedSensors에 포함된 모든 센서를 활성화하여 데이터 수신을 시작합니다.
     * 센서 활성화 시 isReceivingData가 true로 변경됩니다.
     */
    fun startSelectedSensors() {
        viewModelScope.launch {
            bleManager.startSelectedSensors()
        }
    }
    
    /**
     * 선택된 센서들 비활성화
     * 
     * 현재 활성화된 모든 센서를 비활성화하여 데이터 수신을 중지합니다.
     * 센서 비활성화 시 isReceivingData가 false로 변경됩니다.
     */
    fun stopSelectedSensors() {
        viewModelScope.launch {
            bleManager.stopSelectedSensors()
        }
    }
    
    // ===== CSV 기록 기능 =====
    
    /**
     * CSV 기록 시작
     * 
     * 현재 수신 중인 센서 데이터를 CSV 파일로 기록하기 시작합니다.
     * 기록 시작 시 isRecording이 true로 변경됩니다.
     * 파일은 Download/LinkBand 폴더에 저장됩니다.
     */
    fun startRecording() {
        viewModelScope.launch {
            bleManager.startRecording()
        }
    }
    
    /**
     * CSV 기록 중지
     * 
     * 진행 중인 CSV 기록을 중지하고 파일을 저장합니다.
     * 기록 중지 시 isRecording이 false로 변경됩니다.
     */
    fun stopRecording() {
        viewModelScope.launch {
            bleManager.stopRecording()
        }
    }
    
    // ===== 공통 유틸리티 함수들 =====
    
    /**
     * 연결 상태 초기화
     * 
     * 현재 연결을 해제하고 스캔을 중지하여 모든 상태를 초기화합니다.
     * 앱 재시작이나 오류 복구 시 사용됩니다.
     */
    fun resetConnection() {
        viewModelScope.launch {
            // 연결 상태 초기화
            bleManager.disconnect()
            bleManager.stopScan()
        }
    }
    
    /**
     * 연결 상태 문자열 반환
     * 
     * @return 현재 연결 상태를 나타내는 한국어 문자열
     * 
     * UI에서 연결 상태를 표시할 때 사용되는 유틸리티 함수입니다.
     */
    fun getConnectionStatus(): String {
        return when {
            isConnected.value -> "연결됨"
            isScanning.value -> "스캔 중"
            else -> "연결 해제됨"
        }
    }
    
    /**
     * 데이터 수신 상태 문자열 반환
     * 
     * @return 현재 데이터 수신 상태를 나타내는 한국어 문자열
     * 
     * UI에서 데이터 수신 상태를 표시할 때 사용되는 유틸리티 함수입니다.
     */
    fun getDataStatus(): String {
        return when {
            isReceivingData.value -> "데이터 수신 중"
            selectedSensors.value.isNotEmpty() -> "센서 선택됨"
            else -> "데이터 수신 안됨"
        }
    }
    
    /**
     * CSV 기록 상태 문자열 반환
     * 
     * @return 현재 CSV 기록 상태를 나타내는 한국어 문자열
     * 
     * UI에서 CSV 기록 상태를 표시할 때 사용되는 유틸리티 함수입니다.
     */
    fun getRecordingStatus(): String {
        return if (isRecording.value) "기록 중" else "기록 중지됨"
    }
} 
```

### 3. LinkBand-App.kt
**역할**: Jetpack Compose UI 컴포넌트

**주요 기능**:
- LinkBandScannerScreen: 디바이스 스캔 및 연결 UI
- LinkBandDataScreen: 센서 데이터 표시 및 제어 UI
- 공통 컴포넌트: DeviceItem, SensorDataCard, ReceivingIndicator

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
package com.example.yourProjectName.ui

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
import com.example.yourProjectName.ui.MainViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import android.util.Log

/**
 * LinkBand 스캐너 화면
 * 
 * 블루투스 디바이스를 스캔하고 LinkBand 디바이스와 연결을 관리하는 화면입니다.
 * 
 * @param viewModel MainViewModel 인스턴스 - 블루투스 연결 및 디바이스 관리
 * @param onDataScreenClick 데이터 화면으로 이동하는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBandScannerScreen(
    viewModel: MainViewModel,
    onDataScreenClick: () -> Unit = {}
) {
    // UI 상태 관리를 위한 StateFlow 값들
    val scannedDevices by viewModel.scannedDevices.collectAsState(initial = emptyList())
    val isScanning by viewModel.isScanning.collectAsState(initial = false)
    val isConnected by viewModel.isConnected.collectAsState(initial = false)
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState(initial = null)
    val isAutoReconnectEnabled by viewModel.isAutoReconnectEnabled.collectAsState(initial = false)
    
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
                            viewModel.enableAutoReconnect()
                        } else {
                            viewModel.disableAutoReconnect()
                        }
                    }
                )
            }
        }
        
        // 블루투스 스캔 시작/중지 버튼
        Button(
            onClick = if (isScanning) viewModel::stopScan else viewModel::startScan,
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
                                onConnect = { viewModel.connectToDevice(device) },
                                onDisconnect = { viewModel.disconnect() }
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
 * @param viewModel MainViewModel 인스턴스 - 센서 데이터 및 제어 관리
 * @param onDisconnect 연결 해제 시 호출되는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBandDataScreen(
    viewModel: MainViewModel,
    onDisconnect: () -> Unit = {}
) {
    // UI 상태 관리를 위한 StateFlow 값들
    val isConnected by viewModel.isConnected.collectAsState(initial = false)
    val selectedSensors by viewModel.selectedSensors.collectAsState(initial = emptySet())
    val isReceivingData by viewModel.isReceivingData.collectAsState(initial = false)
    val eegData by viewModel.eegData.collectAsState(initial = emptyList())
    val ppgData by viewModel.ppgData.collectAsState(initial = emptyList())
    val accData by viewModel.accData.collectAsState(initial = emptyList())
    val batteryData by viewModel.batteryData.collectAsState(initial = null)
    val isRecording by viewModel.isRecording.collectAsState(initial = false)
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState(initial = null)
    
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
                                "$connectedDeviceName ${viewModel.getConnectionStatus()}"
                            } else {
                                viewModel.getConnectionStatus()
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
                                        if (checked) viewModel.selectSensor(SensorType.EEG) 
                                        else viewModel.deselectSensor(SensorType.EEG)
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
                                        if (checked) viewModel.selectSensor(SensorType.PPG) 
                                        else viewModel.deselectSensor(SensorType.PPG)
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
                                        if (checked) viewModel.selectSensor(SensorType.ACC) 
                                        else viewModel.deselectSensor(SensorType.ACC)
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
                                    viewModel.stopSelectedSensors()
                                } else {
                                    activationRequested = true
                                    viewModel.startSelectedSensors()
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
                                        text = "timestamp: ${data.timestamp.time}, ch1uV: ${data.channel1.roundToInt()}µV, ch2uV: ${data.channel2.roundToInt()}µV, leadOff: ${if (data.leadOff) "1" else "0"}",
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
                                    viewModel.stopRecording()
                                } else {
                                    viewModel.startRecording()
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
                            text = "기록 상태: ${viewModel.getRecordingStatus()}",
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
                        viewModel.disconnect()
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
## LinkBand SDK 함수 설명

### BleManager 클래스

#### 블루투스 스캔 관련
```kotlin
// 스캔 시작
bleManager.startScan()

// 스캔 중지  
bleManager.stopScan()

// 스캔된 디바이스 목록
val scannedDevices: StateFlow<List<BluetoothDevice>>

// 스캔 상태
val isScanning: StateFlow<Boolean>
```

#### 디바이스 연결 관련
```kotlin
// 특정 디바이스 연결
bleManager.connectToDevice(device: BluetoothDevice)

// 연결 해제
bleManager.disconnect()

// 연결 상태
val isConnected: StateFlow<Boolean>

// 연결된 디바이스 이름
val connectedDeviceName: StateFlow<String?>
```

#### 자동 재연결 관련
```kotlin
// 자동 재연결 활성화
bleManager.enableAutoReconnect()

// 자동 재연결 비활성화
bleManager.disableAutoReconnect()

// 자동 재연결 상태
val isAutoReconnectEnabled: StateFlow<Boolean>
```

#### 센서 제어 관련
```kotlin
// 센서 선택/해제
bleManager.selectSensor(sensor: SensorType)
bleManager.deselectSensor(sensor: SensorType)

// 선택된 센서 목록
val selectedSensors: StateFlow<Set<SensorType>>

// 센서 활성화/비활성화
bleManager.startSelectedSensors()
bleManager.stopSelectedSensors()

// 데이터 수신 상태
val isReceivingData: StateFlow<Boolean>
```

#### 센서 데이터 관련
```kotlin
// EEG 데이터 (뇌파)
val eegData: StateFlow<List<EegData>>
// EegData 구조: timestamp, channel1, channel2, leadOff

// PPG 데이터 (맥파)
val ppgData: StateFlow<List<PpgData>>
// PpgData 구조: timestamp, red, ir

// ACC 데이터 (가속도계)
val accData: StateFlow<List<AccData>>
// AccData 구조: timestamp, x, y, z

// 배터리 데이터
val batteryData: StateFlow<BatteryData?>
// BatteryData 구조: level (0-100)
```

#### CSV 기록 관련
```kotlin
// CSV 기록 시작/중지
bleManager.startRecording()
bleManager.stopRecording()

// 기록 상태
val isRecording: StateFlow<Boolean>
```

## 🔧 고급 설정

### 배치 데이터 수집

#### 기본 함수들
```kotlin
// 수집 모드 설정 (샘플 수, 초, 분)
bleManager.setCollectionMode(mode: CollectionMode)

// 센서별 샘플 수 설정
bleManager.updateSensorSampleCount(sensorType: SensorType, sampleCount: Int, sampleCountText: String)

// 센서별 초 단위 설정
bleManager.updateSensorSeconds(sensorType: SensorType, seconds: Int, secondsText: String)

// 센서별 분 단위 설정
bleManager.updateSensorMinutes(sensorType: SensorType, minutes: Int, minutesText: String)

// 센서 설정 가져오기
bleManager.getSensorConfiguration(sensorType: SensorType): SensorBatchConfiguration?

// 배치 데이터 StateFlow들
val eegBatchData: StateFlow<List<EegData>>
val ppgBatchData: StateFlow<List<PpgData>>
val accBatchData: StateFlow<List<AccData>>
```

#### 관련 데이터 클래스들
```kotlin
// 수집 모드 enum
enum class CollectionMode {
    SAMPLE_COUNT,  // 샘플 수 기반
    SECONDS,       // 초 단위
    MINUTES        // 분 단위
}

// 센서 배치 설정 데이터 클래스
data class SensorBatchConfiguration(
    var sampleCount: Int,      // 샘플 수 (1-100000)
    var seconds: Int,          // 초 단위 (1-3600)
    var minutes: Int,          // 분 단위 (1-60)
    var sampleCountText: String,
    var secondsText: String,
    var minutesText: String
)
```

#### 수집 모드 설정
```kotlin
// 수집 모드 변경
bleManager.setCollectionMode(CollectionMode.SAMPLE_COUNT)  // 샘플 수 기반
bleManager.setCollectionMode(CollectionMode.SECONDS)       // 초 단위
bleManager.setCollectionMode(CollectionMode.MINUTES)       // 분 단위
```

#### 센서별 배치 설정
```kotlin
// 샘플 수 기반 배치 설정
bleManager.updateSensorSampleCount(SensorType.EEG, sampleCount, sampleCountText)

// 시간 기반 배치 설정 (초 단위)
bleManager.updateSensorSeconds(SensorType.PPG, seconds, secondsText)

// 시간 기반 배치 설정 (분 단위)
bleManager.updateSensorMinutes(SensorType.ACC, minutes, minutesText)

// 현재 센서 설정 조회
val config = bleManager.getSensorConfiguration(SensorType.EEG)
```

#### 배치 데이터 수신
```kotlin
// 배치 데이터 StateFlow 수신
bleManager.eegBatchData.collect { batch ->
    // EEG 배치 데이터 처리
}

bleManager.ppgBatchData.collect { batch ->
    // PPG 배치 데이터 처리
}

bleManager.accBatchData.collect { batch ->
    // ACC 배치 데이터 처리
}
```

### TimeBatchManager 사용법

#### 시간 기반 배치 관리자 생성
```kotlin
// 제네릭 타입으로 다양한 센서 데이터 지원
val eegBatchManager = TimeBatchManager<EegData>(
    targetIntervalMs = 1000L,  // 1초 간격
    timestampExtractor = { it.timestamp }
)
```

#### 배치 데이터 처리
```kotlin
// 샘플 추가 및 배치 완성 확인
val batch = timeBatchManager.addSample(sample)
if (batch != null) {
    // 배치가 완성됨 - 처리 로직
}

// 수집 중지 시 마지막 배치 반환
val finalBatch = timeBatchManager.flushBuffer()

// 버퍼 관리
timeBatchManager.clearBuffer()
val bufferSize = timeBatchManager.getBufferSize()
```

### 가속도계 모드 설정

#### 가속도계 모드 선택
```kotlin
enum class AccelerometerMode {
    RAW,    // 원시 가속도 값 (중력 포함)
    MOTION  // 선형 가속도 값 (중력 제거)
}

// 처리된 가속도계 데이터
data class ProcessedAccData(
    val timestamp: Date,
    val x: Short,
    val y: Short,
    val z: Short,
    val mode: AccelerometerMode
)
```

### 데이터 수집 설정

#### DataCollectionConfig 사용
```kotlin
// 샘플 수 기반 설정
val sampleConfig = DataCollectionConfig(
    sensorType = SensorType.EEG,
    mode = DataCollectionConfig.DataCollectionMode.SampleCount(250)
)

// 시간 기반 설정
val timeConfig = DataCollectionConfig(
    sensorType = SensorType.PPG,
    mode = DataCollectionConfig.DataCollectionMode.TimeInterval(5000L) // 5초
)
```

### 사용 예시

#### 기본 배치 수집
```kotlin
// 1. 수집 모드 설정
bleManager.setCollectionMode(CollectionMode.SAMPLE_COUNT)

// 2. 센서 설정
bleManager.updateSensorSampleCount(SensorType.EEG, 250, "250")

// 3. 센서 활성화
bleManager.selectSensor(SensorType.EEG)
bleManager.startSelectedSensors()

// 4. 배치 데이터 수신
bleManager.eegBatchData.collect { batch ->
    // 배치 데이터 처리
}
```

#### 시간 기반 배치 수집
```kotlin
// 1. 시간 기반 모드 설정
bleManager.setCollectionMode(CollectionMode.SECONDS)

// 2. 시간 간격 설정
bleManager.updateSensorSeconds(SensorType.PPG, 5, "5")

// 3. 센서 활성화
bleManager.selectSensor(SensorType.PPG)
bleManager.startSelectedSensors()

// 4. 배치 데이터 수신
bleManager.ppgBatchData.collect { batch ->
    // 배치 데이터 처리
}
```

#### TimeBatchManager 직접 사용
```kotlin
// 커스텀 배치 관리자 생성
val customBatchManager = TimeBatchManager<EegData>(
    targetIntervalMs = 2000L,  // 2초 간격
    timestampExtractor = { it.timestamp }
)

// 데이터 처리
eegData.forEach { data ->
    customBatchManager.addSample(data)?.let { batch ->
        // 배치 완성 시 처리
        processBatch(batch)
    }
}
```

## 요구사항

- Android Studio Arctic Fox 이상
- Java 17 이상
- Android 14.0 (API 34) 이상의 실제 디바이스
- Bluetooth 기능 지원 디바이스

---

Copyright ⓒ 룩시드랩스 All rights reserved.
