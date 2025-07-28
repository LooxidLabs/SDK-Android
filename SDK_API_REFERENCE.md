# LinkBand Android SDK - API 참조

## 📋 개요

LinkBand Android SDK의 모든 API 메서드와 속성에 대한 상세한 참조 문서입니다.

## 🔗 연결 관리

### linkBandSdk.startScan()
주변 LinkBand 디바이스를 스캔합니다. 스캔된 디바이스 목록은 `scannedDevices` StateFlow를 통해 실시간으로 업데이트됩니다. 스캔 중에는 `isScanning` 상태가 true로 변경됩니다.

```kotlin
linkBandSdk.startScan()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.stopScan()
디바이스 스캔을 중지합니다. 스캔 중지 후 `isScanning` 상태가 false로 변경됩니다.

```kotlin
linkBandSdk.stopScan()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.connectToDevice(device)
지정된 Bluetooth 디바이스에 연결합니다. 연결 상태는 `isConnected` StateFlow를 통해 실시간으로 모니터링할 수 있습니다.

```kotlin
linkBandSdk.connectToDevice(selectedDevice)
```

파라미터:
- `device` 필수 · BluetoothDevice
  연결할 LinkBand 디바이스입니다. `scannedDevices`에서 선택된 디바이스를 사용하세요.

반환값: 없음

### linkBandSdk.disconnect()
현재 연결된 디바이스와의 연결을 해제합니다. 연결 해제 후 `isConnected` 상태가 false로 변경됩니다.

```kotlin
linkBandSdk.disconnect()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.enableAutoReconnect()
자동 재연결 기능을 활성화합니다. 연결이 끊어졌을 때 최대 5회까지 자동으로 재연결을 시도합니다.

```kotlin
linkBandSdk.enableAutoReconnect()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.disableAutoReconnect()
자동 재연결 기능을 비활성화합니다. 연결이 끊어져도 자동으로 재연결을 시도하지 않습니다.

```kotlin
linkBandSdk.disableAutoReconnect()
```

파라미터: 없음

반환값: 없음

## 📊 센서 관리

### linkBandSdk.selectSensor(sensor)
수집할 센서를 선택합니다. 선택된 센서는 `selectedSensors` StateFlow에 추가되며, `startSelectedSensors()` 호출 시 활성화됩니다.

```kotlin
linkBandSdk.selectSensor(SensorType.EEG)
linkBandSdk.selectSensor(SensorType.PPG)
linkBandSdk.selectSensor(SensorType.ACC)
```

파라미터:
- `sensor` 필수 · SensorType
  선택할 센서 타입입니다. 지원되는 값: `SensorType.EEG`, `SensorType.PPG`, `SensorType.ACC`

반환값: 없음

### linkBandSdk.deselectSensor(sensor)
선택된 센서를 해제합니다. 해제된 센서는 `selectedSensors` StateFlow에서 제거됩니다.

```kotlin
linkBandSdk.deselectSensor(SensorType.EEG)
```

파라미터:
- `sensor` 필수 · SensorType
  해제할 센서 타입입니다. 지원되는 값: `SensorType.EEG`, `SensorType.PPG`, `SensorType.ACC`

반환값: 없음

### linkBandSdk.startSelectedSensors()
선택된 모든 센서를 순차적으로 시작합니다. 센서별 시작 상태는 `isEegStarted`, `isPpgStarted`, `isAccStarted` StateFlow를 통해 확인할 수 있습니다.

```kotlin
linkBandSdk.startSelectedSensors()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.stopSelectedSensors()
선택된 모든 센서를 중지합니다. 센서 중지 후 해당 센서의 데이터 수신이 중단됩니다.

```kotlin
linkBandSdk.stopSelectedSensors()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.setAccelerometerMode(mode)
가속도계의 데이터 처리 모드를 설정합니다. RAW 모드는 중력을 포함한 원시값을, MOTION 모드는 중력을 제거한 움직임만을 제공합니다.

```kotlin
linkBandSdk.setAccelerometerMode(AccelerometerMode.MOTION)
```

