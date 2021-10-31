# BaroDetector

##### 스마트워치 내 압력센서 및 GPS 원시 데이터 내 가시 위성 수를 이용한 건물 단위 위치 추적 기술 연구

## 연구 배경

기존의 GPS 기반 위치 추적은 실내 진입 시 GPS-multipath 등의 이유로 정밀한 위치 추적이 힘듦

(좌표 이리저리 튀는 사진)

대부분의 건물은 HVAC (Heating, Ventilating and Air conditioning) System을 갖추어 건물 외부와 다른 환경 (온도, 습도, 기압 등)을 유지하기 때문에, 사용자에게 부착된 웨어러블 기기의 기압계를 통해 건물 진입을 탐지할 수 있음.

## 데이터 수집

### 데이터 수집 에이전트 (Android/Tizen)

(논문에 들어갔던 스크린샷 첨부)

Android 에이전트 역할:

Tizen 에이전트 역할: 

데이터 Labeling

## 데이터 전처리

### Sliding window + Moving avrage filter

출입문을 열고, 통과하고, 닫는 일련의 과정이 3초 안에 이루어지기에 3초의 슬라이딩 윈도를 적용함

Outlier가 많은 stream data를 smoothing하기 위해 Moving average filter 적용

(논문에 들어갔던 비교하는 그림 두개 넣기)

### Min-max normalization

기계학습을 위해 scaling이 필요

다양한 환경에서 수집한 데이터들을 0~1 사이의 값으로 만듦

## Feature extraction

Feature들이 나오게 된 배경 (유비컴 페이퍼/다른 페이퍼들 목록)

결과적으로 어떤 feature들을 썼는지, 이에 대한 상관성은 얼마나 나왔는지

(웨카 피처별 상관성 그림)

## GPS 가시 위성

밖에서 받는다는 가정 하지말고, 평균값에서 엄청 바뀌었는데 숫자 비교해보니 크다 -> 안에서 밖으로 나간거
반대다 밖에서 안으로 들어간거 이렇게 해야 논리적이다


## Optimization

### 처음 수행 결과 --> 51%

### 피처를 바꾸고, binary selection으로 만든 후 나온 결과 97%

웨카 결과 업로드

#### False positive

높은 false positive rate (18%), 이를 극복하기위한 방안

기압 데이터는 사용자가 실내에 진입한 시간이 지남에 따라 더 확실한 변화를 보인다는 점에 착안해 슬라이딩 윈도우 길이를 조절


## 데이터 분석 개념 정리

#### Precision

#### Recall

