# TicketVault — Historias de Usuario y Tareas

> Backlog inicial. Cada historia sigue el formato "Como [usuario], quiero [acción], para [beneficio]". Las tareas son técnicas y granulares, pensadas para implementarse con ayuda de Cursor.

---

## Épica 1: Captura de tickets

### HU-1.1 — Fotografiar un ticket
Como usuario, quiero hacer una foto directamente al ticket desde la app, para digitalizarlo sin salir de la aplicación.

- [ ] Configurar CameraX en un composable de captura
- [ ] Integrar ML Kit Document Scanner API (`GmsDocumentScannerOptions`, modo `SCANNER_MODE_FULL`)
- [ ] Gestionar permisos de cámara (o delegarlos al flujo del Document Scanner, que no los requiere)
- [ ] Guardar la imagen resultante (URI JPEG) en almacenamiento interno de la app
- [ ] Manejar errores (cancelación, fallo de escaneo, mala luz)

### HU-1.2 — Importar foto desde galería
Como usuario, quiero importar una foto de un ticket desde mi galería, para digitalizar tickets que ya tenía fotografiados.

- [ ] Habilitar `setGalleryImportAllowed(true)` en el Document Scanner
- [ ] Alternativa: selector de imagen nativo (`ActivityResultContracts.PickVisualMedia`) si el usuario no quiere pasar por el flujo de escaneo
- [ ] Validar formato/tamaño de imagen importada
- [ ] Reutilizar el pipeline de limpieza (paso 2 de HU-1.1) sobre la imagen importada

### HU-1.3 — Limpieza y mejora automática de la imagen
Como usuario, quiero que la app mejore la calidad del ticket capturado, para que el OCR sea más preciso.

- [ ] Aplicar recorte automático de bordes (incluido en Document Scanner)
- [ ] Aplicar corrección de perspectiva y eliminación de sombras/manchas
- [ ] Permitir recorte/ajuste manual como fallback si el automático falla
- [ ] Guardar tanto la imagen original como la limpia (para trazabilidad)

---

## Épica 2: Extracción y estructuración de datos

### HU-2.1 — Extraer texto del ticket (OCR)
Como usuario, quiero que la app lea el texto del ticket automáticamente, para no tener que introducir los datos a mano.

- [ ] Integrar ML Kit Text Recognition v2 sobre la imagen limpia
- [ ] Guardar el texto en bruto (`rawOcrText`) asociado al ticket
- [ ] Mostrar pantalla de "procesando" mientras corre el OCR
- [ ] Manejar tickets con texto ilegible (marcar como "requiere revisión manual")

### HU-2.2 — Detectar el comercio del ticket
Como usuario, quiero que la app identifique automáticamente el supermercado o tienda, para no tener que seleccionarlo manualmente cada vez.

- [ ] Crear diccionario/plantillas de comercios frecuentes (Mercadona, Carrefour, Lidl, Dia, Alcampo, etc.) con patrones de cabecera
- [ ] Implementar matching por texto (regex/contains sobre primeras líneas del OCR)
- [ ] Fallback: permitir que el usuario seleccione o cree manualmente el comercio
- [ ] Persistir la plantilla aprendida para futuras detecciones (mejora incremental)

### HU-2.3 — Extraer líneas de artículos, cantidades y precios
Como usuario, quiero que la app extraiga cada artículo, su cantidad y su precio del ticket, para tener el detalle completo del gasto.

- [ ] Diseñar motor de parsing genérico (líneas con patrón `nombre ... cantidad x precio ... total`)
- [ ] Implementar parsers específicos por plantilla de comercio (mayor precisión)
- [ ] Extraer fecha, hora y total del ticket
- [ ] Pantalla de revisión/edición manual de los datos extraídos antes de guardar
- [ ] Guardar `Ticket` y lista de `TicketItem` en Room

### HU-2.4 — Clasificar artículos por categoría
Como usuario, quiero que cada artículo se clasifique automáticamente en comida/no comestible y en subcategoría (carne, pescado, fruta, verdura, procesados...), para poder analizar mis gastos por tipo de producto.

- [ ] Crear diccionario local de palabras clave → categoría/subcategoría (JSON editable)
- [ ] Implementar función de clasificación (match de palabras clave sobre el nombre del artículo)
- [ ] Permitir al usuario corregir/reclasificar un artículo manualmente
- [ ] Persistir las correcciones para mejorar futuras clasificaciones (aprendizaje simple por diccionario de usuario)

---

## Épica 3: Almacenamiento e histórico

### HU-3.1 — Histórico de tickets escaneados
Como usuario, quiero tener un histórico de todos los tickets que he escaneado, para poder consultarlos en cualquier momento.

- [ ] Definir esquema Room (`Ticket`, `TicketItem`, `Store`, `Category`, `Subcategory`)
- [ ] Implementar DAOs con consultas por fecha, comercio y categoría
- [ ] Pantalla de listado de tickets (orden cronológico, con miniatura de imagen)
- [ ] Pantalla de detalle de ticket (imagen, comercio, fecha, artículos, total)
- [ ] Permitir eliminar o editar un ticket almacenado

### HU-3.2 — Gestión de comercios y categorías
Como usuario, quiero poder gestionar la lista de comercios y categorías de producto, para adaptar la app a mis hábitos de compra.

- [ ] Pantalla de ajustes: CRUD de comercios
- [ ] Pantalla de ajustes: CRUD de categorías/subcategorías y sus palabras clave
- [ ] Validación de duplicados