파라미터:
- `mode` 필수 · AccelerometerMode
  가속도계 처리 모드입니다. 지원되는 값: `AccelerometerMode.RAW`, `AccelerometerMode.MOTION`

반환값: 없음

## 📈 데이터 수집 설정

### linkBandSdk.setCollectionMode(mode)
데이터 수집 방식을 설정합니다. 샘플 수 기반, 초 단위, 분 단위 중 하나를 선택할 수 있습니다.

```kotlin
linkBandSdk.setCollectionMode(CollectionMode.SAMPLE_COUNT)
```

파라미터:
- `mode` 필수 · CollectionMode
  데이터 수집 모드입니다. 지원되는 값: `CollectionMode.SAMPLE_COUNT`, `CollectionMode.SECONDS`, `CollectionMode.MINUTES`

반환값: 없음

### linkBandSdk.updateSensorSampleCount(sensor, count, text)
지정된 센서의 샘플 수 기반 배치 설정을 업데이트합니다. 샘플 수는 1-100,000 범위 내에서 설정해야 합니다.

```kotlin
linkBandSdk.updateSensorSampleCount(SensorType.EEG, 250, "250")
```

파라미터:
- `sensor` 필수 · SensorType
  설정을 변경할 센서 타입입니다.
- `count` 필수 · Int
  배치당 샘플 수입니다. 1-100,000 범위의 정수값을 사용하세요.
- `text` 필수 · String
  UI 표시용 텍스트입니다. 일반적으로 count 값을 문자열로 변환한 값을 사용합니다.

반환값: 없음

### linkBandSdk.updateSensorSeconds(sensor, seconds, text)
지정된 센서의 초 단위 배치 설정을 업데이트합니다. 초 단위는 1-3,600 범위 내에서 설정해야 합니다.

```kotlin
linkBandSdk.updateSensorSeconds(SensorType.EEG, 5, "5")
```

파라미터:
- `sensor` 필수 · SensorType
  설정을 변경할 센서 타입입니다.
- `seconds` 필수 · Int
  배치 간격(초)입니다. 1-3,600 범위의 정수값을 사용하세요.
- `text` 필수 · String
  UI 표시용 텍스트입니다. 일반적으로 seconds 값을 문자열로 변환한 값을 사용합니다.

반환값: 없음

### linkBandSdk.updateSensorMinutes(sensor, minutes, text)
지정된 센서의 분 단위 배치 설정을 업데이트합니다. 분 단위는 1-60 범위 내에서 설정해야 합니다.

```kotlin
linkBandSdk.updateSensorMinutes(SensorType.EEG, 5, "5")
```

파라미터:
- `sensor` 필수 · SensorType
  설정을 변경할 센서 타입입니다.
- `minutes` 필수 · Int
  배치 간격(분)입니다. 1-60 범위의 정수값을 사용하세요.
- `text` 필수 · String
  UI 표시용 텍스트입니다. 일반적으로 minutes 값을 문자열로 변환한 값을 사용합니다.

반환값: 없음

### linkBandSdk.getSensorConfiguration(sensor)
지정된 센서의 현재 설정을 조회합니다. 샘플 수, 초 단위, 분 단위 설정값을 모두 포함합니다.

```kotlin
val config = linkBandSdk.getSensorConfiguration(SensorType.EEG)
```

파라미터:
- `sensor` 필수 · SensorType
  설정을 조회할 센서 타입입니다.

반환값: SensorBatchConfiguration? (센서가 선택되지 않은 경우 null)

## 💾 데이터 저장

### linkBandSdk.startRecording()
센서 데이터를 CSV 파일로 기록을 시작합니다. 기록 상태는 `isRecording` StateFlow를 통해 확인할 수 있습니다.

```kotlin
linkBandSdk.startRecording()
```

파라미터: 없음

반환값: 없음

### linkBandSdk.stopRecording()
CSV 파일 기록을 중지합니다. 기록 중지 후 파일이 저장소에 저장됩니다.

```kotlin
linkBandSdk.stopRecording()
```

파라미터: 없음

반환값: 없음

## 📊 StateFlow 속성

### 연결 상태

