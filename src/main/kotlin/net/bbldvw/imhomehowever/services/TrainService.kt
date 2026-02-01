package net.bbldvw.imhomehowever.services

/**
 * Yahoo電車乗り換えをスクレイピングして乗換情報を取得するサービス
 */
interface TrainService {
    /**
     * 指定した区間の最短経路を検索し、結果を文字列で返す
     *
     * @param from 駅名 (発)
     * @param to 駅名 (到)
     * @return 乗換情報の文字列
     */
    fun searchRoute(
        from: String,
        to: String,
        via: String? = null,
    ): String
}
