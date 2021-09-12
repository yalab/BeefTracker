package org.yalab.beeftracker

import org.jsoup.Connection
import org.jsoup.Jsoup

class NLBC {
    companion object {
        const val AGREEMENT_URL = "https://www.id.nlbc.go.jp/CattleSearch/search/agreement"
        const val ACTION_URL = "https://www.id.nlbc.go.jp/CattleSearch/search/agreement.action"
        const val SEARCH_URL = "https://www.id.nlbc.go.jp/CattleSearch/search/search.action"
        const val INPUT_NAME = "txtIDNO"
    }

    fun fetch(beefTrackingNumber: String): String {
        val response = Jsoup.connect(AGREEMENT_URL).method(Connection.Method.GET).followRedirects(false).execute()

        var connection = Jsoup.connect(ACTION_URL).cookies(response.cookies())
        val inputs = Jsoup.parse(response.body()).select("input")
        inputs.forEach({input ->
            connection.data(input.attr("name"), input.attr("value"))
        })
        val response2 = connection.method(Connection.Method.POST).followRedirects(false).execute()

        var connection2 = Jsoup.connect(SEARCH_URL).cookies(response.cookies())
        val inputs2 = Jsoup.parse(response2.body()).select("input")
        inputs2.forEach({input ->
            val name = input.attr("name")
            if(!INPUT_NAME.equals(name)) {
                connection2.data(name, input.attr("value"))
            }
        })
        connection2.data(INPUT_NAME, beefTrackingNumber).method(Connection.Method.POST)
        val response3 = connection2.execute()
        return response3.body()
    }
}
