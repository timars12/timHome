package com.example.core.ui

sealed class Destination(val destination: String) {
    object SampleContainer : Destination("signIn")
    object SampleContainer1 : Destination("signIn122222")
}