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
    fun foodLabelPNG() {
        val foodLabel = FoodLabel(context, context.assets.open("image.png"))
        assertEquals(listOf("|72ARF", "890 \\"), foodLabel.texts)
    }

    @Test
    fun foodLabelJPG() {
        val foodLabel = FoodLabel(context, context.assets.open("image.jpg"))
        assertEquals(listOf(", 1 - ,\nflﬁiﬂz‘ﬂ'lﬁﬁ\n1490915461"), foodLabel.texts)
    }
}