---

## Épica 4: Métricas y visualización

### HU-4.1 — Total de gastos en supermercados
Como usuario, quiero ver el total gastado en supermercados filtrado por mes, año y comercio, con gráficas comparativas, para controlar mi presupuesto de compra.

- [ ] Query Room: suma de tickets por mes/año/comercio
- [ ] Selectores de mes (actual por defecto) y año (actual por defecto)
- [ ] Selector de comercio (todos por defecto)
- [ ] Gráfico comparativo por supermercado (barras)
- [ ] Gráfico comparativo por meses (línea o barras)

### HU-4.2 — Total de gastos en comida
Como usuario, quiero ver el total gastado específicamente en comida, con los mismos filtros y gráficas que en supermercados, para diferenciar alimentación de otros gastos.

- [ ] Query Room filtrando `TicketItem` por categoría = Comida
- [ ] Reutilizar componentes de filtro y gráfico de HU-4.1
- [ ] Gráfico comparativo por comercio y por mes

### HU-4.3 — Total de gastos en productos no comestibles
Como usuario, quiero ver el total gastado en productos no comestibles, para identificar gasto en limpieza, higiene, etc.

- [ ] Query Room filtrando por categoría = No comestible
- [ ] Reutilizar componentes de filtro y gráfico
- [ ] Gráfico comparativo por comercio y por mes

### HU-4.4 — Total de gastos por tipo de producto
Como usuario, quiero ver el gasto desglosado por tipo de producto (carne, pescado, fruta, verdura, procesados...), para entender en qué se me va el dinero dentro de la alimentación.

- [ ] Selector de subcategoría de producto
- [ ] Query Room de agregación por subcategoría
- [ ] Gráfico de tarta o barras por subcategoría
- [ ] Evolución temporal de una subcategoría concreta

### HU-4.5 — Listado de gastos por día/mes
Como usuario, quiero ver un listado detallado de gastos filtrado por día o mes, para revisar tickets concretos rápidamente.

- [ ] Selector de día y selector de mes
- [ ] Lista filtrable de tickets/artículos con navegación al detalle
- [ ] Totales parciales en la cabecera del listado

---

## Épica 5: Métricas adicionales sugeridas

### HU-5.1 — Comparativa de precio del mismo producto entre comercios
Como usuario, quiero comparar el precio de un mismo artículo en distintos supermercados, para saber dónde comprarlo más barato.

- [ ] Normalizar nombres de artículos (para detectar el "mismo producto" entre tickets)
- [ ] Query de precios históricos de un artículo agrupados por comercio
- [ ] Gráfico de evolución de precio por comercio

### HU-5.2 — Ticket medio y frecuencia de compra
Como usuario, quiero ver mi ticket medio y cada cuánto compro, para entender mis patrones de consumo.

- [ ] Cálculo de importe medio por ticket (global, por comercio, por mes)
- [ ] Cálculo de frecuencia (días entre tickets del mismo comercio)
- [ ] Widget resumen en pantalla principal

### HU-5.3 — Detección de inflación personal
Como usuario, quiero ver cómo ha evolucionado el precio de mis productos habituales a lo largo del tiempo, para detectar subidas de precio.

- [ ] Query de variación de precio de un artículo entre periodos
- [ ] Indicador de % de variación respecto al mes/año anterior
- [ ] Gráfico de tendencia de precio

### HU-5.4 — Presupuesto mensual y alertas
Como usuario, quiero fijar un presupuesto mensual por categoría, para recibir un aviso cuando esté cerca de superarlo.

- [ ] Pantalla de configuración de presupuestos por categoría
- [ ] Cálculo de gasto acumulado del mes vs presupuesto
- [ ] Notificación local cuando se supere un umbral (ej. 80%, 100%)

### HU-5.5 — Exportación de datos
Como usuario, quiero exportar mis tickets y métricas a CSV/JSON, para tener una copia de seguridad o analizarlos externamente (ej. en Python).

- [ ] Exportación de tickets y artículos a CSV usando Storage Access Framework
- [ ] Exportación de tickets y artículos a JSON
- [ ] Opción de compartir el archivo exportado

### HU-5.6 — Top artículos más comprados
Como usuario, quiero ver un ranking de los artículos que compro con más frecuencia y en qué monto total, para identificar mis productos "fijos".

- [ ] Query de agregación por nombre de artículo normalizado (frecuencia y total gastado)
- [ ] Pantalla de ranking (top 10/20)
- [ ] Filtro por periodo de tiempo

### HU-5.7 — Comparativa año contra año
Como usuario, quiero comparar mi gasto total o por categoría de un mes/periodo contra el mismo periodo del año anterior, para ver si mi gasto sube o baja.

- [ ] Query de gasto agregado por periodo y año
- [ ] Gráfico comparativo año actual vs año anterior
- [ ] Indicador de variación porcentual

---

## Épica 6: Revisión y corrección manual

### HU-6.1 — Revisión de ticket tras el escaneo
Como usuario, quiero revisar y corregir los datos extraídos antes de guardarlos definitivamente, para asegurar que las métricas sean precisas.

- [ ] Pantalla de edición post-OCR (comercio, fecha, artículos, precios, total)
- [ ] Validación de que la suma de artículos coincide con el total (alerta si no coincide)
- [ ] Opción de descartar el ticket si el escaneo falló completamente
