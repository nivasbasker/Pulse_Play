# Pulse Play : Native Android Music Player

Pulse Play is an innovative Native Android Music Player App meticulously crafted using Kotlin,
Jetpack Compose, and the powerful MediaPlayer API. The application stands out with its elegant
design and seamless user experience

### Composable

The app's user interface is entirely built on Jetpack Compose and Material3 UI, showcasing a
commitment to the latest and most efficient UI development practices. All composable functions,
responsible for rendering different elements of the app, can be explored in the UI folder. The
design of these composable functions is thoughtfully structured, promoting code re-usability and
maintainability. uses intuitive gesture controls for navigation and incorporates haptic feedback for
player controls.

### Architecture : Single Activity, Multiple Composable, ViewModel for live data

Pulse Play adopts a modern and efficient single-activity architecture, where the entire user
interface is composed of multiple composable functions. The seamless flow between different
composable functions is managed through a shared ViewModel class, ensuring efficient communication
and data sharing.

### MediaPlayer

The core functionality of Pulse Play revolves around the integration of the MediaPlayer API. This
enables the app to fetch songs from URLs and play them in the background seamlessly. The
implementation of the MediaPlayer wrapper class, available in the Helpers folder, encapsulates the
complexities associated with media playback. The wrapper class includes functions that allow the app
to respond to events such as buffering, playing, stopping, and handling errors gracefully.

### Local Database

Incorporated a local database to enhance user experience. Users can now view their favorite songs,
and the app intelligently stores this information locally. This feature enables users to revisit and
enjoy their preferred tracks conveniently.