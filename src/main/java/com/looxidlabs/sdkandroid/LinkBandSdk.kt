package io.github.looxidlabs.sdkandroid

import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * LinkBand SDK 진입점 클래스
 * BLE 센서 제어, 데이터 수집, 파싱, 저장 등 핵심 기능을 제공
 * 내부 BleManager를 래핑하여 간단하고 일관된 API를 제공합니다.
 */
class LinkBandSdk(private val context: Context) {
    
    // 내부 BleManager 인스턴스
    private val bleManager = BleManager(context)
    
    // 코루틴 스코프 (SDK 생명주기에 맞춰 관리)
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // ==================== 연결 관리 ====================
    
    /**
     * 주변 LinkBand 디바이스를 스캔합니다.
     * 스캔된 디바이스 목록은 scannedDevices StateFlow를 통해 실시간으로 업데이트됩니다.
     */
    fun startScan() {
        sdkScope.launch {
            bleManager.startScan()
        }
    }
    
    /**
     * 디바이스 스캔을 중지합니다.
     */
    fun stopScan() {
        sdkScope.launch {
            bleManager.stopScan()
        }
    }
    
    /**
     * 지정된 Bluetooth 디바이스에 연결합니다.
     * @param device 연결할 LinkBand 디바이스
     */
    fun connectToDevice(device: BluetoothDevice) {
        sdkScope.launch {
            bleManager.connectToDevice(device)
        }
    }
    
    /**
     * 현재 연결된 디바이스와의 연결을 해제합니다.
     */
    fun disconnect() {
        sdkScope.launch {
            bleManager.disconnect()
        }
    }
    
    /**
     * 자동 재연결 기능을 활성화합니다.
     */
    fun enableAutoReconnect() {
        sdkScope.launch {
            bleManager.enableAutoReconnect()
        }
    }
    
    /**
     * 자동 재연결 기능을 비활성화합니다.
     */
    fun disableAutoReconnect() {
        sdkScope.launch {
            bleManager.disableAutoReconnect()
        }
    }
    
    // ==================== 센서 관리 ====================
    
    /**
     * 수집할 센서를 선택합니다.
     * @param sensor 선택할 센서 타입
     */
    fun selectSensor(sensor: SensorType) {
        sdkScope.launch {
            bleManager.selectSensor(sensor)
        }
    }
    
    /**
     * 선택된 센서를 해제합니다.
     * @param sensor 해제할 센서 타입
     */
    fun deselectSensor(sensor: SensorType) {
        sdkScope.launch {
            bleManager.deselectSensor(sensor)
        }
    }
    
    /**
     * 선택된 모든 센서를 순차적으로 시작합니다.
     */
    fun startSelectedSensors() {
        sdkScope.launch {
            bleManager.startSelectedSensors()
        }
    }
    
    /**
     * 선택된 모든 센서를 중지합니다.
     */
    fun stopSelectedSensors() {
        sdkScope.launch {
            bleManager.stopSelectedSensors()
        }
    }
    
    /**
     * 가속도계의 데이터 처리 모드를 설정합니다.
     * @param mode 가속도계 처리 모드
     */
    fun setAccelerometerMode(mode: AccelerometerMode) {
        sdkScope.launch {
            bleManager.setAccelerometerMode(mode)
        }
    }
    
    // ==================== 데이터 수집 설정 ====================
    
    /**
     * 데이터 수집 방식을 설정합니다.
     * @param mode 데이터 수집 모드
     */
    fun setCollectionMode(mode: CollectionMode) {
        sdkScope.launch {
            bleManager.setCollectionMode(mode)
        }
    }
    
    /**
     * 지정된 센서의 샘플 수 기반 배치 설정을 업데이트합니다.
     * @param sensor 설정을 변경할 센서 타입
     * @param count 배치당 샘플 수 (1-100,000 범위)
     * @param text UI 표시용 텍스트
     */
    fun updateSensorSampleCount(sensor: SensorType, count: Int, text: String) {
        sdkScope.launch {
            bleManager.updateSensorSampleCount(sensor, count, text)
        }
    }
    
