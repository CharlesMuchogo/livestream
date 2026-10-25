package com.charlesmuchogo.livestream

import com.charlesmuchogo.livestream.data.remote.ApiConfig
import com.charlesmuchogo.livestream.data.remote.LivestreamApi
import com.charlesmuchogo.livestream.data.remote.createHttpClient
import com.charlesmuchogo.livestream.data.repository.MatchRepositoryImpl
import com.charlesmuchogo.livestream.domain.models.MatchStatus
import com.charlesmuchogo.livestream.domain.models.StreamKind
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ApiParsingTest {
    private val requested = mutableListOf<String>()

    private fun repository(status: HttpStatusCode = HttpStatusCode.OK, body: String): MatchRepositoryImpl {
        val engine = MockEngine { request ->
            requested += request.url.toString()
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = createHttpClient(HttpClient(engine), ApiConfig("http://api.test/"))
        return MatchRepositoryImpl(LivestreamApi(client), timeZone = { TimeZone.of("Africa/Nairobi") })
    }

    @Test
    fun parsesHomeFeedFromTheApi() = runTest {
        val body = """
            {"sports":[{"id":"all","label":"All","live":42,"upcoming":76},{"id":"cricket","label":"Cricket","live":4,"upcoming":1}],
             "live":[{"id":78975,"slug":"pakistan-vs-england","sport":"cricket","sport_label":"Cricket","league_id":9,"league":"Cricket",
                      "home_team":"Pakistan","away_team":"England","home_logo":"https://x/Pakistan.webp","away_logo":"",
                      "starts_at":"2026-10-25T08:00:00Z","status":"live","stream_count":23,"extra":"ignored"}],
             "up_next":[{"id":77475,"home_team":"Internazionale","away_team":"Fiorentina","starts_at":null,"status":"scheduled"}],
             "updated_at":"2026-10-25T10:57:43Z"}
        """.trimIndent()

        val home = repository(body = body).home("cricket").getOrThrow()

        assertEquals("http://api.test/api/v1/home?sport=cricket&tz=Africa%2FNairobi", requested.single())
        assertEquals(42, home.sports.first().live)
        val live = home.live.single()
        assertEquals(MatchStatus.Live, live.status)
        assertEquals(Instant.parse("2026-10-25T08:00:00Z"), live.startsAt)
        assertEquals("Pakistan v England", live.title)
        assertEquals(23, live.streamCount)
        assertEquals(null, home.upNext.single().startsAt)
        assertEquals(Instant.parse("2026-10-25T10:57:43Z"), home.updatedAt)
    }

    @Test
    fun parsesStreamKinds() = runTest {
        val body = """
            {"match":{"id":1,"home_team":"A","away_team":"B","status":"live"},
             "streams":[{"id":5,"label":"Alpha","kind":"direct","url":"https://a/live.m3u8","verified":true},
                        {"id":6,"streamer":"Beta","kind":"page","url":"https://b/watch"}]}
        """.trimIndent()

        val detail = repository(body = body).match(1).getOrThrow()

        assertEquals(listOf(StreamKind.Direct, StreamKind.Page), detail.streams.map { it.kind })
        assertEquals("Beta", detail.streams[1].label)
    }

    @Test
    fun serverErrorsBecomeFailures() = runTest {
        val result = repository(HttpStatusCode.InternalServerError, """{"message":"Something went wrong"}""").home("all")
        assertTrue(result.isFailure)
    }
}
