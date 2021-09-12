package org.yalab.beeftracker

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NLBCTest {
    lateinit var nlbc: NLBC

    @Before
    fun setUp() {
        nlbc = NLBC()
    }

    @Test
    fun fetchTest() {
        val responseBody = nlbc.fetch("1490915461")
        assertEquals("\r\n\r\n<!DOCTYPE", responseBody.substring(0, 13))
    }
}
