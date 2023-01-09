# SvitloChecking Telegram Bot

Bot sending message on your ```channelId``` in format:
```
Світло є 🥳/Світла нема 😔 (Lights on 🥳/Lights off 😔)
Час включення/відключення: HH:mm dd/MM/yy (Time state changes HH:mm dd/MM/yy)
Світло було/Світла не було протягом: HH годин і mm хвилин. (During period of time HH:mm)
```
State changing depend on result of ping your provider IP address.

## Requirements 
* java *>9*
* Gradle *>6.9*
## Usage
1. Clone **repo**.
2. Change appropriate fields in **resources/application.yml.**
```
token: Your telegram-bot token.
channelId: Channelname to publish in format "@channelname".
ip: your provider IP address.
```
3. run task ```./gradlew run```

<img src="https://user-images.githubusercontent.com/114079662/211294636-831a9046-2bd9-4a59-887c-1d89b9b91852.png" width="500">

### Based on
https://github.com/kotlin-telegram-bot/kotlin-telegram-bot
