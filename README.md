Project for measuring the temperature and carbon dioxide level indoor and outdoor.
This project can be used to improve indoor air quality. High level of carbon dioxide can cause headache, fatigue, and decreased productivity. The project consists of an Arduino module, BMP-280 temperature and humidity sensor, MH-Z19B carbon dioxide sensor, and NRF24L01 Wi-Fi module. The Arduino Uno collects data from sensors and transmits it to the Wi-Fi module. The Wi-Fi module then transmits the data to an Android device. The program on the Android device can display the data on the screen and notify the user if there is a need to open a window for ventilation.

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
