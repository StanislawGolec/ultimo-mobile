package pl.ultimo.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Root Application class for Ultimo-Mobile.
 * Annotated with @HiltAndroidApp to trigger Hilt code generation and dependency injection tree.
 */
@HiltAndroidApp
class UltimoApplication : Application()
