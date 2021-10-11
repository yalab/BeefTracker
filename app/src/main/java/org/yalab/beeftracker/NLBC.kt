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

    data class Cattle(val trackingNumber: String,
                      val birthDay: String,
                      val gender: String,
                      val motherTrackingNumber: String,
                      val breed: String)
    var cattle: Cattle = Cattle("", "", "", "", "")
    fun fetch(beefTrackingNumber: String) {
        val c = Jsoup.connect(AGREEMENT_URL)
        val response = c.method(Connection.Method.GET).followRedirects(false).execute()

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
        parse(response3.body(), beefTrackingNumber)
    }

    fun parse(html: String, beefTrackingNumber: String) {
        val jsoup = Jsoup.parse(html)
        val table = jsoup.select("body > div > table:nth-child(1) > tbody > tr:nth-child(3) > td:nth-child(2) > table > tbody > tr:nth-child(2) > td:nth-child(2) > table > tbody > tr:nth-child(3) > td:nth-child(2) > table > tbody > tr > td > span > table:nth-child(4)")
        if(table.size > 0) {
            val tr = table.select("tr:nth-child(2)")
            val c = ArrayList<String>(5)
            tr.select("td").forEach({ td -> c.add(td.text()) })
            this.cattle = Cattle(c[0], c[1], c[2], c[3], c[4])
        } else {
            this.cattle = Cattle(beefTrackingNumber, "該当する牛の情報は", "ありません。", "", "")
        }
    }
}
