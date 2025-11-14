# Exteregram Plugin Lab

Нативное Android-приложение (Jetpack Compose), которое помогает создавать Python-плагины для Exteregram и экспортировать их в формате `.plugin`.

## Возможности
- мастер заполнения метаданных плагина (название, package ID, версия, автор, описание);
- редактор Python-точки входа с подсказкой по структуре `handle(event, context)`;
- валидация черновика перед сборкой, чтобы package ID и код были корректны;
- генерация `.plugin` файла во внутреннюю директорию приложения;
- встроенный `FileProvider` и кнопка «Поделиться», чтобы сразу отправить готовый плагин;
- сохранение черновика в DataStore, чтобы не потерять прогресс между сессиями;
- карточка с последними экспортированными файлами.

## Сборка APK
1. Установите Android SDK 35 и JDK 17.
2. Выполните `./gradlew assembleDebug` (при первом запуске Gradle скачает Android Gradle Plugin из Google Maven).
3. Готовый APK появится в `app/build/outputs/apk/debug/app-debug.apk`.

> ⚠️ В офлайн-среде Gradle не сможет скачать Android Gradle Plugin 8.7.2. В таком случае запустите сборку с доступом в интернет или предварительно положите артефакты в локальный Maven.

## Подпись релиза
1. Создайте keystore: `keytool -genkeypair -v -keystore release.keystore -alias extere -keyalg RSA -keysize 4096 -validity 10000`.
2. Добавьте файл `release.keystore` и параметры в `app/build.gradle.kts` в блок `signingConfigs`.
3. Сборка: `./gradlew assembleRelease`.

## Структура `.plugin`
Файлы содержат YAML-манифест с метаданными (events, capabilities, tags) и Python-скриптом точки входа. Формат можно расширять в `PluginSerializer`.
