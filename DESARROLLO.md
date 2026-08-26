# TicketVault — Guía de desarrollo

Comandos esenciales y tutorial para correr la app en un móvil Android físico.

- **App ID:** `com.ticketvault`
- **Mínimo Android:** 8.0 (API 26)
- **Stack:** Kotlin · Jetpack Compose · Hilt · Room · CameraX · ML Kit
- **SO de desarrollo:** Windows (PowerShell)

Todos los comandos se ejecutan desde la raíz del proyecto:

```powershell
cd C:\Users\Usuario\Desktop\Jorge\TicketVault
```

En Windows usa `.\gradlew.bat`. En macOS/Linux usa `./gradlew`.

---

## 1. Comandos más importantes

### Compilar y empaquetar

| Comando | Qué hace |
|---|---|
| `.\gradlew.bat assembleDebug` | Genera el APK debug |
| `.\gradlew.bat assembleRelease` | Genera el APK release |
| `.\gradlew.bat bundleRelease` | Genera el AAB (Play Store) |
| `.\gradlew.bat :app:compileDebugKotlin` | Compila solo Kotlin (rápido para ver errores) |

APK debug resultante:

```text
app\build\outputs\apk\debug\app-debug.apk
```

### Instalar y lanzar en el dispositivo

| Comando | Qué hace |
|---|---|
| `.\gradlew.bat installDebug` | Instala el APK debug en el móvil/emulador conectado |
| `.\gradlew.bat uninstallDebug` | Desinstala la app debug |
| `adb shell am start -n com.ticketvault/.MainActivity` | Abre la app (si `MainActivity` es el launcher) |

### Tests

| Comando | Qué hace |
|---|---|
| `.\gradlew.bat test` | Tests unitarios |
| `.\gradlew.bat testDebugUnitTest` | Tests unitarios del variant debug |
| `.\gradlew.bat connectedDebugAndroidTest` | Tests instrumentados en dispositivo |
| `.\gradlew.bat :app:testDebugUnitTest --tests "*NombreTest*"` | Un test concreto |

### Limpieza y dependencias

| Comando | Qué hace |
|---|---|
| `.\gradlew.bat tasks` | **Equivalente a Gradle Sync** — resuelve dependencias y configura el proyecto (sin compilar) |
| `.\gradlew.bat projects` | Igual que arriba; lista módulos tras configurar |
| `.\gradlew.bat --refresh-dependencies tasks` | Sync forzando **re-descarga** de dependencias (como *Sync Project with Gradle Files* tras cambiar repos/versiones) |
| `.\gradlew.bat clean` | Borra `build/` |
| `.\gradlew.bat --refresh-dependencies` | Fuerza descarga de dependencias (usar junto con una tarea, p. ej. `tasks`) |
| `.\gradlew.bat dependencies` | Árbol de dependencias |
| `.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath` | Dependencias runtime debug |

> **Nota:** Gradle Sync en Android Studio también actualiza el modelo del IDE (`.idea`, classpath del editor). Desde terminal solo ejecutas la parte Gradle; para refrescar el IDE abre el proyecto en Android Studio o usa **File → Sync Project with Gradle Files** (`Ctrl+Shift+O` en Windows).

### Utilidades Gradle

| Comando | Qué hace |
|---|---|
| `.\gradlew.bat tasks` | Lista tareas disponibles |
| `.\gradlew.bat projects` | Módulos del proyecto |
| `.\gradlew.bat --stop` | Para daemons de Gradle |
| `.\gradlew.bat assembleDebug --info` | Build con más logs |
| `.\gradlew.bat assembleDebug --stacktrace` | Stacktrace completo si falla |

### ADB (dispositivo)

Asegúrate de que `adb` esté en el PATH (suele estar en  
`%LOCALAPPDATA%\Android\Sdk\platform-tools`).

| Comando | Qué hace |
|---|---|
| `adb devices` | Lista dispositivos conectados |
| `adb kill-server` / `adb start-server` | Reinicia ADB |
| `adb install -r app\build\outputs\apk\debug\app-debug.apk` | Instala/reinstala el APK |
| `adb uninstall com.ticketvault` | Desinstala por package |
| `adb logcat` | Logs en vivo |
| `adb logcat \| Select-String TicketVault` | Filtra logs (PowerShell) |
| `adb logcat -c` | Limpia buffer de logs |
| `adb shell pm clear com.ticketvault` | Borra datos de la app |
| `adb reverse tcp:8080 tcp:8080` | Reenvía un puerto (si usas servidor local) |

### Flujo diario típico

```powershell
# 1) Ver que el móvil está conectado
adb devices

# 2) Compilar + instalar
.\gradlew.bat installDebug

# 3) Ver logs mientras usas la app
adb logcat
```

O desde Android Studio: botón **Run** (▶) con el dispositivo seleccionado.

---

