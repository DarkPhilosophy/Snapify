package ro.snapify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import ro.snapify.ui.RecomposeReason
import javax.inject.Singleton

// DI module intentionally removed from :core to keep Hilt configuration in the app module.

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecomposeFlow(): MutableSharedFlow<RecomposeReason> = MutableSharedFlow(replay = 1)
}
