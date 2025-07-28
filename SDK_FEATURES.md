# LinkBand Android SDK - 기능 명세서

## 📋 개요

LinkBand Android SDK는 LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 다양한 생체신호 센서 데이터 수집을 위한 종합적인 솔루션입니다.

## 🔧 핵심 기능 분류

### 1. Bluetooth 연결 관리

#### 1.1 디바이스 스캔
- **기능**: 주변 LinkBand 디바이스 자동 검색
- **메서드**: `startScan()`, `stopScan()`
- **상태**: `isScanning`, `scannedDevices`
- **특징**: 
  - 실시간 스캔 결과 업데이트
  - 중복 디바이스 자동 필터링
  - 스캔 타임아웃 자동 관리

#### 1.2 연결 관리
- **기능**: 디바이스 연결/해제 및 상태 모니터링
- **메서드**: `connectToDevice()`, `disconnect()`
- **상태**: `isConnected`, `connectedDeviceName`
- **특징**:
  - 연결 상태 실시간 모니터링
  - 연결 실패 시 자동 재시도
  - MTU 크기 자동 최적화

#### 1.3 자동 재연결
- **기능**: 연결 끊김 시 자동 재연결
- **메서드**: `enableAutoReconnect()`, `disableAutoReconnect()`
- **상태**: `isAutoReconnectEnabled`
- **특징**:
  - 최대 5회 재연결 시도
  - 지수 백오프 전략
  - 수동 재연결 비활성화 가능

### 2. 센서 데이터 수집

#### 2.1 EEG (뇌전도) 센서
- **기능**: 2채널 뇌전도 신호 수집
- **메서드**: `startEegService()`, `stopEegService()`
- **상태**: `isEegStarted`, `eegData`
- **데이터 구조**:
  ```kotlin
  data class EegData(
      val timestamp: Date,        // 타임스탬프
      val leadOff: Boolean,       // 전극 접촉 상태
      val channel1: Double,       // 채널1 전압값 (µV)
      val channel2: Double,       // 채널2 전압값 (µV)
      val ch1Raw: Int,           // 채널1 원시값 (24비트)
      val ch2Raw: Int            // 채널2 원시값 (24비트)
  )
  ```
- **특징**:
  - 250Hz 샘플링 레이트
  - 24비트 해상도
  - 전극 접촉 상태 실시간 모니터링
  - µV 단위 자동 변환

#### 2.2 PPG (광전 용적 맥파) 센서
- **기능**: 혈류량 변화 측정
- **메서드**: `startPpgService()`, `stopPpgService()`
- **상태**: `isPpgStarted`, `ppgData`
- **데이터 구조**:
  ```kotlin
  data class PpgData(
      val timestamp: Date,        // 타임스탬프
      val red: Int,              // 적색 신호값
      val ir: Int                // 적외선 신호값
  )
  ```
- **특징**:
  - 50Hz 샘플링 레이트
  - 적색/적외선 이중 채널
  - 산소 포화도 관련 데이터 제공

#### 2.3 ACC (가속도계) 센서
- **기능**: 3축 가속도 측정
- **메서드**: `startAccService()`, `stopAccService()`
- **상태**: `isAccStarted`, `accData`, `processedAccData`
- **데이터 구조**:
  ```kotlin
  data class AccData(
      val timestamp: Date,        // 타임스탬프
      val x: Short,              // X축 가속도 (16비트)
      val y: Short,              // Y축 가속도 (16비트)
      val z: Short               // Z축 가속도 (16비트)
  )
  ```
- **모드**:
  - `RAW`: 중력을 포함한 원시 가속도 값
  - `MOTION`: 중력을 제거한 움직임만 표시
- **특징**:
  - 25Hz 샘플링 레이트
  - 중력 성분 자동 제거 (움직임 모드)
  - 저역 통과 필터 적용

#### 2.4 배터리 모니터링
- **기능**: 디바이스 배터리 상태 모니터링
- **상태**: `batteryData`
- **데이터 구조**:
  ```kotlin
  data class BatteryData(
      val level: Int             // 배터리 레벨 (0-100%)
  )
  ```
- **특징**:
  - 실시간 배터리 레벨 업데이트
  - 0-100% 범위 표시

### 3. 센서 선택 및 제어

#### 3.1 센서 선택
- **기능**: 수집할 센서 선택/해제
- **메서드**: `selectSensor()`, `deselectSensor()`
- **상태**: `selectedSensors`
- **지원 센서**: `SensorType.EEG`, `SensorType.PPG`, `SensorType.ACC`

#### 3.2 일괄 센서 제어
- **기능**: 선택된 모든 센서 동시 시작/중지
- **메서드**: `startSelectedSensors()`, `stopSelectedSensors()`
- **특징**:
  - 순차적 센서 활성화
  - 센서별 개별 상태 관리
  - 데이터 수신 상태 통합 모니터링

### 4. 배치 데이터 수집

#### 4.1 수집 모드
- **기능**: 데이터 수집 방식 설정
- **메서드**: `setCollectionMode()`
- **지원 모드**:
  - `SAMPLE_COUNT`: 샘플 수 기반 수집
  - `SECONDS`: 초 단위 시간 기반 수집
  - `MINUTES`: 분 단위 시간 기반 수집

#### 4.2 센서별 설정
- **기능**: 각 센서별 개별 수집 설정
- **메서드**: 
  - `updateSensorSampleCount()`: 샘플 수 설정
  - `updateSensorSeconds()`: 초 단위 설정
  - `updateSensorMinutes()`: 분 단위 설정
  - `getSensorConfiguration()`: 설정 조회
- **기본값**:
  - EEG: 250샘플 / 1초 / 1분
  - PPG: 50샘플 / 1초 / 1분
  - ACC: 25샘플 / 1초 / 1분

