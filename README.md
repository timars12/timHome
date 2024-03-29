<table>
  <tr>
    <td>Login Screen</td>
     <td>Home Screen</td>
     <td>Device Detail Screen</td>
  </tr>
  <tr>
    <td><img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExN3VhZm83YmcydGtoZWVldXVtbDRtd28xeTdkeHM1MjgwcTN3c2ZkbCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/BUiDBip9q6m4vJPACI/giphy.gif" width="300"></td>
    <td><img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExaTdqbmVrZHFlb241bmc1OHZpa2I1OTl2d3RkZnU0MzRyejc2cTNyciZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/1Td8UxgHzww3FJwVKB/giphy.gif" width="300"></td>
<td><img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNGZnNDZkOXVoYmUwcWtpMHVsMjQ2aWk0OHRpMGZ5anpuMDV2cGJ6aCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/OJ9eOskh6lgdalW21K/giphy.gif" width="300"></td>
  </tr>
 </table>

Project for measuring the temperature and carbon dioxide level indoor and outdoor.
This project can be used to improve indoor air quality. High level of carbon dioxide can cause headache, fatigue, and decreased productivity. The project consists of an Arduino module, BMP-280 temperature and humidity sensor, MH-Z19B carbon dioxide sensor, and NRF24L01 Wi-Fi module. The Arduino Uno collects data from sensors and transmits it to the Wi-Fi module. The Wi-Fi module then transmits the data to an Android device. The program on the Android device can display the data on the screen and notify the user if there is a need to open a window for ventilation.

<a href="https://play.google.com/store/apps/details?id=com.timhome.modularizationtest" rel="nofollow"><img src="https://camo.githubusercontent.com/bf5c3d9991f2bc80b5500c332c9b00244661511938bb78626a30f06664d495fb/68747470733a2f2f706c61792e676f6f676c652e636f6d2f696e746c2f656e5f75732f6261646765732f7374617469632f696d616765732f6261646765732f656e5f62616467655f7765625f67656e657269632e706e67" height="70" data-canonical-src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" style="max-width: 100%;"></a>

# Continuous integration & pull requests
- Template already has a few GitHub Actions workflows included. Please run commands in the IDE terminal, or setup a github hook. Commands are: `./gradlew ktlintFormat`, `./gradlew detektDebug`.

## 🌐 Socials:
[![LinkedIn](https://img.shields.io/badge/LinkedIn-%230077B5.svg?logo=linkedin&logoColor=white)](https://linkedin.com/in/https://www.linkedin.com/in/ruslan-timkov-165255189/) 

# 💻 Tech Stack:
![ANDROID](https://img.shields.io/badge/android-%2320232a.svg?style=for-the-badge&logo=android&logoColor=%a4c639)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)  ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)

- Multi-module project
  - Dynamic-features
- XML 
- Jetpack Compose
- Jetpack Library
  - Room
  - ViewModel
  - Datastore
  - Lifecycles
  - Navigation
- Firebase
  - Analytics
  - Crashlytics
  - Performance 
- GitHub Action(CI)
  - Lint
  - Detekt
- Dependabot (disabled)
- Retrofit2
- Leakcanary
- Build configuration
  - Groovy (used before)
  - Kotlin DSL

# 💻 Hardware equipmentk:
![Arduino](https://img.shields.io/badge/-Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white)
- Boards:
  - Arduino Uno
  - Arduino Nano
  - NodeMcu v3
- Module:
  - mhz19b(co2 sensor)
  - nrf24l01(wify)
  - bme280(temprature)
  - mq-7(carbon moxide)
