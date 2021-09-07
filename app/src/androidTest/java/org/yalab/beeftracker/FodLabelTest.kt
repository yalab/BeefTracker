package org.yalab.beeftracker

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class FodLabelTest {
    lateinit var foodLabel : FoodLabel
    lateinit var context : Context
    @Before
    fun setUp() {
        foodLabel = FoodLabel()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun foodLabelDetect() {
        val image = context.assets.open("image.png")
        foodLabel.detect(context, image)
    }
}