#### 4.3 배치 데이터 스트림
- **기능**: 수집된 배치 데이터 실시간 제공
- **상태**: `eegBatchData`, `ppgBatchData`, `accBatchData`
- **특징**:
  - 실시간 배치 데이터 업데이트
  - 센서별 독립적 배치 처리
  - 버퍼 자동 관리

### 5. 데이터 저장

#### 5.1 CSV 기록
- **기능**: 센서 데이터를 CSV 파일로 저장
- **메서드**: `startRecording()`, `stopRecording()`
- **상태**: `isRecording`
- **특징**:
  - 센서별 별도 파일 생성
  - 타임스탬프 포함
  - 자동 파일명 생성
  - 실시간 기록

#### 5.2 파일 관리
- **저장 위치**: 외부 저장소
- **파일 형식**: CSV
- **파일명 패턴**: `{센서명}_{날짜}_{시간}.csv`
- **데이터 형식**: 타임스탬프, 센서값들

### 6. 데이터 파싱 및 처리

#### 6.1 바이너리 파싱
- **기능**: BLE 바이너리 데이터를 구조화된 데이터로 변환
- **클래스**: `SensorDataParser`
- **지원 센서**: EEG, PPG, ACC, 배터리
- **특징**:
  - 실시간 파싱
  - 타임스탬프 자동 생성
  - 에러 처리 및 예외 발생

#### 6.2 가속도계 처리
- **기능**: 가속도계 데이터 중력 제거 처리
- **클래스**: `ProcessedAccData`
- **알고리즘**: 저역 통과 필터
- **필터 계수**: 0.1

### 7. 상태 모니터링

#### 7.1 연결 상태
- `isScanning`: 스캔 중 여부
- `isConnected`: 연결 상태
- `connectedDeviceName`: 연결된 디바이스 이름
- `isAutoReconnectEnabled`: 자동 재연결 활성화 여부

#### 7.2 센서 상태
- `selectedSensors`: 선택된 센서들
- `isEegStarted`: EEG 센서 시작 여부
- `isPpgStarted`: PPG 센서 시작 여부
- `isAccStarted`: 가속도계 센서 시작 여부
- `isReceivingData`: 데이터 수신 중 여부
- `accelerometerMode`: 가속도계 모드

#### 7.3 수집 상태
- `selectedCollectionMode`: 선택된 수집 모드
- `isRecording`: CSV 기록 중 여부

### 8. 유효성 검사

#### 8.1 입력값 검증
- **샘플 수**: 1-100,000 범위
- **초 단위**: 1-3,600 범위 (최대 1시간)
- **분 단위**: 1-60 범위 (최대 60분)

#### 8.2 상태 검증
- 연결 상태 확인
- 센서 선택 상태 확인
- 권한 상태 확인

## 📊 데이터 흐름

### 1. 연결 단계
```
디바이스 스캔 → 디바이스 선택 → 연결 → 서비스 발견 → 특성 활성화
```

### 2. 데이터 수집 단계
```
센서 선택 → 센서 시작 → 데이터 수신 → 파싱 → 배치 처리 → 저장
```

### 3. 배치 처리 단계
```
개별 데이터 수신 → 버퍼에 추가 → 배치 조건 확인 → 배치 데이터 생성 → StateFlow 업데이트
```

## 🔧 고급 기능

### 1. 동시 센서 수집
- 여러 센서 동시 수집 지원
- 센서별 독립적 설정
- 데이터 충돌 방지

### 2. 실시간 모니터링
- StateFlow 기반 반응형 데이터 스트림
- UI 자동 업데이트
- 메모리 효율적 데이터 관리

### 3. 에러 처리
- 연결 실패 시 자동 재시도
- 센서 오류 시 자동 복구
- 데이터 파싱 에러 처리

### 4. 성능 최적화
- 비동기 데이터 처리
- 메모리 효율적 버퍼링
- 배치 처리로 성능 향상

## 📈 사용 시나리오

### 1. 기본 생체신호 모니터링
```kotlin
// EEG + PPG 동시 수집
linkBandSdk.selectSensor(SensorType.EEG)
linkBandSdk.selectSensor(SensorType.PPG)
linkBandSdk.startSelectedSensors()
```

### 2. 움직임 분석
```kotlin
// 가속도계 움직임 모드
linkBandSdk.selectSensor(SensorType.ACC)
linkBandSdk.setAccelerometerMode(AccelerometerMode.MOTION)
linkBandSdk.startSelectedSensors()
```

### 3. 장시간 데이터 수집
```kotlin
// 5분마다 배치 수집
linkBandSdk.setCollectionMode(CollectionMode.MINUTES)
linkBandSdk.updateSensorMinutes(SensorType.EEG, 5, "5")
linkBandSdk.startRecording()
```

### 4. 실시간 분석
```kotlin
// 실시간 데이터 스트림
lifecycleScope.launch {
    linkBandSdk.eegData.collect { eegDataList ->
        // 실시간 분석 로직
        analyzeEegData(eegDataList)
    }
}
```

## 🔍 디버깅 및 모니터링

### 1. 로그 확인
- 연결 상태 로그
- 데이터 수신 로그
- 에러 로그

### 2. 상태 확인
```kotlin
Log.d("Status", "연결: ${linkBandSdk.isConnected.value}")
Log.d("Status", "선택된 센서: ${linkBandSdk.selectedSensors.value}")
Log.d("Status", "데이터 수신: ${linkBandSdk.isReceivingData.value}")
```

### 3. 데이터 검증
- 타임스탬프 연속성 확인
- 데이터 범위 검증
- 센서별 데이터 품질 확인

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2024년 12월  
**작성자**: LooxidLabs Team 