#### linkBandSdk.scannedDevices
스캔된 Bluetooth 디바이스 목록을 제공합니다. `startScan()` 호출 시 실시간으로 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.scannedDevices.collect { devices ->
        devices.forEach { device ->
            Log.d("Device", "Found: ${device.name}")
        }
    }
}
```

타입: StateFlow<List<BluetoothDevice>>

#### linkBandSdk.isScanning
현재 디바이스 스캔 중인지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isScanning.collect { isScanning ->
        updateScanButton(isScanning)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.isConnected
현재 LinkBand 디바이스와 연결되어 있는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isConnected.collect { isConnected ->
        updateConnectionStatus(isConnected)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.connectedDeviceName
현재 연결된 디바이스의 이름을 제공합니다. 연결되지 않은 경우 null입니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.connectedDeviceName.collect { deviceName ->
        deviceName?.let { name ->
            Log.d("Connected", "Device: $name")
        }
    }
}
```

타입: StateFlow<String?>

#### linkBandSdk.isAutoReconnectEnabled
자동 재연결 기능이 활성화되어 있는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isAutoReconnectEnabled.collect { enabled ->
        updateAutoReconnectToggle(enabled)
    }
}
```

타입: StateFlow<Boolean>

### 센서 데이터

#### linkBandSdk.eegData
EEG 센서에서 수집된 데이터 목록을 제공합니다. 실시간으로 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.eegData.collect { eegDataList ->
        eegDataList.forEach { eegData ->
            Log.d("EEG", "Channel1: ${eegData.channel1}µV")
        }
    }
}
```

타입: StateFlow<List<EegData>>

#### linkBandSdk.ppgData
PPG 센서에서 수집된 데이터 목록을 제공합니다. 실시간으로 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.ppgData.collect { ppgDataList ->
        ppgDataList.forEach { ppgData ->
            Log.d("PPG", "Red: ${ppgData.red}, IR: ${ppgData.ir}")
        }
    }
}
```

타입: StateFlow<List<PpgData>>

#### linkBandSdk.accData
가속도계 센서에서 수집된 원시 데이터 목록을 제공합니다. 실시간으로 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.accData.collect { accDataList ->
        accDataList.forEach { accData ->
            Log.d("ACC", "X: ${accData.x}, Y: ${accData.y}, Z: ${accData.z}")
        }
    }
}
```

타입: StateFlow<List<AccData>>

#### linkBandSdk.processedAccData
가속도계 센서에서 수집된 처리된 데이터 목록을 제공합니다. 중력 제거 등 처리가 적용된 데이터입니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.processedAccData.collect { processedAccDataList ->
        processedAccDataList.forEach { processedAccData ->
            Log.d("ProcessedACC", "Mode: ${processedAccData.mode}")
        }
    }
}
```

타입: StateFlow<List<ProcessedAccData>>

#### linkBandSdk.batteryData
디바이스의 배터리 상태 정보를 제공합니다. 실시간으로 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.batteryData.collect { batteryData ->
        batteryData?.let { battery ->
            Log.d("Battery", "Level: ${battery.level}%")
        }
    }
}
```

타입: StateFlow<BatteryData?>

### 센서 상태

#### linkBandSdk.selectedSensors
현재 선택된 센서들의 집합을 제공합니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.selectedSensors.collect { selectedSensors ->
        Log.d("Selected", "Sensors: $selectedSensors")
    }
}
```

타입: StateFlow<Set<SensorType>>

#### linkBandSdk.isEegStarted
EEG 센서가 시작되었는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isEegStarted.collect { isStarted ->
        updateEegButton(isStarted)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.isPpgStarted
PPG 센서가 시작되었는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isPpgStarted.collect { isStarted ->
        updatePpgButton(isStarted)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.isAccStarted
가속도계 센서가 시작되었는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isAccStarted.collect { isStarted ->
        updateAccButton(isStarted)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.isReceivingData
현재 데이터를 수신하고 있는지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isReceivingData.collect { isReceiving ->
        updateDataIndicator(isReceiving)
    }
}
```

타입: StateFlow<Boolean>

