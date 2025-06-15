# 📝 ToDo List App на Kotlin 
**Простое и удобное приложение для управления задачами, написанное на Kotlin для Android.**

## ✨ Возможности
- Добавление и удаление задач
- Отметка выполненных задач
- Сортировка
- Поддержка нескольких списков
- Сохранение в локальную БД DataStore

## 📌 Обновления
| Версия | Скачать                                                                                                    |
|--------|------------------------------------------------------------------------------------------------------------|
| 1.0    | [Скачать v1.0.0](https://github.com/Denis24-sdk/ToDo-List-in-Kotlin/releases/download/v1.0.0/app-debug.apk)  |
                

## 🛠 Технологии
- Kotlin — основной язык программирования приложения.
- Jetpack Compose — современный декларативный UI-фреймворк от Google для создания интерфейсов на Android
- AndroidX Activity (ComponentActivity) — базовый класс для активности с поддержкой Jetpack Compose
- Material3 (Material You) — библиотека компонентов Material Design 3 для Compose (material3)
- Compose Animation — API для анимаций в Jetpack Compose (animation, animation.core)
- Compose Foundation и LazyColumn — базовые компоненты интерфейса и списков в Compose
- Jetpack DataStore (Preferences DataStore) — современный способ хранения данных настроек и небольших структурированных данных (используется для хранения списков задач)
- Gson — библиотека для сериализации и десериализации объектов Kotlin в JSON и обратно
- Kotlin Coroutines — для асинхронного программирования и работы с потоками данных (lifecycleScope, Flow, launch)
- Flow (Kotlin Flow) — реактивный поток данных для наблюдения за изменениями в DataStore
- Material Icons — иконки из набора Material Design (Icons.Default.*)

## 📦 Установка

### Способ 1: Скачать APK из таблицы обновлений
*Требуется Android 5.0+*

### Способ 2: Сборка из исходников
1. **Клонируйте репозиторий**:
   ```
   bash
   git clone https://github.com/Denis24-sdk/ToDo-List-in-Kotlin.git
   cd ToDo-List-in-Kotlin

