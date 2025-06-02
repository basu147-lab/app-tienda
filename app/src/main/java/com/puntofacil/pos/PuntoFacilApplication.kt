package com.puntofacil.pos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Clase principal de la aplicación PuntoFácil
 * Maneja la inicialización de componentes globales y configuraciones
 */
@HiltAndroidApp
class PuntoFacilApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber para logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("PuntoFácil Application iniciada")
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("PuntoFácil Application terminada")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.w("Memoria baja detectada")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Timber.w("Trim memory nivel: $level")
    }
}