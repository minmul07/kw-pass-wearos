# KW Pass for WearOS
<img src="https://github.com/user-attachments/assets/7932c20b-f93e-4d05-8ef9-95df19d6ba69" width="240">

Wear OS를 지원하는 광운대학교 도서관 qr코드 인증 어플리케이션

현재 개발 중 입니다. In development.


## 📱스크린샷
| | | |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/2ecd79ae-b310-4f4c-9230-9c91e235addd" width="240"> | <img src="https://github.com/user-attachments/assets/5f42e6d0-25e9-4678-9f62-b0c72c4c535b" width="240"> | <img src="https://github.com/user-attachments/assets/c7971b9f-4313-4409-8344-83e1f3fda558" width="240"> |
| **KW Pass WearOS 실행 화면** | **컴플리케이션 (텍스트)** | **컴플리케이션 (아이콘)** |
| <img src="https://github.com/user-attachments/assets/7d4f2277-e8ee-4933-904d-348a40d37c70" width="240"> | <img src="https://github.com/user-attachments/assets/591d25b8-e17a-4d2d-8911-06d480cd671b" width="240"> |<img src="https://github.com/user-attachments/assets/b3157d55-20dd-43d5-a9f6-310c71452cc8" width="240">  |
| **KW Pass Phone 실행 화면** | **위젯 실행 화면** | **설정 화면**|
| <img src="https://github.com/user-attachments/assets/5d41ae1a-e425-4653-8c05-3c3bf88ea4e3" width="240"> | | |
|  **Phone 위젯** | | |

## 기능
- 다국어 지원
### Phone
- QR코드 위젯 지원
- 도서관 주변에서 잠금해제 없이 잠금화면에서 QR코드 보기 (개발 중)
### Watch
- 컴플리케이션을 통해 빠른 QR코드 확인

## 🛠 Tech Stack
- Kotlin
- Jetpack Compose
- ZXing
- Retrofit2 & OkHttp3
- TikXml
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

