# BaroDetector: 스마트워치를 이용한 건물 단위 위치 추적

한국통신학회논문지 47권 2호 게재

## 연구 배경

1. 우리는 일상생활에서 여러 위치 기반 서비스를 이용하며 살고 있음. 이러한 서비스는 일반적으로 GPS (Global Positioning System)를 사용함.
2. GPS는 실내와 같이 인공위성의 영향을 받기 어려운 곳은 신뢰도가 매우 낮고, 소모 전력량이 매우 많음. 특히, 사용자의 위치를 특정하기 어려울 때 정확한 위치 정보를 얻고자 지속적으로 위성 스캔을 시도함. 이로 인해 지도상의 위치가 계속해서 바뀌어 혼동을 주며, 배터리 소모도 그만큼 증가함.
3. 위치를 특정하는데 실내/외 감지 기술이 존재함. 앞서 언급한 GPS의 단점을 보완하기 위해 여러 연구에서 스마트폰에 내장된 여러 센서 조합을 사용하여 시도하였음.
4. 하지만 이러한 연구를 실제 (in-the-wild) 환경에 적용하기 위해서는 일정한 환경을 유지해야함. 하지만, 스마트폰은 handy-device로 일정한 환경을 유지하기 힘듦(ex. 가방 안, 주머니 안 등).
5. 스마트워치의 경우, 대부분의 시간 동안 사용자의 손목 위에 위치한다. 이 점은 스마트워치가 사용자의 주변 환경을 대표하는 가장 적절한 기기임을 의미함.
6. 실내/외 감지를 함에 있어, 압력 센서가 중요한 역할을 할 수 있음. 건물은 쾌적한 실내 온도 및 압력을 유지하기 위해 HVAC (Heating, ventilating and air conditioning) 시스템을 갖추고 있음.
7. 이로 인해 건물 내부와 외부 사이에 환경적인 차이를 만듦. 이러한 차이로 인해 출입문을 통과할 때, 통상적으로 10Pa에서 30Pa의 기압 변화를 감지할 수 있음. 이로인해 일정한 수준의 기압을 유지하다가 출입문을 통과할 때 우하향 혹은 우상향 후 다시 일정 수준의 기압을 유지하는 패턴을 관찰할 수 있음. 
8. 또한, 압력 센서는 센서가 활성화된 상황에서 110mW의 전력을 소모해 GPS 센서에 비해 전력량 소모가 적어 스마트워치를 이용한 기술에 사용하기 적절함.
9. 실내/외 감지를 함에 있어 GPS 원시 데이터 내 가시 위성의 숫자도 중요한 역할을 할 수 있음. Kongyang Chen 등은 실험을 통해 가시 위성의 수가 실내/외 상태와 강한 상관관계가 있음을 보임.
10. 본 연구에서는 스마트워치의 기압 센서 및 스마트폰 내 GPS 원시 데이터인 가시 위성의 수를 이용해 건물 단위의 위치 추적을 구현하고자 하였음.

## 데이터 수집

다양한 환경에서도 신뢰성 있게 동작하기 위해 다중 환경을 고려함. 사용자가 통과하는 문을 자동문, 회전문, 미는 문 및 당기는 문 총 4개의 출입 유형으로 나누어 데이터를 수집. 데이터 수집은 실내에서 실외, 실외에서 실내 총 2가지 시나리오에서 진행함. 시립 도서관, 대형마트, 대학 건물 등 5층 이상의 높이를 가진 밀폐되지 않은 건물과 밀폐된 3m 이하의 가정 주택 등 다양한 장소에서 데이터를 수집함. 
학습을 위한 데이터 세트 개수는 총 80개. 4개의 출입 유형으로 나누어 각 문의 종류마다 20개의 데이터 세트가 있으며, 각 데이터 세트는 시나리오별로 나누어져 총 8종류의 데이터 세트로 구성되어 있음.

### 데이터 수집 에이전트 (Android/Tizen)

