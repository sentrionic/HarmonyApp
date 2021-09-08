# HarmonyApp

Harmony is an [Instagram](https://instagram.com/) like app that communicates with the [Django](https://github.com/sentrionic/Harmony-Django) backend.

This app is incomplete and not being worked on anymore.

## Architecture

The app is based on [codingwithmitch's course](https://codingwithmitch.com/courses/powerful-android-apps-with-jetpack-architecture/):

- MVI
- Dagger 2
- Jetpack
- Room
- Retrofit

### App

1. Clone the repository and open the `app` directory in Android Studio.
2. Set the `BASE_URL` in `Constants.kt`. If the backend runs locally, make sure to also add `android:usesCleartextTraffic="true"` to the Android Manifest.
3. Run the app.
