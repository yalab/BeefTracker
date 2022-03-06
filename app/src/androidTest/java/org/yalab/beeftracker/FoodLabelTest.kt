package org.yalab.beeftracker

import android.content.Context
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.*
import org.yalab.beeftracker.databinding.CattleInfoBinding

@RunWith(AndroidJUnit4::class)
class FoodLabelTest {
    lateinit var context : Context
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    fun foodLabel(filename: String): FoodLabel {
        return FoodLabel(context, context.assets.open(filename))
    }

    fun foodLabel(): FoodLabel {
        return FoodLabel(context)
    }

    @Test
    fun packageName() {
        assertEquals("org.yalab.beeftracker", context.packageName)
    }

//    @Test
//    fun beefTrackingNumberPNG() {
//        assertEquals("", foodLabel("image.png").beefTrackingNumber())
//    }
//
//    @Test
//    fun beefTrackingNumberJPG() {
//        assertEquals("1490915461", foodLabel("image.jpg").beefTrackingNumber())
//    }
//
//    @Test
//    fun addBeefTrackingNumber() {
//        val beefTrackingNumbers: List<String> = listOf("1466716191", "1466716191")
//        val foodLabel = foodLabel()
//        val method = foodLabel::class.members.find{ it.name == "addBeefTrackingNumbers" }?.apply{ isAccessible = true }
//        method?.call(foodLabel, beefTrackingNumbers)
//        assertEquals(listOf("1466716191") as List<String>, foodLabel.beefTrackingNumbers)
//    }
//
//    @Test
//    fun emptyBeefTrackingNumber() {
//        assertEquals("", FoodLabel(context).beefTrackingNumber())
//    }
//
//    @Test
//    fun renderCattleInfo() {
//        val context: Context = mock(Context::class.java, "context")
//        val trackingNumber: TextView = mock(TextView::class.java, "textView")
//        val rootView: ConstraintLayout = mock(ConstraintLayout::class.java, "ConstraintLayout")
//        `when`(rootView.findViewById<TextView>(R.id.tracking_number)).thenReturn(trackingNumber)
//        val binding: CattleInfoBinding = CattleInfoBinding.bind(rootView)
//        FoodLabel.renderCattleInfo("", context, binding)
//        assertEquals("", trackingNumber.text)
//    }
}
