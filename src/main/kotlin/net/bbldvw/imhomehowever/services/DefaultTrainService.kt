package net.bbldvw.imhomehowever.services

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DefaultTrainService : TrainService {
    private val log = LoggerFactory.getLogger(DefaultTrainService::class.java)
    // Yahoo路線情報のURL
    private val baseUrl = "https://transit.yahoo.co.jp/search/result"

    override fun searchRoute(
        from: String,
        to: String,
        via: String?,
    ): String {
        try {
            // 1. URLパラメータの作成
            val encFrom = URLEncoder.encode(from, StandardCharsets.UTF_8)
            val encTo = URLEncoder.encode(to, StandardCharsets.UTF_8)
            val encVia = URLEncoder.encode(via ?: "", StandardCharsets.UTF_8)

            // 最短経路、現時刻で検索
            val url = "$baseUrl?from=$encFrom&to=$encTo&type=1&via=$encVia"

            // 2. スクレイピング実行
            val doc = Jsoup.connect(url).timeout(10000).get() // 10秒で諦める設定

            // 3. HTML解析
            // id="rsltlst" の中にある li 要素が各経路
            val firstRoute =
                doc.select("#rsltlst > li").first()
                    ?: return "経路が見つかりませんでした😢 ($url)"

            // 時間の取得 (class="time")
            // 例: 17:30→18:00
            val rawTimeText = firstRoute.select(".time").text()
            val timeElement = formatTimeText(rawTimeText)

            // 乗り換え回数など
            val summary = firstRoute.select(".summary").text()

            return "🚃 **経路検索結果**\n" +
                "区間: $from → $to\n" +
                "時間: $timeElement\n" +
                "概要: $summary\n" +
                "詳細: $url"
        } catch (e: Exception) {
            log.error("Route search failed: {}", e.message, e)
            return "検索中にエラーが発生しました: ${e.message}"
        }
    }

    private fun formatTimeText(text: String): String {
        val s = text.trim()

        // 例: "00:09→05:195時間10分" を
        //     "00:09 → 05:19 5時間10分" にしたい
        val m = Regex("""(\d{1,2}:\d{2})\s*→\s*(\d{1,2}:\d{2})(.*)""").matchEntire(s)
        if (m != null) {
            val start = m.groupValues[1]
            val end = m.groupValues[2]
            val rest = m.groupValues[3].trim()
            return if (rest.isNotEmpty()) "$start → $end : $rest" else "$start → $end"
        }

        // フォールバック：とりあえず矢印の前後だけは空ける
        return s.replace(Regex("""\s*→\s*"""), " → ")
    }
}
