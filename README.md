# KW Pass for WearOS
<img src="https://github.com/user-attachments/assets/7932c20b-f93e-4d05-8ef9-95df19d6ba69" width="120">

Wear OS를 지원하는 광운대학교 도서관 qr코드 인증 어플리케이션

<br/>

**다운로드**

[<img height="60" alt="Download KW Pass" src="https://github.com/user-attachments/assets/f54c5f08-4c5c-467c-8a57-34662cd0a172" />](https://play.google.com/store/apps/details?id=minmul.kwpass)


## 📱스크린샷
| | | |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/ea748f8e-bdd5-49b6-8c99-1f9cc343ed6f" width="240" >  | <img src="https://github.com/user-attachments/assets/5f42e6d0-25e9-4678-9f62-b0c72c4c535b" width="240"> | <img src="https://github.com/user-attachments/assets/c7971b9f-4313-4409-8344-83e1f3fda558" width="240"> |
| **KW Pass WearOS 실행 화면** | **컴플리케이션 (텍스트)** | **컴플리케이션 (아이콘)** |
| <img  src="https://github.com/user-attachments/assets/8f1f059b-0344-459f-a945-4aae26a3a25d" width="240"/> | <img src="https://github.com/user-attachments/assets/e0eca86d-485d-456e-ab5a-d36eaf694c04" width="240"> | <img src="https://github.com/user-attachments/assets/fe72ac7e-e979-41fe-9577-3845b57f1f77" width="240"> |
| **KW Pass Phone 실행 화면** | **설정 화면** | **언어 선택 화면**|
| <img src="https://github.com/user-attachments/assets/e9f718b2-c1b6-4020-9eb6-86bbffaee5fb" width="240"/> | <img src="https://github.com/user-attachments/assets/0742ccb4-3ffa-45d8-bd5b-d4889bce5728" width="240"> | <img src="https://github.com/user-attachments/assets/57c0c279-7028-4850-846b-895cb813bdb1" width="240"> |
| **다크 모드 지원** | **위젯 실행 화면** | **QR코드 크기 조정 화면** |

## 기능
- 다국어 지원 (한국어, 영어, 일본어, 베트남어, 러시아어, 중국어 간체, 중국어 번체)
### Phone
- QR코드 위젯 지원
- (갤럭시 기기의 경우) 모드 및 루틴 앱 등을 통해 QR코드 실행 자동화
- 앱 내부/위젯의 QR코드 크기 조정 가능
### Watch
- 컴플리케이션을 통해 빠른 QR코드 확인
- 휴대폰에 로그인된 계정 연동

## 🛠 Tech Stack
- Kotlin
- Jetpack Compose
- ZXing
- Retrofit2 & OkHttp3
- XmlPullParser
- Wearable Data Layer API
- Hilt
- MVVM

### 📂 Module Structure
* **`:kwpass-phone`**: Phone App, Glance, Log-in
* **`:kwpass-wearos`**: WearOS App, Watchface Complication
* **`:shared`**: Network, Encryption, Data Module

## Special Thanks

[mirusu400 / KWU-library-QR-PoC](https://github.com/mirusu400/KWU-library-QR-PoC)

[yjyoon-dev / kw-pass-android](https://github.com/yjyoon-dev/kw-pass-android)