#### linkBandSdk.accelerometerMode
현재 가속도계의 처리 모드를 제공합니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.accelerometerMode.collect { mode ->
        Log.d("AccMode", "Current mode: $mode")
    }
}
```

타입: StateFlow<AccelerometerMode>

### 배치 데이터

#### linkBandSdk.eegBatchData
EEG 센서의 배치 데이터를 제공합니다. 설정된 배치 조건에 따라 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.eegBatchData.collect { batch ->
        if (batch.isNotEmpty()) {
            Log.d("EEGBatch", "Received ${batch.size} samples")
        }
    }
}
```

타입: StateFlow<List<EegData>>

#### linkBandSdk.ppgBatchData
PPG 센서의 배치 데이터를 제공합니다. 설정된 배치 조건에 따라 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.ppgBatchData.collect { batch ->
        if (batch.isNotEmpty()) {
            Log.d("PPGBatch", "Received ${batch.size} samples")
        }
    }
}
```

타입: StateFlow<List<PpgData>>

#### linkBandSdk.accBatchData
가속도계 센서의 배치 데이터를 제공합니다. 설정된 배치 조건에 따라 업데이트됩니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.accBatchData.collect { batch ->
        if (batch.isNotEmpty()) {
            Log.d("ACCBatch", "Received ${batch.size} samples")
        }
    }
}
```

타입: StateFlow<List<AccData>>

#### linkBandSdk.selectedCollectionMode
현재 선택된 데이터 수집 모드를 제공합니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.selectedCollectionMode.collect { mode ->
        Log.d("CollectionMode", "Current mode: $mode")
    }
}
```

타입: StateFlow<CollectionMode>

### 기록 상태

#### linkBandSdk.isRecording
현재 CSV 파일 기록 중인지 여부를 나타냅니다.

```kotlin
lifecycleScope.launch {
    linkBandSdk.isRecording.collect { isRecording ->
        updateRecordingButton(isRecording)
    }
}
```

타입: StateFlow<Boolean>

## 📊 데이터 모델

### EegData
EEG 센서 데이터를 나타내는 데이터 클래스입니다.

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
PPG 센서 데이터를 나타내는 데이터 클래스입니다.

```kotlin
data class PpgData(
    val timestamp: Date,        // 타임스탬프
    val red: Int,              // 적색 신호값
    val ir: Int                // 적외선 신호값
)
```

### AccData
가속도계 원시 데이터를 나타내는 데이터 클래스입니다.

```kotlin
data class AccData(
    val timestamp: Date,        // 타임스탬프
    val x: Short,              // X축 가속도 (16비트)
    val y: Short,              // Y축 가속도 (16비트)
    val z: Short               // Z축 가속도 (16비트)
)
```

### ProcessedAccData
처리된 가속도계 데이터를 나타내는 데이터 클래스입니다.

```kotlin
data class ProcessedAccData(
    val timestamp: Date,        // 타임스탬프
    val x: Short,              // X축 가속도
    val y: Short,              // Y축 가속도
    val z: Short,              // Z축 가속도
    val mode: AccelerometerMode // 처리 모드
)
```

### BatteryData
배터리 상태 데이터를 나타내는 데이터 클래스입니다.

```kotlin
data class BatteryData(
    val level: Int             // 배터리 레벨 (0-100%)
)
```

## 🔧 열거형

### SensorType
센서 타입을 나타내는 열거형입니다.

```kotlin
enum class SensorType {
    EEG,    // 뇌전도 센서
    PPG,    // 광전 용적 맥파 센서
    ACC     // 가속도계 센서
}
```

### AccelerometerMode
가속도계 처리 모드를 나타내는 열거형입니다.

```kotlin
enum class AccelerometerMode {
    RAW,    // 중력을 포함한 원시 가속도 값
    MOTION  // 중력을 제거한 움직임만 표시
}
```

### CollectionMode
데이터 수집 모드를 나타내는 열거형입니다.

```kotlin
enum class CollectionMode {
    SAMPLE_COUNT,  // 샘플 수 기반 수집
    SECONDS,       // 초 단위 시간 기반 수집
    MINUTES        // 분 단위 시간 기반 수집
}
```

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2024년 12월  
**작성자**: LooxidLabs Team 