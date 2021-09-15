package org.yalab.beeftracker

import org.jsoup.Connection
import org.jsoup.helper.HttpConnection
import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.IOException

class NLBCTest {
    lateinit var nlbc: NLBC

    @Before
    fun setUp() {
        nlbc = NLBC()
    }

    @Test
    @Throws(IOException::class)
    fun fetchTest() {
        Mockito.mockStatic(Jsoup::class.java)
        mockJsoup(NLBC.AGREEMENT_URL, "agreement.html")
        mockJsoup(NLBC.ACTION_URL, "action.html")
        mockJsoup(NLBC.SEARCH_URL, "search.html")
        nlbc.fetch("1490915461")
        val cattle = nlbc.cattle
        assertEquals("1490915461", cattle.trackingNumber)
        assertEquals("2019.11.20", cattle.birthDay)
        assertEquals("去勢（雄）", cattle.gender)
        assertEquals("1474113579", cattle.motherTrackingNumber)
        assertEquals("ホルスタイン種", cattle.breed)
    }

    private fun mockJsoup(url: String, resourceName: String) {
        val fakeAction = Mockito.spy(HttpConnection())
        fakeAction.url("https://www.google.com/")
        val fakeActionResponse = Mockito.mock(Connection.Response::class.java)
        val text = File(javaClass.classLoader.getResource(resourceName).path).readText()
        Mockito.`when`(fakeActionResponse.body()).thenReturn(text)
        Mockito.`when`(fakeAction.execute()).thenReturn(fakeActionResponse)
        Mockito.`when`(Jsoup.connect(url)).thenReturn(fakeAction)
        Mockito.`when`(fakeAction.cookies(Mockito.anyMap())).thenReturn(fakeAction)
        Mockito.`when`(Jsoup.parse(Mockito.anyString())).thenCallRealMethod()
    }
}
