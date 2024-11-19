package kiwi.hoonkun.ckremote.core.feeds

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kiwi.hoonkun.ckremote.core.networking.DefaultHttpClientEngine
import kiwi.hoonkun.ckremote.utils.DefaultJson
import kiwi.hoonkun.ckremote.utils.UserSpaceStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString


object Feeds {

    private val Json = DefaultJson

    private val client = HttpClient(DefaultHttpClientEngine)  {
        install(ContentNegotiation) {
            json(Json)
        }
    }

    val list = mutableStateListOf<Feed>()
    val imageBitmaps = mutableStateMapOf<String, ImageBitmap>()

    private val imageUrls = mutableStateMapOf<String, String>()

    suspend fun fetchList(count: Int): List<Feed> {
        val fetchCount = if (list.size < 16) 16 else count
        val response = client.get("https://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=1621690&count=$fetchCount&maxlength=0&format=json")
        val body = response.body<FeedsApiResponse>()

        val newList = list.merge(body.appnews.newsitems)

        list.clear()
        list.addAll(newList)

        return body.appnews.newsitems
    }

    suspend fun fetchImage(gid: String, entryUrl: String): ImageBitmap? {
        imageBitmaps[gid]?.let { return it }

        val url = imageUrls[gid] ?: run {
            var nextUrl = entryUrl
            var response: HttpResponse?
            while (true) {
                response = client.get(nextUrl)

                if (response.status != HttpStatusCode.Found) {
                    break
                } else {
                    nextUrl = response.headers["Location"] ?: break
                }
            }
            if (response == null) return null

            val html = response.bodyAsText()
            val found = Regex("<link rel=\"image_src\" href=\"(.+?)\">").find(html) ?: return null

            found.groups[1]?.value ?: return null
        }

        if (url.contains("store_item_assets")) return null

        imageUrls[gid] = url

        return loadImage(url).also { imageBitmaps[gid] = it }
    }

    fun restoreList(store: UserSpaceStore) {
        val stored = store["feeds_list"] ?: return

        val newList = list.merge(Json.decodeFromString(stored))

        list.clear()
        list.addAll(newList)
    }

    fun restoreImages(store: UserSpaceStore) {
        val stored = store["feeds_images"] ?: return

        imageUrls.putAll(Json.decodeFromString(stored))
    }

    fun cacheList(store: UserSpaceStore) {
        store["feeds_list"] = Json.encodeToString(list.toList())
    }

    fun cacheImages(store: UserSpaceStore) {
        store["feeds_images"] = Json.encodeToString(imageUrls.toMap())
    }

    suspend fun loadImage(url: String): ImageBitmap =
        decodeImageBytes(client.get(url).bodyAsBytes())

    private fun List<Feed>.merge(other: List<Feed>) = toMutableList()
        .apply { addAll(other) }
        .distinctBy { it.gid }
        .filter { it.url.contains("steam_community_announcements") }
        .toMutableList()
        .apply { sortByDescending { it.date } }

}

@Serializable
data class Feed(
    val gid: String,
    val title: String,
    val url: String,
    val author: String,
    val contents: String,
    val date: Long,
    val tags: List<String> = emptyList()
)

@Serializable
data class FeedsApiResponse(
    val appnews: FeedsApiResponseAppNews
)

@Serializable
data class FeedsApiResponseAppNews(
    val appid: Int,
    val newsitems: List<Feed>
)

expect fun decodeImageBytes(bytes: ByteArray): ImageBitmap