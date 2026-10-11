# TicketVault — Contexto del Proyecto (para IA de Cursor)

> Documento de referencia rápida. Léelo antes de modificar código. Evita re-analizar todo el repo: esta es la fuente de verdad sobre arquitectura y decisiones.

## 1. Qué es la app
App Android nativa, **100% cliente, sin backend/servidor**. El usuario fotografía o importa tickets de compra en papel, la app limpia la imagen, extrae el texto (OCR) y estructura la información (comercio, fecha, artículos, precios, total) para guardarla localmente y mostrar métricas de gasto con gráficos.

## 2. Restricciones clave (no negociables)
- Todo el procesamiento (imagen, OCR, parsing, cálculo de métricas) ocurre **en el dispositivo**. Nada de llamadas a servidores propios.
- Persistencia **local únicamente** (base de datos embebida).
- Debe funcionar sin conexión a internet tras la instalación (los modelos ML Kit se descargan una vez vía Google Play Services, luego funcionan offline).

## 3. Stack tecnológico elegido

| Capa | Tecnología | Motivo |
|---|---|---|
| Lenguaje / UI | Kotlin + Jetpack Compose | Nativo, moderno, coincide con el interés del usuario en Kotlin |
| Captura de imagen | CameraX (preview) + ML Kit Document Scanner | Preview en app; escaneo/crop vía GMS |
| Limpieza de ticket | Document Scanner en `SCANNER_MODE_BASE` (crop/perspectiva) + `TicketImageCleaner` propio | Sin filtros/retoque de Google; mejora automática (contraste, grayscale) en código tras el URI. Si falla la limpieza, error + reintento (sin edición manual). Pipeline más agresivo (sombras/threshold) queda como mejora futura si el OCR lo exige |
| OCR | ML Kit Text Recognition v2 (`TextRecognition`) | Extracción de texto on-device, sin coste, sin red |
| Parsing de ticket | Motor de reglas propio (Kotlin) + diccionario de plantillas por comercio (JSON local) | Cada cadena (Mercadona, Carrefour, Lidl, Dia, etc.) tiene formato de ticket distinto; reglas por regex/heurística |
| Clasificación de artículos | Diccionario local de palabras clave → categoría/subcategoría (JSON editable) | Sin necesidad de modelo ML complejo; ampliable por el usuario |
| Persistencia | Room (SQLite embebido) | Estándar Android, offline-first, consultas SQL para métricas |
| Inyección de dependencias | Hilt | Encaja con el interés del usuario en DI; facilita testing |
| Navegación / estado | Navigation Compose + ViewModel + StateFlow/Coroutines | Arquitectura MVVM estándar |
| Gráficos | Vico (com.patrykandpatrick.vico) o MPAndroidChart-Compose wrapper | Gráficos nativos Compose, sin dependencias de red |
| Export/backup opcional | Export a CSV/JSON a almacenamiento local (Storage Access Framework); backup opcional a Google Drive del propio usuario (no es "servidor propio") | Mantiene todo client-side |

**No se usa:** backend propio, API REST, servidor de OCR en la nube (Google Vision Cloud, AWS Textract, etc.), sincronización multi-dispositivo vía servidor.

## 4. Arquitectura de módulos

```
app/
 ├─ camera/        # Captura y Document Scanner
 ├─ ocr/           # Wrapper sobre ML Kit Text Recognition
 ├─ parsing/       # Motor de reglas, plantillas por comercio, extracción de campos
 ├─ classification/# Diccionario de categorías/subcategorías de artículos
 ├─ data/
 │   ├─ local/     # Room: entities, DAOs, database
 │   └─ repository/# Repositorios que exponen datos a la capa UI
 ├─ domain/        # Modelos de dominio, casos de uso (UseCases)
 ├─ metrics/        # Cálculo de métricas agregadas (queries + lógica)
 ├─ ui/
 │   ├─ capture/
 │   ├─ tickets/   # listado y detalle de tickets
 │   ├─ metrics/   # dashboards y gráficos
 │   └─ settings/  # gestión de comercios/categorías
 └─ di/            # módulos Hilt
```

## 5. Modelo de datos (Room) — entidades principales
- **Store** (id, nombre, alias/patrones de reconocimiento, plantilla de parsing)
- **Ticket** (id, storeId, fecha, hora, total, imagenUriOriginal, imagenUriLimpia, rawOcrText)
- **TicketItem** (id, ticketId, nombreProducto, cantidad, precioUnitario, precioTotal, category, subcategory)
- **Category** (id, nombre) — Supermercado / Comida / No comestible
- **Subcategory** (id, categoryId, nombre) — Frescos(Carne, Pescado, Fruta, Verdura), Procesados, etc.
- **ProductCategoryRule** (nombreNormalizado, category, subcategory) — correcciones de categoría aprendidas del usuario

## 6. Flujo funcional end-to-end
1. Usuario captura foto o importa de galería (ambos vía Document Scanner GMS, con `setGalleryImportAllowed(true)`).
2. GMS en `SCANNER_MODE_BASE` hace detección/crop/perspectiva (sin filtros de Google). La app valida la imagen, guarda el original y aplica `TicketImageCleaner` (contraste + grayscale) → `cleaned`. Si la limpieza o el guardado fallan, se muestra error y el usuario puede reintentar el escaneo.
3. ML Kit Text Recognition extrae texto en bruto.
4. Módulo `parsing` detecta el comercio (por cabecera/logo textual) y aplica la plantilla correspondiente (o un parser genérico si no hay plantilla).
5. Se extraen: fecha, comercio, líneas de artículo (nombre, cantidad, precio) y total. El usuario los revisa y corrige en la pantalla de revisión (`ui/review`) antes de guardarlos.
6. Módulo `classification` asigna categoría/subcategoría a cada artículo (por diccionario de palabras clave `assets/classification/keywords.json`). En la revisión el usuario puede reclasificarlo; la corrección se guarda en `product_category_rules` y prevalece sobre el diccionario en próximos tickets.
7. Se guarda todo en Room (`Ticket` + `TicketItem`s).
8. Las pantallas de métricas consultan Room (vía DAOs con Flow) y renderizan gráficos.

## 7. Convenciones de código
- MVVM + Clean Architecture ligera (UI → ViewModel → UseCase → Repository → Room).
- Un `UseCase` por acción relevante (ej. `ProcessTicketImageUseCase`, `GetMonthlyExpensesUseCase`).
- Nombrado en español para conceptos de dominio si el usuario lo prefiere; código (clases, funciones) en inglés estándar Kotlin.
- Tests unitarios para: parsing de tickets (por plantilla) y cálculo de métricas.

## 8. Fuera de alcance (por ahora)
- Sincronización entre dispositivos.
- Reconocimiento por foto de logotipos (se usa texto del ticket, no imagen del logo).
- Escaneo de tickets electrónicos (PDF/email) — posible fase futura.