    /**
     * 지정된 센서의 초 단위 배치 설정을 업데이트합니다.
     * @param sensor 설정을 변경할 센서 타입
     * @param seconds 배치 간격(초) (1-3,600 범위)
     * @param text UI 표시용 텍스트
     */
    fun updateSensorSeconds(sensor: SensorType, seconds: Int, text: String) {
        sdkScope.launch {
            bleManager.updateSensorSeconds(sensor, seconds, text)
        }
    }
    
    /**
     * 지정된 센서의 분 단위 배치 설정을 업데이트합니다.
     * @param sensor 설정을 변경할 센서 타입
     * @param minutes 배치 간격(분) (1-60 범위)
     * @param text UI 표시용 텍스트
     */
    fun updateSensorMinutes(sensor: SensorType, minutes: Int, text: String) {
        sdkScope.launch {
            bleManager.updateSensorMinutes(sensor, minutes, text)
        }
    }
    
    /**
     * 지정된 센서의 현재 설정을 조회합니다.
     * @param sensor 설정을 조회할 센서 타입
     * @return 센서 설정 (센서가 선택되지 않은 경우 null)
     */
    fun getSensorConfiguration(sensor: SensorType): SensorBatchConfiguration? = 
        bleManager.getSensorConfiguration(sensor)
    
    // ==================== 데이터 저장 ====================
    
    /**
     * 센서 데이터를 CSV 파일로 기록을 시작합니다.
     */
    fun startRecording() {
        sdkScope.launch {
            bleManager.startRecording()
        }
    }
    
    /**
     * CSV 파일 기록을 중지합니다.
     */
    fun stopRecording() {
        sdkScope.launch {
            bleManager.stopRecording()
        }
    }
    
    // ==================== StateFlow 속성들 ====================
    
    // 연결 상태
    val scannedDevices: StateFlow<List<BluetoothDevice>> = bleManager.scannedDevices
    val isScanning: StateFlow<Boolean> = bleManager.isScanning
    val isConnected: StateFlow<Boolean> = bleManager.isConnected
    val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
    val isAutoReconnectEnabled: StateFlow<Boolean> = bleManager.isAutoReconnectEnabled
    
    // 센서 데이터
    val eegData: StateFlow<List<EegData>> = bleManager.eegData
    val ppgData: StateFlow<List<PpgData>> = bleManager.ppgData
    val accData: StateFlow<List<AccData>> = bleManager.accData
    val processedAccData: StateFlow<List<ProcessedAccData>> = bleManager.processedAccData
    val batteryData: StateFlow<BatteryData?> = bleManager.batteryData
    
    // 센서 상태
    val selectedSensors: StateFlow<Set<SensorType>> = bleManager.selectedSensors
    val isEegStarted: StateFlow<Boolean> = bleManager.isEegStarted
    val isPpgStarted: StateFlow<Boolean> = bleManager.isPpgStarted
    val isAccStarted: StateFlow<Boolean> = bleManager.isAccStarted
    val isReceivingData: StateFlow<Boolean> = bleManager.isReceivingData
    val accelerometerMode: StateFlow<AccelerometerMode> = bleManager.accelerometerMode
    
    // 배치 데이터
    val eegBatchData: StateFlow<List<EegData>> = bleManager.eegBatchData
    val ppgBatchData: StateFlow<List<PpgData>> = bleManager.ppgBatchData
    val accBatchData: StateFlow<List<AccData>> = bleManager.accBatchData
    val selectedCollectionMode: StateFlow<CollectionMode> = bleManager.selectedCollectionMode
    
    // 기록 상태
    val isRecording: StateFlow<Boolean> = bleManager.isRecording
    
    /**
     * SDK를 정리합니다. 연결을 해제하고 리소스를 정리합니다.
     */
    fun cleanup() {
        sdkScope.launch {
            bleManager.disconnect()
        }
    }
} 
