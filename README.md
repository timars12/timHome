# Continuous integration & pull requests
- Template already has a few GitHub Actions workflows included. Please ensure you're passing the checks locally, before opening pull request. To do that, either run commands in the IDE terminal, or setup a github hook. Commands are: `./gradlew ktlintFormat`, `./gradlew detektDebug`. **Request a review only after the CI checks have passed successfully.** <br />
- If pull request contains code that should close the issue, please write: close #1, close #2(number == issue number) somewhere in the PR description. This allows for automatic issue closing upon successfull PR merge.
- Commit code as many times as you want while working on a feature. When the feature is ready - do a careful rebase over origin/master and squash all this stuff into one or two meaningful commits that clearly represent the feature, before opening a pull request.
- Features should be splitted into logical chunks if they require a lot of code changes.
- Attempt to keep PR size in range of 250 - 300 lines of code changed.


## 🌐 Socials:
[![LinkedIn](https://img.shields.io/badge/LinkedIn-%230077B5.svg?logo=linkedin&logoColor=white)](https://linkedin.com/in/https://www.linkedin.com/in/ruslan-timkov-165255189/) 

# 💻 Tech Stack:
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![ANDROID](https://img.shields.io/badge/android-%2320232a.svg?style=for-the-badge&logo=android&logoColor=%a4c639) ![Room](https://img.shields.io/badge/sqlite-%2307405e.svg?style=for-the-badge&logo=sqlite&logoColor=white) ![Arduino](https://img.shields.io/badge/-Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white) ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white) 
