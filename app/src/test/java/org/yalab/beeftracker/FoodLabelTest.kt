package org.yalab.beeftracker

import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class FoodLabelTest {
    lateinit var foodLabel : FoodLabel
    @Before
    fun setUp() {
        foodLabel = FoodLabel()
    }

    @Test
    fun detectTest() {
        assertEquals("1234", foodLabel.detect())
    }
}
