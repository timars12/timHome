package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.ui.TransitionStyle
import com.timhome.core.ui.transitionStyle

/**
 * The style to animate with when the screen with this entry [metadata] is the one being shown.
 * It is the same for a push, a pop, a tab switch and predictive back.
 */
internal fun transitionStyleFor(metadata: Map<String, Any>): TransitionStyle =
    metadata.transitionStyle() ?: error("The entry being shown declares no transition style")