![Data collection agents](https://user-images.githubusercontent.com/88572107/139855303-18566ec3-2822-410b-bb1e-5075f6c6dbdf.png)

Android 에이전트: 버튼을 통해 기압 데이터를 수집한 위치에 대한 GT 수집 역할

Tizen 에이전트: 내장된 압력센서를 이용하여 기압 데이터 수집 및 Android 에이전트로 데이터 파일 송신

* Android 에이전트에 나온대로 데이터를 수집하는 장소에 따라, Indoor, passing, Outdoor로 나누어 Labeling 함.


## 데이터 전처리

### Sliding window + Moving avrage filter

출입문을 열고, 통과하고, 닫는 일련의 과정이 3초 안에 이루어지기에 3초의 슬라이딩 윈도를 적용함

Outlier가 많은 stream data를 smoothing하기 위해 Moving average filter 적용

![Barometer sensor stream data](https://user-images.githubusercontent.com/88572107/140635081-b00535d3-5109-413b-9c2e-23e3c774c7f3.PNG)

### Min-max normalization

기계학습을 위해 scaling이 필요. 이를 위해 다양한 환경에서 수집한 데이터들을 0~1 사이의 값으로 scaling 하였음.

### Feature extraction

슬라이딩 윈도 내에서 8개의 feature를 추출하였다. 각 feature에 절댓값을 적용해 실내에서 실외, 실외에서 실내 두 가지 경우를 모두 탐지할 수 있게 했다. feature들의 종류는 다음과 같음.

    1. Rate of change: 기울기
    2. Mean crossing rate: 평균을 지나는 횟수
    3. Standard deviation: 표준편차
    4. Inter quartile range: 사분위수 범위 (Q3 - Q1)
    5. Kurtosis: 첨도
    6. Root mean square: 제곱평균제곱근
    7. Root sum square: 제곱합제곱근
    8. Value difference: 최댓값과 최솟값의 차

## GPS 가시 위성

![Number of visible satellite](https://user-images.githubusercontent.com/88572107/139855422-01de3b9e-4b0f-48e1-bdb8-5bc3cf9f1013.png)

GPS 데이터 내 가시 위성 개수는 사용자의 실내/외 상태와 강한 상관관계가 있음. 상기의 그림은 사용자의 위치에 따른 GPS 가시 위성의 개수 추이임. 실내에선 주로 10개 이하로 감지하며, 실외로 이동할 경우 급격히 증가하여 최대 32개 이상 감지할 수 있음.

우리는 가시 위성의 추이를 파악하기 위해 가시 위성 숫자를 길이 20의 큐 (Queue)에 저장하고, 큐에 저장된 가시 위성 수의 평균을 Qmean 이라 함. 이후 수집된 가시 위성의 개수가 Qmean의 ±50% 범위에 존재하면, 사용자의 위치는 실내/외로의 변화가 없다고 판단함. 이후 수집된 가시 위성 개수가 Qmean 값의 0.5배 이하라면, 사용자가 실외에서 실내로 이동한 것으로 판단함.
반대로, 수집된 가시 위성 개수가 Qmean 값의 1.5배 이상이라면, 이는 BaroDetector가 실내에서 작동이 시작되었음을 의미함. 이 경우 Queue를 clear. 50%의 임곗값은 엄밀하게 사용자의 위치 상태를 파악하고자 설정하였음. 상기 그림에서 보듯 가시 위성 개수는 실내에서 극단적으로 줄어들기 때문에, 사용자가 실내 혹은 실외에 있는지 충분히 구분할 수 있음.


## 시스템 평가

### ML classifier 비교 및 선정

![ML classifier comparison](https://user-images.githubusercontent.com/88572107/140892323-9390ed8f-33b3-4e9b-8c17-5bd66a8ce38b.png)

스마트워치 압력센서 데이터에서 추출한 8개의 feature들을 출입문의 종류(a:자동문, b:회전문, c:미는 문, d:당기는 문)에 나누어 6개의 ML classifier에 학습시켜 정확도를 비교하였음.
사용된 classifier의 종류는 다음과 같음

    1. Random Forest
    2. J48 Decision tree
    3. SMO (Sequential Minimal Optimization)
    4. BN (Bayes Net)
    5. NB (Naive Bayes)
    6. LWL (Locally Weighted Learning)
    
이 중 Random Forest가 평균 88.53%의 정확도로 가장 높은 모습을 보여, 추후 실험은 이를 사용하였음.

### Optimization

기압 데이터의 추이를 이용해 출입문 통과 이벤트를 탐지함 (Stream data). 기존에 설정하였던 3초의 슬라이딩 윈도우를 통해 보인 결과에서 높은 false positive rate (18%)를 관찰함. 이를 낮추고, 궁극적으로 정확도를 높이기 위한 방안 모색이 필요하였음.

기압의 패턴은 시간이 가면서 더욱 또렷하게 보이므로, 이를 이용하기 위해 슬라이딩 윈도의 시간을 바꾸어가며 정확도를 비교하였음.

![Optimization_with_sliding_window](https://user-images.githubusercontent.com/88572107/148518882-47d82050-ba67-424e-854a-4e33f27ba154.PNG)

결과 6초의 슬라이딩 윈도우에서 제일 높은 정확도를 보이는 것을 확인함

### 건물 단위 위치 추적 시스템 검증


### 배터리 사용량 
