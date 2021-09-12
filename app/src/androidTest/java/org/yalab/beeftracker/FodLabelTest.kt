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
    lateinit var context : Context
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun foodLabelDetect() {
        val image = context.assets.open("image.png")
        val foodLabel = FoodLabel(context, image)
        assertEquals(listOf("|72ARF", "890 \\"), foodLabel.texts)
    }
}