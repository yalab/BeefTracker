package org.yalab.beeftracker

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class FoodLabelTest {
    lateinit var context : Context
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    fun foodLabel(filename: String) : FoodLabel {
        return FoodLabel(context, context.assets.open(filename))
    }

    @Test
    fun packageName() {
        assertEquals("org.yalab.beeftracker", context.packageName)
    }

//    @Test
//    fun foodLabelPNG() {
//        assertEquals(listOf("173456", "890 \\"), foodLabel("image.png").texts)
//    }
//
//    @Test
//    fun foodLabelJPG() {
//        assertEquals(listOf("|l ,_\nflﬁiﬁﬁﬂ'lﬁﬁ\n1490915461"), foodLabel("image.jpg").texts)
//    }
//
//    @Test
//    fun beefTrackingNumberPNG() {
//        assertEquals("", foodLabel("image.png").beefTrackingNumber())
//    }
//
    @Test
    fun beefTrackingNumberJPG() {
        assertEquals("1490915461", foodLabel("image.jpg").beefTrackingNumber())
    }
}
