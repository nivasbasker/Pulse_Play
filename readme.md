# Pulse Play : Native Android Music Player

Pulse Play is an innovative Native Android Music Player App built using Kotlin, Jetpack Compose, and
the MediaPlayer API at its core.
<br/>Additional libs : Glide, Volley, Room
<br/> Latest Release APK
available [here](https://github.com/nivasbasker/Pulse_Play/tree/master/outputs)

### specs

1. Navigation accompanied with smooth transitions, horizontal swipe for tab switch
2. Music Player's gradient background based on the song's cover image
3. Swipe gestures for player expansion and minimization
4. Incorporated haptic feedback for tab changes and play/pause actions.
5. For-you tab displays the top 5 most recently played songs, cached in local db

### Composable

The app's user interface is entirely built on Jetpack Compose and Material3 UI, showcasing a
commitment to the latest and most efficient UI development practices. All composable functions,
responsible for rendering different elements of the app, can be explored in
the [UI folder](https://github.com/nivasbasker/Pulse_Play/tree/master/app/src/main/java/com/zio/pulseplay/ui).
The design of these composable functions is thoughtfully structured, promoting code re-usability and
maintainability. uses intuitive gesture controls and haptic feedback for ease of use.

### Architecture : Single Activity, Multiple Composable, ViewModel for live data

Pulse Play adopts a modern and efficient single-activity architecture, where the entire user
interface is composed of multiple composable functions. The seamless flow between different
composable functions is managed through a shared ViewModel class, ensuring efficient communication
and data sharing.

### MediaPlayer

The core functionality of Pulse Play revolves around the integration of the MediaPlayer API. This
enables the app to fetch songs from URLs and play them in the background seamlessly. The
implementation of the MediaPlayer wrapper class, available in
the [Helpers folder](https://github.com/nivasbasker/Pulse_Play/tree/master/app/src/main/java/com/zio/pulseplay/util),
encapsulates the complexities associated with media playback. The wrapper class includes functions
that allow the app to respond to events such as buffering, playing, stopping, and handling errors
gracefully.

### Local Database

Incorporated a local database to enhance user experience. Users can now view their favorite songs,
and the app intelligently stores this information locally. This feature enables users to revisit and
enjoy their preferred tracks conveniently.
