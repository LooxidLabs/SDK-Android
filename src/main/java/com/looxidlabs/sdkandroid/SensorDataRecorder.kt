package io.github.looxidlabs.sdkandroid

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 센서 데이터 기록 전용 클래스
 * CSV와 JSON 형식으로 센서 데이터를 저장하는 기능을 담당
 */
class SensorDataRecorder {
    
    // 기록 상태 관리
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // CSV 파일 writer들
    private var eegCsvWriter: FileWriter? = null
    private var ppgCsvWriter: FileWriter? = null
    private var accCsvWriter: FileWriter? = null
    
    // JSON 파일 writer
    private var jsonWriter: FileWriter? = null
    
    // 기록 시작 시간
    private var recordingStartTime: Long = 0
    
    // JSON 데이터 버퍼들
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
    
    /**
     * 센서 데이터 기록을 시작합니다
     * @param selectedSensors 기록할 센서 목록
     * @return 기록 시작 성공 여부
     */
    fun startRecording(selectedSensors: Set<SensorType>): Boolean {
        if (_isRecording.value) {
            Log.w("SensorDataRecorder", "이미 기록이 진행 중입니다")
            return false
        }
        
        if (selectedSensors.isEmpty()) {
            Log.w("SensorDataRecorder", "선택된 센서가 없습니다")
            return false
        }
        
        return try {
            recordingStartTime = System.currentTimeMillis()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            
            // LinkBand 전용 폴더 생성
            val linkBandDir = createLinkBandDirectory()
            
            // JSON 데이터 버퍼 초기화
            clearJsonBuffers()
            
            // JSON 파일 생성 (선택된 센서가 있으면 하나만 생성)
            if (selectedSensors.isNotEmpty()) {
                val jsonFile = File(linkBandDir, "LinkBand_SensorData_${timestamp}.json")
                jsonWriter = FileWriter(jsonFile)
                Log.d("SensorDataRecorder", "JSON 파일 생성: ${jsonFile.name}")
            }
            
            // 선택된 센서별 CSV 파일 생성
            createCsvFiles(selectedSensors, linkBandDir, timestamp)
            
            _isRecording.value = true
            Log.i("SensorDataRecorder", "센서 데이터 기록 시작: $selectedSensors")
            true
            
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "기록 시작 실패", e)
            stopRecording()
            false
        }
    }
    
    /**
     * 센서 데이터 기록을 중지합니다
     * @return 기록 중지 성공 여부
     */
    fun stopRecording(): Boolean {
        if (!_isRecording.value) {
            return true
        }
        
        return try {
            // JSON 데이터를 파일에 저장
            saveJsonFile()
            
            // 모든 파일 writer들 닫기
            closeAllWriters()
            
            // JSON 데이터 버퍼 정리
            clearJsonBuffers()
            
            val recordingDuration = (System.currentTimeMillis() - recordingStartTime) / 1000.0
            Log.i("SensorDataRecorder", "센서 데이터 기록 완료 (${String.format("%.1f", recordingDuration)}초)")
            
            _isRecording.value = false
            true
            
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "기록 중지 실패", e)
            _isRecording.value = false
            false
        }
    }
    
    /**
     * EEG 데이터를 기록합니다
     */
    fun recordEegData(data: EegData) {
        if (!_isRecording.value) return
        
        try {
            // CSV 파일에 기록
            eegCsvWriter?.let { writer ->
                val leadOffValue = if (data.leadOff) 1 else 0
                writer.write("${data.timestamp.time},${data.ch1Raw},${data.ch2Raw},${data.channel1},${data.channel2},$leadOffValue\n")
                writer.flush()
            }
            
            // JSON 데이터 버퍼에 추가
            jsonWriter?.let {
                synchronized(jsonDataLock) {
                    timestampData.add(data.timestamp.time / 1000.0)
                    eegChannel1Data.add(data.channel1)
                    eegChannel2Data.add(data.channel2)
                    eegLeadOffData.add(if (data.leadOff) 1 else 0)
                }
            }
            
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "EEG 데이터 기록 실패", e)
        }
    }
    
    /**
     * PPG 데이터를 기록합니다
     */
    fun recordPpgData(data: PpgData) {
        if (!_isRecording.value) return
        
        try {
            // CSV 파일에 기록
            ppgCsvWriter?.let { writer ->
                writer.write("${data.timestamp.time},${data.red},${data.ir}\n")
                writer.flush()
            }
            
            // JSON 데이터 버퍼에 추가
            jsonWriter?.let {
                synchronized(jsonDataLock) {
                    timestampData.add(data.timestamp.time / 1000.0)
                    ppgRedData.add(data.red)
                    ppgIrData.add(data.ir)
                }
            }
            
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "PPG 데이터 기록 실패", e)
        }
    }
    
    /**
     * 가속도계 데이터를 기록합니다
     */
    fun recordAccData(data: ProcessedAccData) {
        if (!_isRecording.value) return
        
        try {
            // CSV 파일에 기록
            accCsvWriter?.let { writer ->
                writer.write("${data.timestamp.time},${data.x},${data.y},${data.z}\n")
                writer.flush()
            }
            
            // JSON 데이터 버퍼에 추가
            jsonWriter?.let {
                synchronized(jsonDataLock) {
                    timestampData.add(data.timestamp.time / 1000.0)
                    accelXData.add(data.x.toInt())
                    accelYData.add(data.y.toInt())
                    accelZData.add(data.z.toInt())
                }
            }
            
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "가속도계 데이터 기록 실패", e)
        }
    }
    
    /**
     * 기록 진행 시간을 반환합니다 (초)
     */
    fun getRecordingDuration(): Double {
        return if (_isRecording.value) {
            (System.currentTimeMillis() - recordingStartTime) / 1000.0
        } else {
            0.0
        }
    }
    
    /**
     * 기록된 데이터 개수를 반환합니다
     */
    fun getRecordedDataCount(): Int {
        return synchronized(jsonDataLock) {
            timestampData.size
        }
    }
    
    // ==================== 내부 헬퍼 함수들 ====================
    
    private fun createLinkBandDirectory(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val linkBandDir = File(downloadsDir, "LinkBand")
        if (!linkBandDir.exists()) {
            linkBandDir.mkdirs()
        }
        
        return linkBandDir
    }
    
    private fun createCsvFiles(selectedSensors: Set<SensorType>, linkBandDir: File, timestamp: String) {
        if (selectedSensors.contains(SensorType.EEG)) {
            val eegFile = File(linkBandDir, "LinkBand_EEG_${timestamp}.csv")
            eegCsvWriter = FileWriter(eegFile)
            eegCsvWriter?.write("timestamp,ch1Raw,ch2Raw,ch1uV,ch2uV,leadOff\n")
            Log.d("SensorDataRecorder", "EEG CSV 파일 생성: ${eegFile.name}")
        }
        
        if (selectedSensors.contains(SensorType.PPG)) {
            val ppgFile = File(linkBandDir, "LinkBand_PPG_${timestamp}.csv")
            ppgCsvWriter = FileWriter(ppgFile)
            ppgCsvWriter?.write("timestamp,red,ir\n")
            Log.d("SensorDataRecorder", "PPG CSV 파일 생성: ${ppgFile.name}")
        }
        
        if (selectedSensors.contains(SensorType.ACC)) {
            val accFile = File(linkBandDir, "LinkBand_ACC_${timestamp}.csv")
            accCsvWriter = FileWriter(accFile)
            accCsvWriter?.write("timestamp,x,y,z\n")
            Log.d("SensorDataRecorder", "ACC CSV 파일 생성: ${accFile.name}")
        }
    }
    
    private fun saveJsonFile() {
        try {
            jsonWriter?.let { writer ->
                if (timestampData.isNotEmpty()) {
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
                        
                        writer.write(jsonString)
                        writer.flush()
                        Log.d("SensorDataRecorder", "JSON 데이터 저장 완료: ${timestampData.size}개 레코드")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "JSON 파일 저장 실패", e)
        }
    }
    
    private fun closeAllWriters() {
        try {
            eegCsvWriter?.close()
            ppgCsvWriter?.close()
            accCsvWriter?.close()
            jsonWriter?.close()
        } catch (e: Exception) {
            Log.e("SensorDataRecorder", "파일 writer 닫기 실패", e)
        } finally {
            // writer 변수들 초기화
            eegCsvWriter = null
            ppgCsvWriter = null
            accCsvWriter = null
            jsonWriter = null
        }
    }
    
    private fun clearJsonBuffers() {
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
    }
    
    /**
     * 리소스 정리 함수
     * SDK 종료 시 호출하여 모든 리소스를 정리합니다
     */
    fun cleanup() {
        if (_isRecording.value) {
            stopRecording()
        }
        clearJsonBuffers()
    }
} 