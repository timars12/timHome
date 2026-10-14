package com.timhome.core.di

import com.timhome.core.common.navigation.ChannelNavigator
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    // There is deliberately no Navigator binding: screen logic gets a navigator scoped to its
    // route from ViewModelFactory, so every command carries its origin.
    @Singleton
    @Provides
    fun provideChannelNavigator(): ChannelNavigator = ChannelNavigator()

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics
}
