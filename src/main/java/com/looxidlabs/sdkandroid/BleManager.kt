package com.looxidlabs.sdkandroid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Environment
import android.util.Log
import com.example.linkbandsdk.AccData
import com.example.linkbandsdk.BatteryData
import com.example.linkbandsdk.EegData
import com.example.linkbandsdk.PpgData
import com.example.linkbandsdk.SensorDataParser
import com.example.linkbandsdk.SensorDataParsingException
import com.example.linkbandsdk.SensorBatchConfiguration
import com.example.linkbandsdk.AccelerometerMode
import com.example.linkbandsdk.ProcessedAccData
import com.example.linkbandsdk.CollectionMode
import com.example.linkbandsdk.DataCollectionConfig
import com.example.linkbandsdk.TimeBatchManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.roundToInt
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.linkbandsdk.SensorType
import java.util.concurrent.atomic.AtomicBoolean
import org.json.JSONObject
import org.json.JSONArray

// 센서 타입 enum 추가
// (SensorType enum 정의를 SensorData.kt로 이동)

// BLE(블루투스 저에너지) 센서 데이터 관리 및 수집, 배치, 기록, 상태 관리 등 LinkBand 앱의 핵심 BLE 로직을 담당하는 클래스
@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    
    // UUID 상수들 (LinkBand BLE 서비스 및 특성)
    companion object {
        val ACCELEROMETER_SERVICE_UUID = UUID.fromString("75c276c3-8f97-20bc-a143-b354244886d4")
        val ACCELEROMETER_CHAR_UUID = UUID.fromString("d3d46a35-4394-e9aa-5a43-e7921120aaed")
        
        val EEG_NOTIFY_SERVICE_UUID = UUID.fromString("df7b5d95-3afe-00a1-084c-b50895ef4f95")
        val EEG_NOTIFY_CHAR_UUID = UUID.fromString("00ab4d15-66b4-0d8a-824f-8d6f8966c6e5")
        val EEG_WRITE_CHAR_UUID = UUID.fromString("0065cacb-9e52-21bf-a849-99a80d83830e")
        
        val PPG_SERVICE_UUID = UUID.fromString("1cc50ec0-6967-9d84-a243-c2267f924d1f")
        val PPG_CHAR_UUID = UUID.fromString("6c739642-23ba-818b-2045-bfe8970263f6")
        
        val BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val BATTERY_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        
        const val EEG_SAMPLE_RATE = 250
        const val PPG_SAMPLE_RATE = 50
        const val ACC_SAMPLE_RATE = 25
    }
    
    // 블루투스 시스템 서비스 및 핸들러
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())
    
    // 센서 데이터 파서 (바이너리 → 구조화 데이터)
    private val sensorDataParser = SensorDataParser(SensorConfiguration.default)
    
    // BLE 연결 및 상태 관리
    private var bluetoothGatt: BluetoothGatt? = null
    
    // 센서별 데이터 StateFlow (UI와 연동)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()
    
    private val _eegData = MutableStateFlow<List<EegData>>(emptyList())
    val eegData: StateFlow<List<EegData>> = _eegData.asStateFlow()
    
    private val _ppgData = MutableStateFlow<List<PpgData>>(emptyList())
    val ppgData: StateFlow<List<PpgData>> = _ppgData.asStateFlow()
    
    private val _accData = MutableStateFlow<List<AccData>>(emptyList())
    val accData: StateFlow<List<AccData>> = _accData.asStateFlow()
    
    // 가속도계 모드 (원시값/움직임)
    private val _accelerometerMode = MutableStateFlow(AccelerometerMode.RAW)
    val accelerometerMode: StateFlow<AccelerometerMode> = _accelerometerMode.asStateFlow()
    
    private val _processedAccData = MutableStateFlow<List<ProcessedAccData>>(emptyList())
    val processedAccData: StateFlow<List<ProcessedAccData>> = _processedAccData.asStateFlow()
    
    // 중력 추정 관련 변수 (움직임 모드용)
    private var gravityX: Double = 0.0
    private var gravityY: Double = 0.0
    private var gravityZ: Double = 0.0
    private var isGravityInitialized: Boolean = false
    private val gravityFilterFactor: Double = 0.1 // 저역 통과 필터 계수
    
    private val _batteryData = MutableStateFlow<BatteryData?>(null)
    val batteryData: StateFlow<BatteryData?> = _batteryData.asStateFlow()
    
    // 배치 수집 관련 변수들
    private val _selectedCollectionMode = MutableStateFlow(CollectionMode.SAMPLE_COUNT)
    val selectedCollectionMode: StateFlow<CollectionMode> = _selectedCollectionMode.asStateFlow()
    
    // 센서별 설정 관리
    private val sensorConfigurations = mutableMapOf<SensorType, SensorBatchConfiguration>().apply {
        put(SensorType.EEG, SensorBatchConfiguration.defaultConfiguration(SensorType.EEG))
        put(SensorType.PPG, SensorBatchConfiguration.defaultConfiguration(SensorType.PPG))
        put(SensorType.ACC, SensorBatchConfiguration.defaultConfiguration(SensorType.ACC))
    }
    
    // 데이터 수집 설정
    private val dataCollectionConfigs = mutableMapOf<SensorType, DataCollectionConfig>()
    
    // 시간 기반 배치 관리자 (센서별)
    private var eegTimeBatchManager: TimeBatchManager<EegData>? = null
    private var ppgTimeBatchManager: TimeBatchManager<PpgData>? = null
    private var accTimeBatchManager: TimeBatchManager<AccData>? = null
    
    // 샘플 기반 배치 버퍼들
    private val eegSampleBuffer = mutableListOf<EegData>()
    private val ppgSampleBuffer = mutableListOf<PpgData>()
    private val accSampleBuffer = mutableListOf<AccData>()
    
    // 배치 데이터 StateFlow들
    private val _eegBatchData = MutableStateFlow<List<EegData>>(emptyList())
    val eegBatchData: StateFlow<List<EegData>> = _eegBatchData.asStateFlow()
    
    private val _ppgBatchData = MutableStateFlow<List<PpgData>>(emptyList())
    val ppgBatchData: StateFlow<List<PpgData>> = _ppgBatchData.asStateFlow()
    
    private val _accBatchData = MutableStateFlow<List<AccData>>(emptyList())
    val accBatchData: StateFlow<List<AccData>> = _accBatchData.asStateFlow()
    
    // 유효성 검사 범위
    private object ValidationRange {
        val sampleCount = 1..100000  // 최대 10만 샘플
        val seconds = 1..3600        // 최대 1시간
        val minutes = 1..60          // 최대 60분
    }
    
    private val _isEegStarted = MutableStateFlow(false)
    val isEegStarted: StateFlow<Boolean> = _isEegStarted.asStateFlow()
    
    private val _isPpgStarted = MutableStateFlow(false) 
    val isPpgStarted: StateFlow<Boolean> = _isPpgStarted.asStateFlow()
    
    private val _isAccStarted = MutableStateFlow(false)
    val isAccStarted: StateFlow<Boolean> = _isAccStarted.asStateFlow()
    
    // 자동연결 관련 StateFlow 추가
    private val _isAutoReconnectEnabled = MutableStateFlow(true) // 디폴트로 활성화
    val isAutoReconnectEnabled: StateFlow<Boolean> = _isAutoReconnectEnabled.asStateFlow()
    
    // 마지막 연결된 디바이스 정보 저장
    private var lastConnectedDevice: BluetoothDevice? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var reconnectRunnable: Runnable? = null
    
    // 서비스 준비 상태 플래그 추가
    private var servicesReady = false
    
    // 센서 선택 상태 관리
    private val _selectedSensors = MutableStateFlow<Set<SensorType>>(emptySet())
    val selectedSensors: StateFlow<Set<SensorType>> = _selectedSensors.asStateFlow()
    
    private val _isReceivingData = MutableStateFlow(false)
    val isReceivingData: StateFlow<Boolean> = _isReceivingData.asStateFlow()
    
    // CSV 기록 상태 관리
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // CSV 파일 관련 변수들
    private var eegCsvWriter: FileWriter? = null
    private var ppgCsvWriter: FileWriter? = null
    private var accCsvWriter: FileWriter? = null
    private var recordingStartTime: Long = 0
    
    // JSON 기록 상태 관리
    private var jsonWriter: FileWriter? = null
    private var recordingStartTimeJson: Long = 0
    
    // 통합 JSON 데이터 구조 (스위프트 SensorDataJSON과 동일)
    private val timestampData = mutableListOf<Double>()
    private val eegChannel1Data = mutableListOf<Double>()
    private val eegChannel2Data = mutableListOf<Double>()
    private val eegLeadOffData = mutableListOf<Int>()
    private val ppgRedData = mutableListOf<Int>()
    private val ppgIrData = mutableListOf<Int>()
    private val accelXData = mutableListOf<Int>()
    private val accelYData = mutableListOf<Int>()
    private val accelZData = mutableListOf<Int>()
    
    // JSON 데이터 스레드 안전성을 위한 락
    private val jsonDataLock = Any()
    
    // 센서 활성화 큐 관리 변수들 추가
    private var sensorActivationQueue = mutableListOf<SensorType>()
    private var currentActivatingSensor: SensorType? = null
    private var sensorTimeoutRunnable: Runnable? = null
    private val sensorTimeoutMs = 8000L // 8초 타임아웃
    
    // 각 센서별 마지막 데이터 수신 크기 추적
    private var lastEegDataSize = 0
    private var lastPpgDataSize = 0  
    private var lastAccDataSize = 0
    
    private val eegBufferLock = Any()
    private val ppgBufferLock = Any()
    private val accBufferLock = Any()
    
    // 중복 notification 방지 플래그
    private val eegNotificationEnabled = AtomicBoolean(false)
    
    // 연속 EEG 타임스탬프 관리 변수
    private var lastEegSampleTimestampMillis: Long? = null
    
    // BLE 스캔 콜백 (LinkBand 디바이스 필터)
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: return
            
            // "LXB-"로 시작하는 디바이스만 필터링
            if (deviceName.startsWith("LXB-")) {
                val currentDevices = _scannedDevices.value.toMutableList()
                if (!currentDevices.any { it.address == device.address }) {
                    currentDevices.add(device)
                    _scannedDevices.value = currentDevices
                }
            }
        }
    }
    
    // BLE GATT 콜백 (연결, 서비스, 데이터 수신 등)
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    _isConnected.value = true
                    _connectedDeviceName.value = gatt.device.name
                    // 연결 성공 시 재연결 시도 횟수 리셋
                    reconnectAttempts = 0
                    // 현재 연결된 디바이스를 마지막 연결 디바이스로 저장
                    lastConnectedDevice = gatt.device
                    Log.d("BleManager", "Connected to device: ${gatt.device.name}")
                    // 연결 완료 후 최대 MTU 설정 (515바이트)
                    Log.d("BleManager", "Requesting maximum MTU: 515")
                    gatt.requestMtu(515)
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    // 기록 중이면 기록 중지 (연결 해제 시)
                    if (_isRecording.value) {
                        Log.d("BleManager", "Stopping recording due to disconnection")
                        stopRecording()
                    }
                    
                    _isConnected.value = false
                    _connectedDeviceName.value = null
                    // 연결 해제 시 모든 센서 상태 리셋
                    _isEegStarted.value = false
                    _isPpgStarted.value = false
                    _isAccStarted.value = false
                    // 수집 상태도 리셋
                    _isReceivingData.value = false
                    // 서비스 준비 상태도 리셋
                    servicesReady = false
                    Log.d("BleManager", "Connection disconnected - all sensor states, collection, and recording stopped")
                    bluetoothGatt = null
                    
                    // 자동연결이 활성화되어 있고 마지막 연결 디바이스가 있으면 재연결 시도
                    if (_isAutoReconnectEnabled.value && lastConnectedDevice != null) {
                        attemptAutoReconnect()
                    }
                }
            }
        }
        
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d("BleManager", "MTU changed to: $mtu, status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleManager", "MTU successfully set to: $mtu")
            } else {
                Log.w("BleManager", "MTU change failed with status: $status")
            }
            // MTU 설정 완료 후 서비스 발견 시작 (안정성을 위해 복원)
            handler.postDelayed({
                gatt.discoverServices()
            }, 1000)
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BleManager", "Services discovered, status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt.services
                Log.d("BleManager", "Found ${services.size} services")
                for (service in services) {
                    Log.d("BleManager", "Service UUID: ${service.uuid}")
                }
                
                // 디바이스 연결 시 모든 센서를 디폴트로 선택
                val allSensors = setOf(SensorType.EEG, SensorType.PPG, SensorType.ACC)
                _selectedSensors.value = allSensors
                Log.d("BleManager", "All sensors selected by default: $allSensors")
                
                // 서비스 발견 후 notification 설정 전 딜레이 (안정성 복원)
                handler.postDelayed({
                    startNotifications(gatt)
                }, 500)
                
                // 서비스 완전 준비 완료 플래그 설정 (안정성 복원)
                handler.postDelayed({
                    servicesReady = true
                    Log.d("BleManager", "All services are now ready for sensor operations")
                }, 2000)
            } else {
                Log.e("BleManager", "Service discovery failed with status: $status")
            }
        }
        
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            
            if (data != null && data.isNotEmpty()) {
                when (characteristic.uuid) {
                    EEG_NOTIFY_CHAR_UUID -> {
                        parseEegData(data)
                    }
                    PPG_CHAR_UUID -> {
                        parsePpgData(data)
                    }
                    ACCELEROMETER_CHAR_UUID -> {
                        parseAccData(data)
                    }
                    BATTERY_CHAR_UUID -> {
                        parseBatteryData(data)
                    }
                    else -> {
                        Log.w("BleManager", "Unknown characteristic UUID: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.w("BleManager", "Received null or empty data from ${characteristic.uuid}")
            }
        }
        
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d("BleManager", "Characteristic read: ${characteristic.uuid}, status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                if (data != null && characteristic.uuid == BATTERY_CHAR_UUID) {
                    parseBatteryData(data)
                }
            }
        }
        
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d("BleManager", "Characteristic write: ${characteristic.uuid}, status: $status")
            // EEG write 명령 완료 로그만 남기고 자동 notification 설정 제거
            if (characteristic.uuid == EEG_WRITE_CHAR_UUID && status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleManager", "EEG start/stop command sent successfully")
            }
        }
        
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d("BleManager", "Descriptor write: ${descriptor.uuid}, status: $status")
            // Descriptor 쓰기 완료 처리
        }
    }
    
    // BLE 스캔/연결/서비스/센서 제어 함수들
    fun startScan() {
        if (!_isScanning.value) {
            _scannedDevices.value = emptyList()
            _isScanning.value = true
            bluetoothLeScanner.startScan(scanCallback)
        }
    }
    
    fun stopScan() {
        if (_isScanning.value) {
            _isScanning.value = false
            bluetoothLeScanner.stopScan(scanCallback)
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        stopScan()
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }
    
    // 자동연결 제어 함수들
    fun enableAutoReconnect() {
        _isAutoReconnectEnabled.value = true
        Log.d("BleManager", "Auto-reconnect enabled")
    }
    
    fun disableAutoReconnect() {
        _isAutoReconnectEnabled.value = false
        // 진행 중인 재연결 시도 취소
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        reconnectAttempts = 0
        Log.d("BleManager", "Auto-reconnect disabled")
    }
    
    private fun attemptAutoReconnect() {
        if (!_isAutoReconnectEnabled.value || lastConnectedDevice == null) {
            return
        }
        
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w("BleManager", "Max reconnect attempts reached. Auto-reconnect stopped.")
            return
        }
        
        reconnectAttempts++
        // 재연결 딜레이 계산 (3초, 5초, 10초, 20초, 30초)
        val delays = arrayOf(3000L, 5000L, 10000L, 20000L, 30000L)
        val delay = delays.getOrElse(reconnectAttempts - 1) { 30000L }
        
        Log.d("BleManager", "Attempting auto-reconnect ${reconnectAttempts}/${maxReconnectAttempts} in ${delay/1000}s...")
        
        reconnectRunnable = Runnable {
            lastConnectedDevice?.let { device ->
                Log.d("BleManager", "Auto-reconnecting to ${device.name}...")
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
            }
        }
        
        reconnectRunnable?.let { handler.postDelayed(it, delay) }
    }
    
    fun disconnect() {
        // 수동 연결 해제 시 자동연결 시도 취소
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        reconnectAttempts = 0
        
        // 기록 중이면 기록 중지
        if (_isRecording.value) {
            Log.d("BleManager", "Stopping recording due to disconnect")
            stopRecording()
        }
        
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _isConnected.value = false
        // 수동 연결 해제 시에도 모든 센서 상태 리셋
        _isEegStarted.value = false
        _isPpgStarted.value = false
        _isAccStarted.value = false
        // 수집 상태도 리셋
        _isReceivingData.value = false
        // 서비스 준비 상태도 리셋
        servicesReady = false
        Log.d("BleManager", "Manual disconnect - all sensor states, collection, and recording stopped")
    }
    
    private fun startNotifications(gatt: BluetoothGatt) {
        Log.d("BleManager", "Connection established - ready for manual service control")
        // 배터리만 즉시 읽기 (파이썬과 동일)
        val batteryChar = gatt.getService(BATTERY_SERVICE_UUID)?.getCharacteristic(BATTERY_CHAR_UUID)
        batteryChar?.let {
            Log.d("BleManager", "Reading battery characteristic")
            gatt.readCharacteristic(it)
        } ?: Log.e("BleManager", "Battery characteristic not found")
        
        // 나머지는 수동으로 시작하도록 변경
        // setupNotifications 자동 호출 제거
    }
    
    // EEG 수동 시작 함수
    fun startEegService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "[LOG] startEegService: called")
            Log.d("BleManager", "Starting EEG service manually")
            // 1. EEG 시작 명령 전송 (바이너리 명령 시도)
            val eegWriteChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_WRITE_CHAR_UUID)
            eegWriteChar?.let {
                Log.d("BleManager", "[LOG] startEegService: writeCharacteristic start")
                Log.d("BleManager", "Sending EEG start command (binary)")
                // 바이너리 명령 시도: 0x01 = start, 0x00 = stop
                it.value = byteArrayOf(0x01)
                gatt.writeCharacteristic(it)
                // 2. EEG notification 설정 (파이썬의 toggle_eeg_notify와 동일)
                handler.postDelayed({
                    val eegNotifyChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_NOTIFY_CHAR_UUID)
                    eegNotifyChar?.let { notifyChar ->
                        Log.d("BleManager", "[LOG] startEegService: setCharacteristicNotification true (EEG)")
                        gatt.setCharacteristicNotification(notifyChar, true)
                        val descriptor = notifyChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor?.let { desc ->
                            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            Log.d("BleManager", "[LOG] startEegService: writeDescriptor ENABLE (EEG)")
                            gatt.writeDescriptor(desc)
                            _isEegStarted.value = true
                        } ?: Log.e("BleManager", "EEG descriptor not found")
                    } ?: Log.e("BleManager", "EEG notification characteristic not found")
                }, 200)
            } ?: Log.e("BleManager", "EEG write characteristic not found")
        }
    }
    
    // PPG 수동 시작 함수
    fun startPpgService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "Starting PPG service manually")
            val ppgChar = gatt.getService(PPG_SERVICE_UUID)?.getCharacteristic(PPG_CHAR_UUID)
            ppgChar?.let {
                // PPG 명령 전송이 필요하다면 여기에 추가
                Log.d("BleManager", "Setting up PPG notification")
                gatt.setCharacteristicNotification(it, true)
                val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.let { desc ->
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(desc)
                    _isPpgStarted.value = true
                } ?: Log.e("BleManager", "PPG descriptor not found")
            } ?: Log.e("BleManager", "PPG characteristic not found")
        }
    }
    
    // ACC 수동 시작 함수  
    fun startAccService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "Starting ACC service manually")
            val accChar = gatt.getService(ACCELEROMETER_SERVICE_UUID)?.getCharacteristic(ACCELEROMETER_CHAR_UUID)
            accChar?.let {
                // ACC 명령 전송이 필요하다면 여기에 추가
                Log.d("BleManager", "Setting up ACC notification")
                gatt.setCharacteristicNotification(it, true)
                val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.let { desc ->
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(desc)
                    _isAccStarted.value = true
                } ?: Log.e("BleManager", "ACC descriptor not found")
            } ?: Log.e("BleManager", "ACC characteristic not found")
        }
    }
    
    // 서비스 중지 함수들
    fun stopEegService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "[LOG] stopEegService: called")
            Log.d("BleManager", "Stopping EEG service")
            val eegNotifyChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_NOTIFY_CHAR_UUID)
            eegNotifyChar?.let {
                Log.d("BleManager", "[LOG] stopEegService: setCharacteristicNotification false (EEG)")
                gatt.setCharacteristicNotification(it, false)
                _isEegStarted.value = false
                eegNotificationEnabled.set(false) // 플래그 초기화
            }
            val eegWriteChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_WRITE_CHAR_UUID)
            eegWriteChar?.let {
                Log.d("BleManager", "[LOG] stopEegService: writeCharacteristic stop")
                it.value = "stop".toByteArray()
                gatt.writeCharacteristic(it)
            }
            // 센서 데이터 파서의 EEG 타임스탬프도 리셋
            sensorDataParser.resetEegTimestamp()
        }
    }
    
    fun stopPpgService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "Stopping PPG service")
            val ppgChar = gatt.getService(PPG_SERVICE_UUID)?.getCharacteristic(PPG_CHAR_UUID)
            ppgChar?.let {
                gatt.setCharacteristicNotification(it, false)
                _isPpgStarted.value = false
            }
            // 센서 데이터 파서의 PPG 타임스탬프도 리셋
            sensorDataParser.resetPpgTimestamp()
        }
    }
    
    fun stopAccService() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "Stopping ACC service")
            val accChar = gatt.getService(ACCELEROMETER_SERVICE_UUID)?.getCharacteristic(ACCELEROMETER_CHAR_UUID)
            accChar?.let {
                gatt.setCharacteristicNotification(it, false)
                _isAccStarted.value = false
            }
            // 센서 데이터 파서의 ACC 타임스탬프도 리셋
            sensorDataParser.resetAccTimestamp()
        }
    }
    
    // 센서별 데이터 파싱 함수 (EEG/PPG/ACC/Battery)
    private fun parseEegData(data: ByteArray) {
        try {
            val readings = sensorDataParser.parseEegData(data)
            if (readings.isNotEmpty()) {
                val currentData = _eegData.value.takeLast(1000).toMutableList()
                currentData.addAll(readings)
                _eegData.value = currentData
                if (_eegData.value.size > lastEegDataSize) {
                    onSensorDataReceived(SensorType.EEG)
                }
                readings.forEach { reading ->
                    addToEegBuffer(reading)
                }
                readings.forEach { data ->
                    writeEegToCsv(data)
                }
            }
        } catch (e: SensorDataParsingException) {
            Log.e("BleManager", "EEG parsing error: ${e.message}")
        }
    }
    
    private fun parsePpgData(data: ByteArray) {
        try {
            val readings = sensorDataParser.parsePpgData(data)
            if (readings.isNotEmpty()) {
                val currentData = _ppgData.value.takeLast(500).toMutableList()
                currentData.addAll(readings)
                _ppgData.value = currentData
                
                // 데이터 수신 확인 (새로운 데이터가 들어왔는지 확인)
                if (_ppgData.value.size > lastPpgDataSize) {
                    onSensorDataReceived(SensorType.PPG)
                }
                
                // 배치 처리 추가
                readings.forEach { reading ->
                    addToPpgBuffer(reading)
                }
                
                // CSV 파일에 저장
                readings.forEach { data ->
                    writePpgToCsv(data)
                }
            }
        } catch (e: SensorDataParsingException) {
            Log.e("BleManager", "PPG parsing error: ${e.message}")
        }
    }
    
    private fun parseAccData(data: ByteArray) {
        try {
            val readings = sensorDataParser.parseAccelerometerData(data)
            if (readings.isNotEmpty()) {
                val currentMode = _accelerometerMode.value
                
                // 원시 데이터 업데이트
                val currentData = _accData.value.takeLast(300).toMutableList()
                currentData.addAll(readings)
                _accData.value = currentData
                
                // 처리된 데이터 생성 및 업데이트
                val processedReadings = readings.map { reading ->
                    processAccelerometerReading(reading)
                }
                val currentProcessedData = _processedAccData.value.takeLast(300).toMutableList()
                currentProcessedData.addAll(processedReadings)
                _processedAccData.value = currentProcessedData
                
                // 데이터 수신 확인 (새로운 데이터가 들어왔는지 확인)
                if (_accData.value.size > lastAccDataSize) {
                    onSensorDataReceived(SensorType.ACC)
                }
                
                // 배치 처리 추가 (원시 데이터 사용)
                readings.forEach { reading ->
                    addToAccBuffer(reading)
                }
                
                // CSV 파일에 저장 (처리된 데이터 사용)
                processedReadings.forEach { processedData ->
                    writeAccToCsv(processedData)
                }
            }
        } catch (e: SensorDataParsingException) {
            Log.e("BleManager", "ACC parsing error: ${e.message}")
        }
    }
    
    private fun parseBatteryData(data: ByteArray) {
        try {
            val batteryReading = sensorDataParser.parseBatteryData(data)
            _batteryData.value = batteryReading
        } catch (e: SensorDataParsingException) {
            Log.e("BleManager", "Battery parsing error: ${e.message}")
        }
    }
    
    // 센서 선택/모드/배치/기록 등 제어 함수들
    fun selectSensor(sensor: SensorType) {
        val currentSelected = _selectedSensors.value.toMutableSet()
        currentSelected.add(sensor)
        _selectedSensors.value = currentSelected
        Log.d("BleManager", "Sensor selected: $sensor, current selection: $currentSelected")
    }
    
    fun deselectSensor(sensor: SensorType) {
        val currentSelected = _selectedSensors.value.toMutableSet()
        currentSelected.remove(sensor)
        _selectedSensors.value = currentSelected
        Log.d("BleManager", "Sensor deselected: $sensor, current selection: $currentSelected")
    }
    
    // 가속도계 모드 제어 함수들
    fun setAccelerometerMode(mode: AccelerometerMode) {
        if (_accelerometerMode.value != mode) {
            _accelerometerMode.value = mode
            Log.d("BleManager", "Accelerometer mode changed to: ${mode.description}")
            
            // 모드 변경 시 중력 추정 초기화
            if (mode == AccelerometerMode.MOTION) {
                resetGravityEstimate()
            }
        }
    }
    
    // 중력 추정 초기화 (스위프트의 resetGravityEstimate와 동일)
    private fun resetGravityEstimate() {
        isGravityInitialized = false
        gravityX = 0.0
        gravityY = 0.0
        gravityZ = 0.0
        Log.d("BleManager", "Gravity estimate reset for motion mode")
    }
    
    // 중력 성분을 추정하고 업데이트하는 함수 (스위프트와 동일)
    private fun updateGravityEstimate(reading: AccData) {
        if (!isGravityInitialized) {
            // 첫 번째 읽기: 초기값으로 설정
            gravityX = reading.x.toDouble()
            gravityY = reading.y.toDouble()
            gravityZ = reading.z.toDouble()
            isGravityInitialized = true
            Log.d("BleManager", "Gravity initialized: X=$gravityX, Y=$gravityY, Z=$gravityZ")
        } else {
            // 저역 통과 필터를 사용한 중력 추정
            gravityX = gravityX * (1 - gravityFilterFactor) + reading.x.toDouble() * gravityFilterFactor
            gravityY = gravityY * (1 - gravityFilterFactor) + reading.y.toDouble() * gravityFilterFactor
            gravityZ = gravityZ * (1 - gravityFilterFactor) + reading.z.toDouble() * gravityFilterFactor
        }
    }
    
    // 가속도계 모드에 따라 처리된 데이터를 생성 (스위프트와 동일)
    private fun processAccelerometerReading(reading: AccData): ProcessedAccData {
        return when (_accelerometerMode.value) {
            AccelerometerMode.RAW -> {
                // 원시값 모드: 원래 데이터 그대로 반환
                ProcessedAccData(reading.timestamp, reading.x, reading.y, reading.z, AccelerometerMode.RAW)
            }
            AccelerometerMode.MOTION -> {
                // 움직임 모드: 중력 제거된 선형 가속도 반환
                updateGravityEstimate(reading)
                val linearX = (reading.x.toDouble() - gravityX).toInt().toShort()
                val linearY = (reading.y.toDouble() - gravityY).toInt().toShort()
                val linearZ = (reading.z.toDouble() - gravityZ).toInt().toShort()
                
                ProcessedAccData(reading.timestamp, linearX, linearY, linearZ, AccelerometerMode.MOTION)
            }
        }
    }
    
    private fun setupNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, sensorName: String) {
        if (sensorName == "EEG") {
            if (eegNotificationEnabled.get()) {
                Log.w("BleManager", "⚠️ EEG notification already enabled, skipping...")
                return
            }
        }
        Log.d("BleManager", "[LOG] setupNotification called for $sensorName: setCharacteristicNotification true")
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        descriptor?.let { desc ->
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            Log.d("BleManager", "[LOG] setupNotification: writeDescriptor ENABLE for $sensorName")
            gatt.writeDescriptor(desc)
            if (sensorName == "EEG") {
                eegNotificationEnabled.set(true)
            }
        } ?: Log.e("BleManager", "$sensorName descriptor not found")
    }
    
    // 모든 센서 notification 비활성화 헬퍼 함수 (스위프트 방식과 동일)
    private fun disableAllSensorNotifications() {
        bluetoothGatt?.let { gatt ->
            Log.d("BleManager", "[LOG] disableAllSensorNotifications: called")
            Log.d("BleManager", "Disabling all sensor notifications")
            handler.removeCallbacksAndMessages(null)
            Log.d("BleManager", "🛑 All pending handler callbacks cancelled in disable function")
            setNotifyValue(false, SensorType.EEG, gatt)
            handler.postDelayed({
                setNotifyValue(false, SensorType.PPG, gatt)
            }, 200)
            handler.postDelayed({
                setNotifyValue(false, SensorType.ACC, gatt)
            }, 400)
            _isEegStarted.value = false
            _isPpgStarted.value = false
            _isAccStarted.value = false
            _isReceivingData.value = false
            eegNotificationEnabled.set(false) // 플래그 초기화
            Log.d("BleManager", "All sensor notifications disabled (배터리는 항상 활성 상태 유지)")
        }
    }
    
    // 스위프트의 setNotifyValue(_:for:) 메서드와 동일한 기능
    private fun setNotifyValue(enabled: Boolean, sensorType: SensorType, gatt: BluetoothGatt) {
        val (serviceUUID, characteristicUUID) = when (sensorType) {
            SensorType.EEG -> Pair(EEG_NOTIFY_SERVICE_UUID, EEG_NOTIFY_CHAR_UUID)
            SensorType.PPG -> Pair(PPG_SERVICE_UUID, PPG_CHAR_UUID)
            SensorType.ACC -> Pair(ACCELEROMETER_SERVICE_UUID, ACCELEROMETER_CHAR_UUID)
        }
        
        // 서비스에서 해당 characteristic 찾기
        for (service in gatt.services ?: emptyList()) {
            for (characteristic in service.characteristics ?: emptyList()) {
                if (characteristic.uuid == characteristicUUID) {
                    Log.d("BleManager", "[LOG] setNotifyValue: setCharacteristicNotification $enabled for $sensorType")
                    gatt.setCharacteristicNotification(characteristic, enabled)
                    
                    // descriptor 설정하여 펌웨어에 notify 활성화/비활성화 명령 전송
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.let { desc ->
                        desc.value = if (enabled) {
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        } else {
                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        }
                        Log.d("BleManager", "[LOG] setNotifyValue: writeDescriptor ${if (enabled) "ENABLE" else "DISABLE"} for $sensorType")
                        gatt.writeDescriptor(desc)
                    }
                    
                    // EEG의 경우 추가로 stop 명령 전송
                    if (sensorType == SensorType.EEG && !enabled) {
                        val eegWriteChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_WRITE_CHAR_UUID)
                        eegWriteChar?.let {
                            Log.d("BleManager", "[LOG] setNotifyValue: writeCharacteristic stop (EEG)")
                            it.value = "stop".toByteArray()
                            gatt.writeCharacteristic(it)
                        }
                    }
                    
                    return // characteristic 찾았으므로 종료
                }
            }
        }
        
        Log.w("BleManager", "Characteristic not found for ${sensorType.name}")
    }
    
    fun startSelectedSensors() {
        val selectedSensors = _selectedSensors.value
        if (selectedSensors.isEmpty()) {
            Log.w("BleManager", "No sensors selected")
            return
        }
        
        Log.d("BleManager", "=== 센서 큐 기반 활성화 시작: $selectedSensors ===")
        
        bluetoothGatt?.let { gatt ->
            
            // 1단계: 모든 센서 notification 비활성화 (펌웨어 데이터 전송 중단)
            Log.d("BleManager", "1단계: 모든 센서 notification 비활성화")
            disableAllSensorNotifications()
            
            // 2단계: 선택된 센서들을 큐에 추가하고 순차 활성화 시작
            handler.postDelayed({
                Log.d("BleManager", "2단계: 센서 큐 생성 및 순차 활성화 시작")
                
                // 큐 초기화
                sensorActivationQueue.clear()
                currentActivatingSensor = null
                
                // ✅ 스위프트와 동일: 배터리 센서는 항상 먼저 활성화 (PPG 단독 동작을 위해 필수)
                Log.d("BleManager", "🔋 배터리 센서 notification 활성화 (스위프트 configureSensorNotifications 로직)")
                val batteryChar = gatt.getService(BATTERY_SERVICE_UUID)?.getCharacteristic(BATTERY_CHAR_UUID)
                batteryChar?.let { char ->
                    gatt.setCharacteristicNotification(char, true)
                    val descriptor = char.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.let { desc ->
                        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(desc)
                        Log.d("BleManager", "🔋 배터리 센서 notification 활성화 완료")
                    }
                } ?: Log.w("BleManager", "🔋 배터리 characteristic 찾을 수 없음")
                
                // EEG write 명령은 EEG가 선택되었을 때만 전송 (스위프트와 동일)
                if (selectedSensors.contains(SensorType.EEG)) {
                    val eegWriteChar = gatt.getService(EEG_NOTIFY_SERVICE_UUID)?.getCharacteristic(EEG_WRITE_CHAR_UUID)
                    eegWriteChar?.let {
                        it.value = "start".toByteArray()
                        gatt.writeCharacteristic(it)
                        Log.d("BleManager", "EEG write command sent (EEG selected)")
                    }
                }
                
                // 선택된 센서들을 순서대로 큐에 추가 (EEG → ACC → PPG 순서)
                if (selectedSensors.contains(SensorType.EEG)) {
                    sensorActivationQueue.add(SensorType.EEG)
                }
                if (selectedSensors.contains(SensorType.ACC)) {
                    sensorActivationQueue.add(SensorType.ACC)
                }
                if (selectedSensors.contains(SensorType.PPG)) {
                    sensorActivationQueue.add(SensorType.PPG)
                }
                
                Log.d("BleManager", "센서 활성화 큐 생성됨: $sensorActivationQueue")
                
                // 선택된 센서들에 대해 배치 수집 설정 적용
                selectedSensors.forEach { sensorType ->
                    configureSensorCollection(sensorType)
                }
                
                // 서비스가 완전히 준비되지 않았으면 추가 딜레이
                val initialDelay = if (!servicesReady) {
                    Log.d("BleManager", "Services not fully ready, adding initial delay...")
                    2000L
                } else {
                    1000L
                }
                
                // 배터리 센서 활성화 완료 후 실제 센서 활성화 시작
                handler.postDelayed({
                    Log.d("BleManager", "큐 기반 순차 활성화 시작 - 큐: $sensorActivationQueue")
                    activateNextSensorInQueue()
                }, initialDelay)
                
            }, 1200) // 1단계 완료 후 1.2초 대기 (BLE 명령어 안정화)
        }
    }
    
    fun stopSelectedSensors() {
        Log.d("BleManager", "=== 수집 중지: 모든 센서 펌웨어 notify 중단 ===")
        
        // 모든 pending handler 작업 취소 (센서 활성화 큐 포함)
        handler.removeCallbacksAndMessages(null)
        Log.d("BleManager", "🛑 All pending handler callbacks cancelled")
        
        // 센서 활성화 큐 상태 초기화
        sensorActivationQueue.clear()
        currentActivatingSensor = null
        sensorTimeoutRunnable = null
        
        // 데이터 크기 추적 변수 초기화
        lastEegDataSize = 0
        lastPpgDataSize = 0
        lastAccDataSize = 0
        
        // 모든 센서 notification 비활성화 (펌웨어 데이터 전송 완전 중단)
        disableAllSensorNotifications()
        
        Log.d("BleManager", "🛑 All sensors stopped - firmware data transmission completely stopped, queue cleared")
    }
    
    // CSV 기록 제어 함수들
    fun startRecording() {
        if (_isRecording.value) {
            Log.w("BleManager", "Recording already in progress")
            return
        }
        
        val selectedSensors = _selectedSensors.value
        if (selectedSensors.isEmpty()) {
            Log.w("BleManager", "No sensors selected for recording")
            return
        }
        
        try {
            recordingStartTime = System.currentTimeMillis()
            recordingStartTimeJson = recordingStartTime
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            
            // 내장 저장공간의 Downloads 폴더에 파일 생성
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // LinkBand 전용 폴더 생성
            val linkBandDir = File(downloadsDir, "LinkBand")
            if (!linkBandDir.exists()) {
                linkBandDir.mkdirs()
            }
            
            // JSON 데이터 버퍼 초기화
            synchronized(jsonDataLock) {
                timestampData.clear()
                eegChannel1Data.clear()
                eegChannel2Data.clear()
                eegLeadOffData.clear()
                ppgRedData.clear()
                ppgIrData.clear()
                accelXData.clear()
                accelYData.clear()
                accelZData.clear()
            }
            
            // 선택된 센서에 대해서만 CSV와 JSON 파일 생성
            val createdFiles = mutableListOf<String>()
            
            // 통합 JSON 파일 생성 (선택된 센서가 있으면 하나만 생성)
            if (selectedSensors.isNotEmpty()) {
                val jsonFile = File(linkBandDir, "LinkBand_SensorData_${timestamp}.json")
                jsonWriter = FileWriter(jsonFile)
                createdFiles.add("JSON=${jsonFile.name}")
                Log.d("BleManager", "Unified JSON file created: ${jsonFile.name}")
            }
            
            if (selectedSensors.contains(SensorType.EEG)) {
                // CSV 파일
                val eegFile = File(linkBandDir, "LinkBand_EEG_${timestamp}.csv")
                eegCsvWriter = FileWriter(eegFile)
                eegCsvWriter?.write("timestamp,ch1Raw,ch2Raw,ch1uV,ch2uV,leadOff\n")
                createdFiles.add("EEG_CSV=${eegFile.name}")
                
                Log.d("BleManager", "EEG CSV file created: ${eegFile.name}")
            }
            
            if (selectedSensors.contains(SensorType.PPG)) {
                // CSV 파일
                val ppgFile = File(linkBandDir, "LinkBand_PPG_${timestamp}.csv")
                ppgCsvWriter = FileWriter(ppgFile)
                ppgCsvWriter?.write("timestamp,red,ir\n")
                createdFiles.add("PPG_CSV=${ppgFile.name}")
                
                Log.d("BleManager", "PPG CSV file created: ${ppgFile.name}")
            }
            
            if (selectedSensors.contains(SensorType.ACC)) {
                // CSV 파일
                val accFile = File(linkBandDir, "LinkBand_ACC_${timestamp}.csv")
                accCsvWriter = FileWriter(accFile)
                accCsvWriter?.write("timestamp,x,y,z\n")
                createdFiles.add("ACC_CSV=${accFile.name}")
                
                Log.d("BleManager", "ACC CSV file created: ${accFile.name}")
            }
            
            _isRecording.value = true
            Log.d("BleManager", "Recording started at: ${linkBandDir.absolutePath}")
            Log.d("BleManager", "Created files: ${createdFiles.joinToString(", ")}")
            
        } catch (e: Exception) {
            Log.e("BleManager", "Failed to start recording", e)
            stopRecording()
        }
    }
    
    fun stopRecording() {
        if (!_isRecording.value) {
            Log.w("BleManager", "No recording in progress")
            return
        }
        
        try {
            // JSON 데이터를 파일에 저장
            saveJsonFiles()
            
            // CSV 파일들 닫기
            eegCsvWriter?.close()
            ppgCsvWriter?.close()
            accCsvWriter?.close()
            
            // JSON 파일들 닫기
            jsonWriter?.close()
            
            // 모든 writer 초기화
            eegCsvWriter = null
            ppgCsvWriter = null
            accCsvWriter = null
            jsonWriter = null
            
            // JSON 데이터 버퍼 정리
            synchronized(jsonDataLock) {
                timestampData.clear()
                eegChannel1Data.clear()
                eegChannel2Data.clear()
                eegLeadOffData.clear()
                ppgRedData.clear()
                ppgIrData.clear()
                accelXData.clear()
                accelYData.clear()
                accelZData.clear()
            }
            
            _isRecording.value = false
            
            val recordingDuration = (System.currentTimeMillis() - recordingStartTime) / 1000.0
            Log.d("BleManager", "Recording stopped. Duration: ${recordingDuration}s")
            
        } catch (e: Exception) {
            Log.e("BleManager", "Error stopping recording", e)
        }
    }
    
    private fun writeEegToCsv(data: EegData) {
        if (_isRecording.value && _selectedSensors.value.contains(SensorType.EEG)) {
            try {
                // CSV 파일에 기록
                if (eegCsvWriter != null) {
                    val leadOffValue = if (data.leadOff) 1 else 0
                    eegCsvWriter?.write("${data.timestamp.time},${data.ch1Raw},${data.ch2Raw},${data.channel1},${data.channel2},$leadOffValue\n")
                    eegCsvWriter?.flush()
                }
                
                // JSON 데이터 버퍼에 추가
                if (jsonWriter != null) {
                    synchronized(jsonDataLock) {
                        timestampData.add(data.timestamp.time / 1000.0)
                        eegChannel1Data.add(data.channel1) // µV 값 사용
                        eegChannel2Data.add(data.channel2) // µV 값 사용
                        eegLeadOffData.add(if (data.leadOff) 1 else 0)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("BleManager", "Error writing EEG data", e)
            }
        }
    }
    
    private fun writePpgToCsv(data: PpgData) {
        if (_isRecording.value && _selectedSensors.value.contains(SensorType.PPG)) {
            try {
                // CSV 파일에 기록
                if (ppgCsvWriter != null) {
                    ppgCsvWriter?.write("${data.timestamp.time},${data.red},${data.ir}\n")
                    ppgCsvWriter?.flush()
                }
                
                // JSON 데이터 버퍼에 추가
                if (jsonWriter != null) {
                    synchronized(jsonDataLock) {
                        timestampData.add(data.timestamp.time / 1000.0)
                        ppgRedData.add(data.red)
                        ppgIrData.add(data.ir)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("BleManager", "Error writing PPG data", e)
            }
        }
    }
    
    private fun writeAccToCsv(data: ProcessedAccData) {
        if (_isRecording.value && _selectedSensors.value.contains(SensorType.ACC)) {
            try {
                // CSV 파일에 기록
                if (accCsvWriter != null) {
                    accCsvWriter?.write("${data.timestamp.time},${data.x},${data.y},${data.z}\n")
                    accCsvWriter?.flush()
                }
                
                // JSON 데이터 버퍼에 추가
                if (jsonWriter != null) {
                    synchronized(jsonDataLock) {
                        timestampData.add(data.timestamp.time / 1000.0)
                        accelXData.add(data.x.toInt())
                        accelYData.add(data.y.toInt())
                        accelZData.add(data.z.toInt())
                    }
                }
                
            } catch (e: Exception) {
                Log.e("BleManager", "Error writing ACC data", e)
            }
        }
    }

    // 센서 활성화 큐 관리 함수들
    private fun activateNextSensorInQueue() {
        if (sensorActivationQueue.isEmpty()) {
            Log.d("BleManager", "🎉 All sensors in queue have been activated successfully!")
            _isReceivingData.value = true
            currentActivatingSensor = null
            return
        }
        
        val nextSensor = sensorActivationQueue.removeAt(0)
        currentActivatingSensor = nextSensor
        
        Log.d("BleManager", "🚀 Activating sensor: $nextSensor (${sensorActivationQueue.size} remaining in queue)")
        
        // 타임아웃 설정
        sensorTimeoutRunnable = Runnable {
            Log.w("BleManager", "⏰ Timeout waiting for $nextSensor data, proceeding to next sensor...")
            currentActivatingSensor = null
            activateNextSensorInQueue()
        }
        handler.postDelayed(sensorTimeoutRunnable!!, sensorTimeoutMs)
        
        // 센서별 활성화 실행
        when (nextSensor) {
            SensorType.EEG -> {
                lastEegDataSize = _eegData.value.size
                startEegService()
            }
            SensorType.ACC -> {
                lastAccDataSize = _accData.value.size
                startAccService()
            }
            SensorType.PPG -> {
                lastPpgDataSize = _ppgData.value.size
                startPpgService()
            }
        }
    }
    
    private fun onSensorDataReceived(sensorType: SensorType) {
        // 현재 활성화 대기 중인 센서와 일치하는지 확인
        if (currentActivatingSensor == sensorType) {
            Log.d("BleManager", "✅ $sensorType data confirmed - proceeding to next sensor")
            
            // 타임아웃 취소
            sensorTimeoutRunnable?.let { handler.removeCallbacks(it) }
            sensorTimeoutRunnable = null
            
            currentActivatingSensor = null
            
            // 500ms 대기 후 다음 센서 활성화 (안정성을 위해)
            handler.postDelayed({
                activateNextSensorInQueue()
            }, 500)
        }
    }
    
  
    
 
    
 
    
    // ============ 배치 수집 관련 메서드들 ============
    
    /**
     * 수집 모드를 설정합니다
     */
    fun setCollectionMode(mode: CollectionMode) {
        if (_selectedCollectionMode.value != mode) {
            _selectedCollectionMode.value = mode
            Log.d("BleManager", "수집 모드 변경: ${mode.description}")
            
            // 현재 활성화된 센서들에 대해 새로운 모드 적용
            _selectedSensors.value.forEach { sensorType ->
                configureSensorCollection(sensorType)
            }
        }
    }
    
    /**
     * 특정 센서의 샘플 수 설정을 업데이트합니다
     */
    fun updateSensorSampleCount(sensorType: SensorType, sampleCount: Int, sampleCountText: String) {
        if (sampleCount in ValidationRange.sampleCount) {
            sensorConfigurations[sensorType]?.let { config ->
                config.sampleCount = sampleCount
                config.sampleCountText = sampleCountText
                
                // 현재 샘플 수 모드이고 해당 센서가 활성화되어 있으면 즉시 적용
                if (_selectedCollectionMode.value == CollectionMode.SAMPLE_COUNT && 
                    _selectedSensors.value.contains(sensorType)) {
                    configureSensorCollection(sensorType)
                }
                
                Log.d("BleManager", "${sensorType.name} 샘플 수 설정: $sampleCount")
            }
        } else {
            Log.w("BleManager", "유효하지 않은 샘플 수: $sampleCount (허용 범위: ${ValidationRange.sampleCount})")
        }
    }
    
    /**
     * 특정 센서의 초 단위 설정을 업데이트합니다
     */
    fun updateSensorSeconds(sensorType: SensorType, seconds: Int, secondsText: String) {
        if (seconds in ValidationRange.seconds) {
            sensorConfigurations[sensorType]?.let { config ->
                config.seconds = seconds
                config.secondsText = secondsText
                
                // 현재 초 단위 모드이고 해당 센서가 활성화되어 있으면 즉시 적용
                if (_selectedCollectionMode.value == CollectionMode.SECONDS && 
                    _selectedSensors.value.contains(sensorType)) {
                    configureSensorCollection(sensorType)
                }
                
                Log.d("BleManager", "${sensorType.name} 초 단위 설정: ${seconds}초")
            }
        } else {
            Log.w("BleManager", "유효하지 않은 초 값: $seconds (허용 범위: ${ValidationRange.seconds})")
        }
    }
    
    /**
     * 특정 센서의 분 단위 설정을 업데이트합니다
     */
    fun updateSensorMinutes(sensorType: SensorType, minutes: Int, minutesText: String) {
        if (minutes in ValidationRange.minutes) {
            sensorConfigurations[sensorType]?.let { config ->
                config.minutes = minutes
                config.minutesText = minutesText
                
                // 현재 분 단위 모드이고 해당 센서가 활성화되어 있으면 즉시 적용
                if (_selectedCollectionMode.value == CollectionMode.MINUTES && 
                    _selectedSensors.value.contains(sensorType)) {
                    configureSensorCollection(sensorType)
                }
                
                Log.d("BleManager", "${sensorType.name} 분 단위 설정: ${minutes}분")
            }
        } else {
            Log.w("BleManager", "유효하지 않은 분 값: $minutes (허용 범위: ${ValidationRange.minutes})")
        }
    }
    
    /**
     * 특정 센서의 현재 설정을 가져옵니다
     */
    fun getSensorConfiguration(sensorType: SensorType): SensorBatchConfiguration? {
        return sensorConfigurations[sensorType]
    }
    
    /**
     * 센서별 배치 수집 설정을 적용합니다
     */
    private fun configureSensorCollection(sensorType: SensorType) {
        val config = sensorConfigurations[sensorType] ?: return
        
        when (_selectedCollectionMode.value) {
            CollectionMode.SAMPLE_COUNT -> {
                setDataCollectionSampleCount(config.sampleCount, sensorType)
            }
            CollectionMode.SECONDS -> {
                setDataCollectionTimeInterval(config.seconds.toLong() * 1000, sensorType) // 초를 밀리초로 변환
            }
            CollectionMode.MINUTES -> {
                setDataCollectionTimeInterval(config.minutes.toLong() * 60 * 1000, sensorType) // 분을 밀리초로 변환
            }
        }
    }
    
    /**
     * 샘플 개수를 기준으로 배치 데이터 수집을 설정합니다
     */
    private fun setDataCollectionSampleCount(sampleCount: Int, sensorType: SensorType) {
        val config = DataCollectionConfig(
            sensorType = sensorType,
            mode = DataCollectionConfig.DataCollectionMode.SampleCount(sampleCount)
        )
        dataCollectionConfigs[sensorType] = config
        clearSensorBuffer(sensorType)
        
        // 샘플 기반 모드에서는 시간 기반 관리자 제거
        when (sensorType) {
            SensorType.EEG -> eegTimeBatchManager = null
            SensorType.PPG -> ppgTimeBatchManager = null
            SensorType.ACC -> accTimeBatchManager = null
        }
        
        Log.d("BleManager", "🔧 샘플 기반 배치 설정: $sensorType - ${sampleCount}개씩")
    }
    
    /**
     * 시간 간격을 기준으로 배치 데이터 수집을 설정합니다
     */
    private fun setDataCollectionTimeInterval(timeIntervalMs: Long, sensorType: SensorType) {
        val config = DataCollectionConfig(
            sensorType = sensorType,
            mode = DataCollectionConfig.DataCollectionMode.TimeInterval(timeIntervalMs)
        )
        dataCollectionConfigs[sensorType] = config
        clearSensorBuffer(sensorType)
        Log.d("BleManager", "🔧 시간 기반 배치 설정: $sensorType - ${timeIntervalMs}ms 간격")
        // 시간 기반 배치 관리자 초기화
        when (sensorType) {
            SensorType.EEG -> {
                eegTimeBatchManager = TimeBatchManager(timeIntervalMs) { it.timestamp }
                Log.d("BleManager", "📊 EEG TimeBatchManager 초기화됨")
            }
            SensorType.PPG -> {
                ppgTimeBatchManager = TimeBatchManager(timeIntervalMs) { it.timestamp }
                Log.d("BleManager", "📊 PPG TimeBatchManager 초기화됨")
            }
            SensorType.ACC -> {
                accTimeBatchManager = TimeBatchManager(timeIntervalMs) { it.timestamp }
                Log.d("BleManager", "📊 ACC TimeBatchManager 초기화됨")
            }
        }
    }
    
    /**
     * 특정 센서의 버퍼를 비웁니다
     */
    private fun clearSensorBuffer(sensorType: SensorType) {
        when (sensorType) {
            SensorType.EEG -> {
                eegSampleBuffer.clear()
                eegTimeBatchManager?.clearBuffer()
            }
            SensorType.PPG -> {
                ppgSampleBuffer.clear()
                ppgTimeBatchManager?.clearBuffer()
            }
            SensorType.ACC -> {
                accSampleBuffer.clear()
                accTimeBatchManager?.clearBuffer()
            }
        }
    }
    
    /**
     * EEG 데이터를 배치 버퍼에 추가합니다
     */
    private fun addToEegBuffer(reading: EegData) {
        synchronized(eegBufferLock) {
            val config = dataCollectionConfigs[SensorType.EEG] ?: return
            when (val mode = config.mode) {
                is DataCollectionConfig.DataCollectionMode.TimeInterval -> {
                    eegTimeBatchManager?.addSample(reading)?.let { batch ->
                        Log.d("BleManager", "\uD83D\uDCE6 EEG 시간 배치 완성: \\${batch.size}개 샘플")
                        logEegBatch(batch)
                        _eegBatchData.value = batch
                    }
                }
                is DataCollectionConfig.DataCollectionMode.SampleCount -> {
                    eegSampleBuffer.add(reading)
                    if (eegSampleBuffer.size >= mode.count) {
                        val batch = eegSampleBuffer.take(mode.count)
                        eegSampleBuffer.removeAll(batch.toSet())
                        Log.d("BleManager", "\uD83D\uDCE6 EEG 샘플 배치 완성: \\${batch.size}개 샘플")
                        logEegBatch(batch)
                        _eegBatchData.value = batch
                    }
                }
            }
        }
    }
    
    /**
     * PPG 데이터를 배치 버퍼에 추가합니다
     */
    private fun addToPpgBuffer(reading: PpgData) {
        synchronized(ppgBufferLock) {
            val config = dataCollectionConfigs[SensorType.PPG] ?: return
            when (val mode = config.mode) {
                is DataCollectionConfig.DataCollectionMode.TimeInterval -> {
                    ppgTimeBatchManager?.addSample(reading)?.let { batch ->
                        Log.d("BleManager", "\uD83D\uDCE6 PPG 시간 배치 완성: \\${batch.size}개 샘플")
                        logPpgBatch(batch)
                        _ppgBatchData.value = batch
                    }
                }
                is DataCollectionConfig.DataCollectionMode.SampleCount -> {
                    ppgSampleBuffer.add(reading)
                    if (ppgSampleBuffer.size >= mode.count) {
                        val batch = ppgSampleBuffer.take(mode.count)
                        ppgSampleBuffer.removeAll(batch.toSet())
                        Log.d("BleManager", "\uD83D\uDCE6 PPG 샘플 배치 완성: \\${batch.size}개 샘플")
                        logPpgBatch(batch)
                        _ppgBatchData.value = batch
                    }
                }
            }
        }
    }
    
    /**
     * ACC 데이터를 배치 버퍼에 추가합니다
     */
    private fun addToAccBuffer(reading: AccData) {
        synchronized(accBufferLock) {
            val config = dataCollectionConfigs[SensorType.ACC] ?: return
            when (val mode = config.mode) {
                is DataCollectionConfig.DataCollectionMode.TimeInterval -> {
                    accTimeBatchManager?.addSample(reading)?.let { batch ->
                        Log.d("BleManager", "\uD83D\uDCE6 ACC 시간 배치 완성: \\${batch.size}개 샘플")
                        val processedBatch = batch.mapNotNull { acc ->
                            _processedAccData.value.find { it.timestamp == acc.timestamp }
                        }
                        logAccBatch(processedBatch)
                        _accBatchData.value = batch
                    }
                }
                is DataCollectionConfig.DataCollectionMode.SampleCount -> {
                    accSampleBuffer.add(reading)
                    if (accSampleBuffer.size >= mode.count) {
                        val batch = accSampleBuffer.take(mode.count)
                        accSampleBuffer.removeAll(batch.toSet())
                        Log.d("BleManager", "\uD83D\uDCE6 ACC 샘플 배치 완성: \\${batch.size}개 샘플")
                        val processedBatch = batch.mapNotNull { acc ->
                            _processedAccData.value.find { it.timestamp == acc.timestamp }
                        }
                        logAccBatch(processedBatch)
                        _accBatchData.value = batch
                    }
                }
            }
        }
    }
    
    // ============ 배치 데이터 로깅 헬퍼 함수들 ============
    
    private fun logEegBatch(batch: List<EegData>) {
        Log.i("BleManager", "--- EEG Batch (${batch.size} samples) ---")
        batch.forEachIndexed { index, data ->
            val leadOffValue = if (data.leadOff) 1 else 0
            Log.d(
                "BleManager",
                "  [${index + 1}] timestamp: ${data.timestamp.time}, " +
                "ch1Raw: ${data.ch1Raw}, ch2Raw: ${data.ch2Raw}, " +
                String.format("ch1uV: %.1fµV, ch2uV: %.1fµV, ", data.channel1, data.channel2) +
                "leadOff: $leadOffValue"
            )
        }
        Log.i("BleManager", "------------------------------------")
    }
    
    private fun logPpgBatch(batch: List<PpgData>) {
        Log.i("BleManager", "--- PPG Batch (${batch.size} samples) ---")
        batch.forEachIndexed { index, data ->
            Log.d("BleManager", "  [${index + 1}] timestamp: ${data.timestamp.time}, red: ${data.red}, ir: ${data.ir}")
        }
        Log.i("BleManager", "------------------------------------")
    }
    
    private fun logAccBatch(batch: List<ProcessedAccData>) {
        Log.i("BleManager", "--- ACC Batch (${batch.size} samples) ---")
        batch.forEachIndexed { index, data ->
            Log.d(
                "BleManager",
                "  [${index + 1}] timestamp: ${data.timestamp.time}, x: ${data.x}, y: ${data.y}, z: ${data.z}"
            )
        }
        Log.i("BleManager", "------------------------------------")
    }

    // JSON 기록 제어 함수들
    private fun saveJsonFiles() {
        try {
            // EEG JSON 파일 저장
            if (jsonWriter != null && timestampData.isNotEmpty()) {
                synchronized(jsonDataLock) {
                    val jsonString = JSONObject().apply {
                        put("timestamp", timestampData)
                        put("eegChannel1", eegChannel1Data)
                        put("eegChannel2", eegChannel2Data)
                        put("eegLeadOff", eegLeadOffData)
                        put("ppgRed", ppgRedData)
                        put("ppgIr", ppgIrData)
                        put("accelX", accelXData)
                        put("accelY", accelYData)
                        put("accelZ", accelZData)
                    }.toString(2)
                    jsonWriter?.write(jsonString)
                    jsonWriter?.flush()
                }
                Log.d("BleManager", "EEG, PPG, ACC JSON data saved: ${timestampData.size} records")
            }
            
        } catch (e: Exception) {
            Log.e("BleManager", "Error saving JSON files", e)
        }
    }
} 