## 2. Tutorial: arrancar TicketVault en tu móvil

### Requisitos

1. **Android Studio** instalado (Ladybug / Meerkat o superior recomendado).
2. **SDK Android** configurado (en este repo ya apunta a tu SDK vía `local.properties`).
3. Móvil con **Android 8.0+** (API 26).
4. Cable USB (o Wi‑Fi debugging, ver más abajo).

> `local.properties` es local a tu máquina y no debe subirse a Git.

### Paso 1 — Activar opciones de desarrollador en el móvil

1. Abre **Ajustes → Acerca del teléfono**.
2. Toca **Número de compilación** 7 veces hasta que diga que eres desarrollador.
3. Vuelve a **Ajustes → Sistema → Opciones de desarrollador** (en algunos fabricantes: Ajustes → Opciones de desarrollador).
4. Activa:
   - **Depuración USB**
   - (Opcional) **Instalar vía USB** / **Depuración inalámbrica**

### Paso 2 — Conectar el móvil al PC

1. Conecta el móvil con el cable USB.
2. En el móvil, acepta el diálogo **¿Permitir depuración USB?** (marca “Siempre permitir desde este equipo” si quieres).
3. En PowerShell:

```powershell
adb devices
```

Debes ver algo como:

```text
List of devices attached
XXXXXXXX    device
```

Si aparece `unauthorized`, vuelve a aceptar el diálogo en el móvil o ejecuta:

```powershell
adb kill-server
adb start-server
adb devices
```

Si aparece `offline` o la lista vacía: prueba otro cable/puerto, cambia el modo USB a **Transferencia de archivos (MTP)** y reinicia ADB.

### Paso 3 — Abrir el proyecto

1. Abre Android Studio.
2. **File → Open** → carpeta `TicketVault`.
3. Espera a que Gradle sincronice (barra inferior).
4. Si pide JDK, usa **JDK 17** (el proyecto está configurado con Java 17).

### Paso 4A — Arrancar desde Android Studio (recomendado)

1. Arriba, en el selector de dispositivos, elige tu móvil.
2. Pulsa **Run** (▶) o `Shift + F10`.
3. Android Studio compila, instala y abre la app.

La primera vez puede tardar más (descarga de dependencias, KSP/Hilt/Room, etc.).

### Paso 4B — Arrancar solo con terminal

```powershell
cd C:\Users\Usuario\Desktop\Jorge\TicketVault
adb devices
.\gradlew.bat installDebug
```

Luego abre **TicketVault** desde el launcher del móvil, o:

```powershell
adb shell monkey -p com.ticketvault -c android.intent.category.LAUNCHER 1
```

### Paso 5 — Verificar que funciona

1. La app debería instalarse como **TicketVault** (`com.ticketvault`).
2. Concede permisos de **cámara** cuando los pida (CameraX / escáner de tickets).
3. Si falla al abrir, mira logs:

```powershell
adb logcat *:E
```

---

## 3. Depuración por Wi‑Fi (sin cable)

Con Android 11+:

1. En el móvil: **Opciones de desarrollador → Depuración inalámbrica** → activar.
2. Anota IP y puerto (ej. `192.168.1.40:41234`).
3. En el PC (misma Wi‑Fi):

```powershell
adb pair 192.168.1.40:XXXXX
# introduce el código de emparejamiento
adb connect 192.168.1.40:YYYYY
adb devices
```

Después puedes usar `.\gradlew.bat installDebug` igual que con cable.

---

## 4. Emulador (alternativa al móvil físico)

1. En Android Studio: **Device Manager → Create Device**.
2. Elige un Pixel (u otro) con **API 26+** (mejor API 34/35).
3. Arranca el AVD y pulsa **Run** (▶).

Para cámara/OCR real, el **móvil físico** suele ir mejor que el emulador.

---

## 5. Problemas frecuentes

| Problema | Qué probar |
|---|---|
| `SDK location not found` | Revisa `sdk.dir` en `local.properties` |
| Gradle sync falla | `.\gradlew.bat tasks` (sync CLI), `.\gradlew.bat --refresh-dependencies tasks`, **File → Invalidate Caches**, o `.\gradlew.bat --stop` y vuelve a sync |
| `adb devices` vacío | Cable datos (no solo carga), depuración USB, reiniciar ADB |
| App instalada pero crash al abrir | `adb logcat` y busca `FATAL EXCEPTION` / `com.ticketvault` |
| Build lento la primera vez | Normal (descargas + KSP). Siguientes builds son más rápidos |
| Error de Java/Kotlin | Confirma JDK **17** en Android Studio (Settings → Build → Gradle) |

---

## 6. Resumen express

```powershell
# Conectar móvil + depuración USB activada
adb devices

# Compilar e instalar
.\gradlew.bat installDebug

# Ver logs
adb logcat
```

O simplemente: **Android Studio → selecciona tu móvil → Run ▶**.
