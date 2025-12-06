# Compass QS Tile

A minimalist, privacy-focused Android application that adds a dynamic Compass to your Quick Settings panel.

**Zero UI. Zero Bloat. Pure Functionality.**

This application is designed to be as lightweight as possible. It has no app drawer entry, no activity, and no heavy UI libraries (AppCompat/Material). It relies solely on the Android Framework.

## Features

-   **Dynamic Tile:** The tile icon rotates in real-time to point North.
-   **Live Label:** Displays the exact azimuth (degrees) and cardinal direction (N, NE, E, SE, S, SW, W, NW).
-   **Battery Efficient:** Sensors are only active when the Quick Settings panel is visible (`onStartListening`) and stop immediately when closed (`onStopListening`).
-   **Ultra Lightweight:** Built without `AppCompat`, `Material Design`, or `AndroidX Core`, resulting in a tiny APK size (aiming for < 100KB).
-   **No Clutter:** Does not appear in your App Drawer.

## Installation

### Building from Source
1.  Clone this repository.
2.  Open the project in **Android Studio**.
3.  Build the project (`Build > Make Project`).
4.  Run on a physical device (Emulators often lack accurate magnetic sensors).

### Usage
Since there is no app interface, you must add the tile manually after installation:

1.  Swipe down twice from the top of your screen to open the **Quick Settings** panel.
2.  Tap the **Edit** (pencil) icon.
3.  Scroll down to find the **"Compass"** tile.
4.  Drag and drop it into your active tiles area.
5.  The compass will now update whenever you open the panel.

## Technical Details

-   **Language:** Kotlin
-   **Min SDK:** 31 (Android 12)
-   **Target SDK:** 35 (Android 15)
-   **Architecture:** `TileService` based.
-   **Optimization:** R8 full mode enabled (Minify + Shrink Resources). All heavy dependencies removed.

## Permissions

-   **Sensors:** Required to access the Accelerometer and Magnetometer to calculate orientation.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](LICENSE)
