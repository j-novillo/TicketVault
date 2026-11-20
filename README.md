# TicketVault

> **Personal project · work in progress.** An Android app to digitize paper shopping receipts and
> analyze where the money paid in cash goes.

## The idea

Card payments show up in your banking app, but cash purchases only leave a paper receipt that
usually gets lost. **TicketVault** lets you take a photo of the receipt and automatically extracts
the store, date, items, prices and total, classifies them into categories (fresh food, processed
food, non-food…) and shows spending metrics.

The main design constraint is **privacy**: all processing (image cleanup, OCR, receipt parsing and
metric calculation) happens **100% on the device**. There is no backend or cloud OCR service, and
the data never leaves the phone.

## Current status

The project foundation is in place: architecture, dependency injection, the Room database with its
schema, and the receipt **capture and scanning** flow (camera + ML Kit Document Scanner to crop and
clean up the image). The next phases are the per-store parser, item classification and the
metrics screens.

The full design and backlog are documented in:

- **[PROJECT.md](PROJECT.md)**: architecture, stack, data model and end-to-end flow.
- **[USER_STORIES.md](USER_STORIES.md)**: epics and user stories with their technical tasks.

## Planned flow

```
Photo / gallery → Document Scanner (crop, perspective, shadows) → OCR (ML Kit Text Recognition v2)
   → Per-store template parser (Mercadona, Lidl, Carrefour…) → Keyword-based classification
   → Room → Metrics and charts
```

## Architecture and stack

- **Kotlin + Jetpack Compose**, MVVM with lightweight Clean Architecture
  (UI → ViewModel → UseCase → Repository → Room).
- **CameraX** and **ML Kit Document Scanner** for capture.
- **ML Kit Text Recognition v2** for on-device OCR.
- A custom rules engine with **per-store JSON templates** to interpret each receipt format.
- **Room** for local persistence, **Hilt** for dependency injection, **Coroutines + Flow**.

```
app/src/main/java/com/ticketvault
├── camera/          Capture and document scanner
├── ocr/             ML Kit Text Recognition wrapper
├── parsing/         Rules engine and per-store templates
├── classification/  Item categories and subcategories
├── metrics/         Aggregated metric calculation
├── data/            Room (entities, DAOs) and repositories
├── domain/          Models and use cases
├── ui/              Compose screens: capture, receipts, metrics, settings
└── di/              Hilt modules
```

## How to run it

Open the project in Android Studio and run the `app` module. The ML Kit models are downloaded once
through Google Play Services; after that the app works offline.

## Development

Solo project. I'm building it with the help of an AI assistant (Cursor): `PROJECT.md` and
`USER_STORIES.md` serve as the specification and context for working with it.
