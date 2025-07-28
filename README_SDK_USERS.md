# LinkBand Android SDK - 사용자 가이드

LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 위한 Android SDK입니다.

## 📋 목차

- [주요 기능](#주요-기능)
- [설치 및 설정](#설치-및-설정)
- [기본 사용법](#기본-사용법)
- [API 참조](#api-참조)
- [데이터 모델](#데이터-모델)
- [예제 코드](#예제-코드)
- [문제 해결](#문제-해결)

## 🚀 주요 기능

### 📡 Bluetooth 연결 관리
- **자동 디바이스 스캔**: 주변 LinkBand 디바이스 자동 검색
- **연결 관리**: 디바이스 연결/해제 및 상태 모니터링
- **자동 재연결**: 연결 끊김 시 자동 재연결 기능
- **실시간 상태**: 연결 상태 실시간 업데이트

### 📊 센서 데이터 수집

#### 🧠 EEG (뇌전도)
- **2채널 원시 신호**: 24비트 해상도의 고품질 뇌전도 데이터
- **전압 변환**: µV 단위로 변환된 신호
- **전극 접촉 상태**: 전극 연결 상태 실시간 모니터링
- **샘플링 레이트**: 250Hz

#### ❤️ PPG (광전 용적 맥파)
- **적외선(IR) 신호**: 혈류량 변화 측정
- **적색(RED) 신호**: 산소 포화도 관련 데이터
- **샘플링 레이트**: 50Hz

#### 📱 ACC (가속도계)
- **3축 데이터**: X, Y, Z축 가속도 값
- **모드 지원**: 원시값(Raw) / 움직임(Motion) 모드
- **중력 제거**: 움직임 모드에서 중력 성분 자동 제거
- **샘플링 레이트**: 25Hz

#### 🔋 배터리 모니터링
- **잔량 표시**: 0-100% 배터리 레벨
- **실시간 업데이트**: 배터리 상태 실시간 모니터링

### 📈 배치 데이터 수집
- **샘플 수 기반**: 지정된 샘플 수만큼 수집
- **시간 기반**: 초/분 단위로 데이터 수집
- **센서별 설정**: 각 센서별 개별 수집 설정
- **실시간 배치**: 수집된 데이터 실시간 배치 처리

### 💾 데이터 저장
- **CSV 형식**: 표준 CSV 파일로 데이터 저장
- **타임스탬프**: 각 데이터에 정확한 타임스탬프 포함
- **센서별 분리**: 센서별로 별도 파일 생성

## 📦 설치 및 설정

### 1. Gradle 의존성 추가

```gradle
dependencies {
    implementation 'com.example:linkbandsdk:1.0.0'
}
```

### 2. 권한 설정

`AndroidManifest.xml`에 다음 권한을 추가:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 3. 최소 요구사항

- **Android API**: 24 (Android 7.0) 이상
- **Java 버전**: 11
- **Kotlin**: 1.8.0 이상

## 🎯 기본 사용법

### 1. SDK 초기화

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var linkBandSdk: LinkBandSdk
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // SDK 초기화
        linkBandSdk = LinkBandSdk(this)
    }
}
```

### 2. 디바이스 스캔 및 연결

```kotlin
// 디바이스 스캔 시작
linkBandSdk.startScan()

// 스캔된 디바이스 목록 관찰
lifecycleScope.launch {
    linkBandSdk.scannedDevices.collect { devices ->
        // 스캔된 디바이스 목록 처리
        devices.forEach { device ->
            Log.d("LinkBand", "Found device: ${device.name}")
        }
    }
}

// 디바이스 연결
linkBandSdk.connectToDevice(selectedDevice)
```

### 3. 센서 데이터 수집

```kotlin
// 센서 선택
linkBandSdk.selectSensor(SensorType.EEG)
linkBandSdk.selectSensor(SensorType.PPG)

// 데이터 수집 시작
linkBandSdk.startSelectedSensors()

// 실시간 데이터 관찰
lifecycleScope.launch {
    linkBandSdk.eegData.collect { eegDataList ->
        // EEG 데이터 처리
        eegDataList.forEach { eegData ->
            Log.d("EEG", "Channel1: ${eegData.channel1}µV")
        }
    }
}
```

### 4. 데이터 저장

```kotlin
// CSV 기록 시작
linkBandSdk.startRecording()

// 기록 중지
linkBandSdk.stopRecording()
```

## 📚 API 참조

### LinkBandSdk 클래스

#### 연결 관리

| 메서드 | 설명 | 매개변수 | 반환값 |
|--------|------|----------|--------|
| `startScan()` | 주변 디바이스 스캔 시작 | 없음 | 없음 |
| `stopScan()` | 디바이스 스캔 중지 | 없음 | 없음 |
| `connectToDevice(device)` | 지정된 디바이스에 연결 | BluetoothDevice | 없음 |
| `disconnect()` | 현재 연결 해제 | 없음 | 없음 |
| `enableAutoReconnect()` | 자동 재연결 활성화 | 없음 | 없음 |
| `disableAutoReconnect()` | 자동 재연결 비활성화 | 없음 | 없음 |

#### 센서 관리

| 메서드 | 설명 | 매개변수 | 반환값 |
|--------|------|----------|--------|
| `selectSensor(sensor)` | 센서 선택 | SensorType | 없음 |
| `deselectSensor(sensor)` | 센서 선택 해제 | SensorType | 없음 |
| `startSelectedSensors()` | 선택된 센서들 시작 | 없음 | 없음 |
| `stopSelectedSensors()` | 선택된 센서들 중지 | 없음 | 없음 |
| `setAccelerometerMode(mode)` | 가속도계 모드 설정 | AccelerometerMode | 없음 |

#### 데이터 수집 설정

| 메서드 | 설명 | 매개변수 | 반환값 |
|--------|------|----------|--------|
| `setCollectionMode(mode)` | 수집 모드 설정 | CollectionMode | 없음 |
| `updateSensorSampleCount(sensor, count, text)` | 샘플 수 설정 | SensorType, Int, String | 없음 |
| `updateSensorSeconds(sensor, seconds, text)` | 초 단위 설정 | SensorType, Int, String | 없음 |
| `updateSensorMinutes(sensor, minutes, text)` | 분 단위 설정 | SensorType, Int, String | 없음 |
| `getSensorConfiguration(sensor)` | 센서 설정 조회 | SensorType | SensorBatchConfiguration? |

#### 데이터 저장

| 메서드 | 설명 | 매개변수 | 반환값 |
|--------|------|----------|--------|
| `startRecording()` | CSV 기록 시작 | 없음 | 없음 |
| `stopRecording()` | CSV 기록 중지 | 없음 | 없음 |

### StateFlow 속성들

#### 연결 상태
- `scannedDevices`: 스캔된 디바이스 목록
- `isScanning`: 스캔 중 여부
- `isConnected`: 연결 상태
- `connectedDeviceName`: 연결된 디바이스 이름
- `isAutoReconnectEnabled`: 자동 재연결 활성화 여부

#### 센서 데이터
- `eegData`: EEG 데이터 목록
- `ppgData`: PPG 데이터 목록
- `accData`: 가속도계 데이터 목록
- `batteryData`: 배터리 데이터
- `processedAccData`: 처리된 가속도계 데이터

#### 센서 상태
- `selectedSensors`: 선택된 센서들
- `isEegStarted`: EEG 센서 시작 여부
- `isPpgStarted`: PPG 센서 시작 여부
- `isAccStarted`: 가속도계 센서 시작 여부
- `isReceivingData`: 데이터 수신 중 여부
- `accelerometerMode`: 가속도계 모드

#### 배치 데이터
- `eegBatchData`: EEG 배치 데이터
- `ppgBatchData`: PPG 배치 데이터
- `accBatchData`: 가속도계 배치 데이터
- `selectedCollectionMode`: 선택된 수집 모드

#### 기록 상태
- `isRecording`: CSV 기록 중 여부

## 📊 데이터 모델

### EegData
```kotlin
data class EegData(
    val timestamp: Date,        // 타임스탬프
    val leadOff: Boolean,       // 전극 접촉 상태 (true: 접촉 끊김)
    val channel1: Double,       // 채널1 전압값 (µV)
    val channel2: Double,       // 채널2 전압값 (µV)
    val ch1Raw: Int,           // 채널1 원시값 (24비트)
    val ch2Raw: Int            // 채널2 원시값 (24비트)
)
```

### PpgData
```kotlin
data class PpgData(
    val timestamp: Date,        // 타임스탬프
    val red: Int,              // 적색 신호값
    val ir: Int                // 적외선 신호값
)
```

### AccData
```kotlin
data class AccData(
    val timestamp: Date,        // 타임스탬프
    val x: Short,              // X축 가속도 (16비트)
    val y: Short,              // Y축 가속도 (16비트)
    val z: Short               // Z축 가속도 (16비트)
)
```

### BatteryData
```kotlin
data class BatteryData(
    val level: Int             // 배터리 레벨 (0-100%)
)
```

### ProcessedAccData
```kotlin
data class ProcessedAccData(
    val timestamp: Date,        // 타임스탬프
    val x: Short,              // X축 가속도
    val y: Short,              // Y축 가속도
    val z: Short,              // Z축 가속도
    val mode: AccelerometerMode // 처리 모드
)
```

## 🎯 예제 코드

### 완전한 사용 예제

```kotlin
class LinkBandExampleActivity : AppCompatActivity() {
    private lateinit var linkBandSdk: LinkBandSdk
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // SDK 초기화
        linkBandSdk = LinkBandSdk(this)
        
        // 상태 관찰 설정
        setupObservers()
        
        // UI 이벤트 설정
        setupUI()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            // 연결 상태 관찰
            linkBandSdk.isConnected.collect { isConnected ->
                updateConnectionStatus(isConnected)
            }
        }
        
        lifecycleScope.launch {
            // EEG 데이터 관찰
            linkBandSdk.eegData.collect { eegDataList ->
                if (eegDataList.isNotEmpty()) {
                    val latestEeg = eegDataList.last()
                    updateEegDisplay(latestEeg)
                }
            }
        }
        
        lifecycleScope.launch {
            // 배터리 상태 관찰
            linkBandSdk.batteryData.collect { batteryData ->
                batteryData?.let { updateBatteryDisplay(it) }
            }
        }
    }
    
    private fun setupUI() {
        // 스캔 버튼
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            linkBandSdk.startScan()
        }
        
        // 연결 버튼
        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            // 선택된 디바이스에 연결
            selectedDevice?.let { device ->
                linkBandSdk.connectToDevice(device)
            }
        }
        
        // 센서 시작 버튼
        findViewById<Button>(R.id.btnStartSensors).setOnClickListener {
            // EEG와 PPG 센서 선택
            linkBandSdk.selectSensor(SensorType.EEG)
            linkBandSdk.selectSensor(SensorType.PPG)
            
            // 센서 시작
            linkBandSdk.startSelectedSensors()
        }
        
        // 기록 시작 버튼
        findViewById<Button>(R.id.btnStartRecording).setOnClickListener {
            linkBandSdk.startRecording()
        }
        
        // 기록 중지 버튼
        findViewById<Button>(R.id.btnStopRecording).setOnClickListener {
            linkBandSdk.stopRecording()
        }
    }
    
    private fun updateConnectionStatus(isConnected: Boolean) {
        runOnUiThread {
            findViewById<TextView>(R.id.tvConnectionStatus).text = 
                if (isConnected) "연결됨" else "연결 끊김"
        }
    }
    
    private fun updateEegDisplay(eegData: EegData) {
        runOnUiThread {
            findViewById<TextView>(R.id.tvEegChannel1).text = 
                "채널1: ${eegData.channel1}µV"
            findViewById<TextView>(R.id.tvEegChannel2).text = 
                "채널2: ${eegData.channel2}µV"
        }
    }
    
    private fun updateBatteryDisplay(batteryData: BatteryData) {
        runOnUiThread {
            findViewById<TextView>(R.id.tvBattery).text = 
                "배터리: ${batteryData.level}%"
        }
    }
}
```

### 배치 데이터 수집 예제

```kotlin
// 배치 데이터 수집 설정
fun setupBatchCollection() {
    // 샘플 수 기반 수집 모드 설정
    linkBandSdk.setCollectionMode(CollectionMode.SAMPLE_COUNT)
    
    // EEG 센서: 250샘플마다 배치
    linkBandSdk.updateSensorSampleCount(SensorType.EEG, 250, "250")
    
    // PPG 센서: 50샘플마다 배치
    linkBandSdk.updateSensorSampleCount(SensorType.PPG, 50, "50")
    
    // 배치 데이터 관찰
    lifecycleScope.launch {
        linkBandSdk.eegBatchData.collect { batch ->
            if (batch.isNotEmpty()) {
                Log.d("Batch", "EEG 배치 수신: ${batch.size}개 샘플")
                // 배치 데이터 처리
                processEegBatch(batch)
            }
        }
    }
}

// 시간 기반 수집 예제
fun setupTimeBasedCollection() {
    // 초 단위 수집 모드 설정
    linkBandSdk.setCollectionMode(CollectionMode.SECONDS)
    
    // 5초마다 배치 수집
    linkBandSdk.updateSensorSeconds(SensorType.EEG, 5, "5")
    linkBandSdk.updateSensorSeconds(SensorType.PPG, 5, "5")
}
```

## 🔧 문제 해결

### 일반적인 문제들

#### 1. 디바이스가 스캔되지 않음
- **해결책**: 
  - Bluetooth가 활성화되어 있는지 확인
  - 위치 권한이 허용되어 있는지 확인
  - 디바이스가 페어링 모드인지 확인

#### 2. 연결이 자주 끊김
- **해결책**:
  - 자동 재연결 기능 활성화: `enableAutoReconnect()`
  - 디바이스와의 거리 확인
  - 다른 Bluetooth 장치 간섭 확인

#### 3. 데이터가 수신되지 않음
- **해결책**:
  - 센서가 선택되어 있는지 확인: `selectedSensors`
  - 센서가 시작되었는지 확인: `isEegStarted`, `isPpgStarted`
  - 전극 접촉 상태 확인: `leadOff` 값

#### 4. CSV 파일이 생성되지 않음
- **해결책**:
  - 저장소 권한 확인
  - `startRecording()` 호출 확인
  - 충분한 저장 공간 확인

### 로그 확인

```kotlin
// 디버그 로그 활성화
Log.d("LinkBand", "연결 상태: ${linkBandSdk.isConnected.value}")
Log.d("LinkBand", "선택된 센서: ${linkBandSdk.selectedSensors.value}")
Log.d("LinkBand", "데이터 수신 중: ${linkBandSdk.isReceivingData.value}")
```

## 📞 지원

- **문서**: [GitHub Wiki](https://github.com/looxidlabs/linkband-sdk-android/wiki)
- **이슈**: [GitHub Issues](https://github.com/looxidlabs/linkband-sdk-android/issues)
- **이메일**: support@looxidlabs.com

## 📄 라이선스

이 SDK는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 LICENSE 파일을 참조하세요.

---

**버전**: 1.0.0  
**최종 업데이트**: 2024년 12월  
**작성자**: LooxidLabs Team 