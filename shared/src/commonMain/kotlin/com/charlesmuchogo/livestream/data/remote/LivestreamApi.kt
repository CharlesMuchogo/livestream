package com.charlesmuchogo.livestream.data.remote

import com.charlesmuchogo.livestream.data.remote.dto.HomeDto
import com.charlesmuchogo.livestream.data.remote.dto.LeagueDto
import com.charlesmuchogo.livestream.data.remote.dto.MatchDetailDto
import com.charlesmuchogo.livestream.data.remote.dto.MatchListDto
import com.charlesmuchogo.livestream.data.remote.dto.ScheduleDayDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

data class ApiConfig(val baseUrl: String)

fun createHttpClient(base: HttpClient, config: ApiConfig): HttpClient = base.config {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; explicitNulls = false })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 8_000
    }
    defaultRequest { url(config.baseUrl.trimEnd('/') + "/api/v1/") }
}

class LivestreamApi(private val client: HttpClient) {
    suspend fun home(sport: String, timeZone: String): HomeDto =
        client.get("home") {
            parameter("sport", sport)
            parameter("tz", timeZone)
        }.body()

    suspend fun matches(date: String?, timeZone: String, search: String?, leagueId: Long? = null): MatchListDto =
        client.get("matches") {
            parameter("tz", timeZone)
            date?.let { parameter("date", it) }
            search?.takeIf { it.isNotBlank() }?.let { parameter("q", it) }
            leagueId?.let { parameter("league", it) }
            parameter("limit", 300)
        }.body()

    suspend fun match(id: Long): MatchDetailDto = client.get("matches/$id").body()

    suspend fun scheduleDays(timeZone: String): List<ScheduleDayDto> =
        client.get("schedule/days") { parameter("tz", timeZone) }.body()

    suspend fun leagues(): List<LeagueDto> = client.get("leagues").body()
}
