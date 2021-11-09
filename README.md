# BaroDetector: 스마트워치를 이용한 건물 단위 위치 추적

한국통신학회논문지 게제 예정

## 연구 배경

우리는 일상생활에서 현재 위치에 따라서 지역 날씨 및 뉴스를 전달받거나, 운동 구간 관련 정보를 얻거나, 가장 가까운 택시를 호출하는 등 여러 위치 기반 서비스를 이용하며 살고 있다. 이러한 서비스는 일반적으로 GPS (Global Positioning System)를 사용한다. GPS는 가시 위성 (Visible satelite)을 통해 정확한 위치 파악 서비스를 제공할 수 있다. 반면, 실내와 같이 인공위성의 영향을 받기 어려운 곳은 GPS의 신뢰도가 매우 낮다. 이 문제는 일반적으로 GPS multipath error와 NLOS (None-line-of-sight)로 인해 발생한다. 이러한 문제는 도시 환경에서 약 100m의 위치 오류를 일으킬 수 있다. GPS 센서는 다른 센서와 비교했을 때 소모 전력량이 매우 많은 센서이기도 하다. 스마트폰의 각 센서를 활성화한 상황에서 GPS 센서는 370mW의 전력을 소모한다. 이에 반해 조도 센서와 마이크 센서는 60mW 남짓 소모한다. GPS 센서는 사용자의 위치를 특정하기 어려운 환경에서 정확한 위치 정보를 얻고자 지속적으로 위성 스캔을 시도한다. 이로 인해 지도상의 위치가 계속해서 바뀌어 사용자의 정확한 위치를 특정하는 데 혼동을 주며, 배터리 소모도 그만큼 증가한다.

위치 기반 서비스의 또 다른 영역으로는 실내/외 감지 기능이 있다. 실내/외 감지는 사용자 상태를 실내 또는 실외로 구분하는 것이다. 대표적인 실내/외 감지 서비스의 예시는 사용자가 실내에 진입한 것을 탐지하여 공기청정기 등의 가전제품을 자동으로 작동시키는 스마트홈 서비스가 있다. 기존의 많은 연구는 앞서 언급한 GPS의 단점을 보완하기 위해 스마트폰에 내장된 여러 센서 조합을 사용하여 실내/외 감지 문제를 해결하려고 시도했다. 하지만 이러한 연구를 실제 (in-the-wild) 환경에 적용하기 위해서는 중요한 문제가 있다. 스마트폰은 보통 가방, 주머니 또는 다른 장소 등 위치가 수시로 바뀌어 일정한 환경을 유지하기 어렵다. 온-보드 (On-board) 센서를 이용한 상황 인식 연구에서 이는 큰 단점이다. 하지만 스마트워치의 경우, 대부분의 시간 동안 사용자의 손목 위에 위치한다. 이 점은 스마트워치가 사용자의 주변 환경을 대표하는 가장 적절한 기기임을 의미한다.

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

큐를 만들고, 20개의 값을 받아 평균값을 냄. 평균값에서 50% 이상 값이 차이날 경우 사용자의 위치에 변화가 있었다는 뜻
실내에서 작동이 시작된 것도 고려

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

