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
        val responseBody = nlbc.fetch("1490915461")
        println(responseBody)
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
//    @Test
//    fun parseTest() {
//        val path = javaClass.classsLoader.getResource("index.html").path
//        val html = File(path).readText()
//        nlbc.parse(html)
//    }
}
