# Mukhtari Application - Root Cause Analysis Report

## 1. Root Cause الحقيقي
The application is missing a custom `Application` class, and `startKoin` is never called. This completely breaks Dependency Injection on startup. 

## 2. أول Exception في Crash Chain
The first exception thrown would be a Koin `NoBeanDefFoundException` or `IllegalStateException` (specifically, "KoinApplication has not been started") when `MainActivity` attempts to render the `MainNavGraph`, which in turn attempts to instantiate `DashboardScreen` containing `viewModel: DashboardViewModel = koinViewModel()`. Because Koin isn't started, `koinViewModel()` crashes immediately.

## 3. الملف المتسبب
Missing file: `MukhtariApplication.kt`.
Misconfigured file: `AndroidManifest.xml` (does not have an `android:name=".MukhtariApplication"` attribute).

## 4. رقم السطر إن أمكن
N/A (The failure happens at the very first invocation of `koinViewModel()` inside `DashboardScreen.kt`).

## 5. Startup path الذي يؤدي إلى الـCrash
APK Install -> App Launch -> `MainActivity.onCreate` -> `setContent` -> `MainNavGraph` -> `DashboardScreen` -> `koinViewModel()` -> CRASH (Koin is not initialized).

## 6. لماذا يحدث الـCrash
Because the `AppModule` containing all our DAOs, Repositories, and ViewModels is completely detached from the Android Lifecycle. The `startKoin` block is required to load the module into memory before any `@Composable` requests a `ViewModel`.

## 7. لماذا compilation وGitHub Actions لم يكشفاه
Dependency injection via Koin (`koinViewModel()`) is resolved at *runtime*, not *compile time*. The Kotlin compiler sees valid syntax and type-safe arguments, so it builds the APK successfully. GitHub Actions only compiled the code and ran logic-based Unit Tests (which manually injected dependencies or bypassed Koin). Since there were no Android Instrumentation tests running on a real emulator in CI, the runtime crash was hidden.

## 8. هل المشكلة مرتبطة بـRoom/DI/App Lock/Navigation/UI أو غيرها
Yes, it is fundamentally a **DI (Dependency Injection)** initialization issue. Since DI fails, Room cannot be built, Repositories cannot be instantiated, and Navigation cannot load the first UI state.

## 9. هل توجد أخطاء ثانوية ناتجة عن Root Cause
No, this is the absolute entry point bottleneck. Once Koin is properly initialized, the graph will construct `AppDatabase` and all ViewModels correctly.

## 10. خطة الإصلاح الدقيقة
1. Create `MukhtariApplication.kt` inheriting from `android.app.Application`.
2. Inside `onCreate()`, call `startKoin { androidContext(this@MukhtariApplication); modules(appModule) }`.
3. Update `AndroidManifest.xml` to point `<application android:name=".MukhtariApplication" ...>`.
