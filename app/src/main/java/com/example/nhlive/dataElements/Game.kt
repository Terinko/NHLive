package com.example.nhlive.dataElements

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


data class GameDetailsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("gameState") val gameState: String,
    @SerializedName("displayPeriod") val displayPeriod: Int,
    @SerializedName("clock") val clock: GameClock
)

data class GameClock(
    @SerializedName("timeRemaining") val timeRemaining: String,
    @SerializedName("secondsRemaining") val secondsRemaining: Int,
    @SerializedName("running") val running: Boolean,
    @SerializedName("inIntermission") val inIntermission: Boolean
)

data class TeamStatsResponse(
    @SerializedName("data") val data: List<TeamStats> = emptyList()
)

data class TeamStats(
    @SerializedName("teamId") val teamId: Int,
    @SerializedName("teamFullName") val teamFullName: String,
    @SerializedName("gamesPlayed") val gamesPlayed: Int,
    @SerializedName("wins") val wins: Int,
    @SerializedName("losses") val losses: Int,
    @SerializedName("otLosses") val otLosses: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("goalsFor") val goalsFor: Int,
    @SerializedName("goalsAgainst") val goalsAgainst: Int,
    @SerializedName("goalsForPerGame") val goalsForPerGame: Double,
    @SerializedName("goalsAgainstPerGame") val goalsAgainstPerGame: Double,
    @SerializedName("shotsForPerGame") val shotsForPerGame: Double,
    @SerializedName("shotsAgainstPerGame") val shotsAgainstPerGame: Double
)

// Schedule response
data class ScheduleResponse(
    @SerializedName("previousStartDate") val previousStartDate: String = "",
    @SerializedName("gameWeek") val gameWeek: List<GameDay> = emptyList()
)

data class GameDay(
    @SerializedName("date") val date: String = "",
    @SerializedName("dayAbbrev") val dayAbbrev: String = "",
    @SerializedName("numberOfGames") val numberOfGames: Int = 0,
    @SerializedName("datePromo") val datePromo: List<Any> = emptyList(),
    @SerializedName("games") val games: List<Game> = emptyList()
)

// Game representation matching the API response structure
data class Game(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("season") val season: Int = 0,
    @SerializedName("gameType") val gameType: Int = 0,
    @SerializedName("neutralSite") val neutralSite: Boolean = false,
    @SerializedName("startTimeUTC") val startTimeUTC: String = "",
    @SerializedName("easternUTCOffset") val easternUTCOffset: String = "",
    @SerializedName("venueUTCOffset") val venueUTCOffset: String = "",
    @SerializedName("venueTimezone") val venueTimezone: String = "",
    @SerializedName("gameState") val gameState: String = "",
    @SerializedName("gameScheduleState") val gameScheduleState: String = "",
    @SerializedName("tvBroadcasts") val tvBroadcasts: List<TvBroadcast> = emptyList(),
    @SerializedName("awayTeam") val awayTeam: TeamInfo = TeamInfo(),
    @SerializedName("homeTeam") val homeTeam: TeamInfo = TeamInfo(),
    @SerializedName("periodDescriptor") val periodDescriptor: PeriodDescriptor? = null,
    @SerializedName("ticketsLink") val ticketsLink: String = "",
    @SerializedName("gameCenterLink") val gameCenterLink: String = ""
) {
    // Helper property to format game time nicely
    val formattedDateTime: String
        get() {
            try {
                val utcTime = ZonedDateTime.parse(startTimeUTC)
                val easternTime = utcTime.withZoneSameInstant(ZoneId.of("America/New_York"))

                val today = LocalDate.now(ZoneId.of("America/New_York"))
                val gameDate = easternTime.toLocalDate()

                // Format just the time if it's today
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
                val formattedTime = easternTime.format(timeFormatter)

                return if (gameDate.equals(today)) {
                    "Today at $formattedTime"
                } else if (gameDate.equals(today.plusDays(1))) {
                    "Tomorrow at $formattedTime"
                } else {
                    // Format with date for other days
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
                    val formattedDate = easternTime.format(dateFormatter)
                    "$formattedDate at $formattedTime"
                }
            } catch (e: Exception) {
                return startTimeUTC.substringAfter("T").substringBefore("Z")
            }
        }
}

data class TvBroadcast(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("market") val market: String = "",
    @SerializedName("countryCode") val countryCode: String = "",
    @SerializedName("network") val network: String = "",
    @SerializedName("sequenceNumber") val sequenceNumber: Int = 0
)

data class TeamInfo(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("commonName") val commonName: CommonName = CommonName(),
    @SerializedName("placeName") val placeName: PlaceName = PlaceName(),
    @SerializedName("placeNameWithPreposition") val placeNameWithPreposition: PlaceNameWithPreposition? = null,
    @SerializedName("abbrev") val abbrev: String = "",
    @SerializedName("logo") val logo: String = "",
    @SerializedName("darkLogo") val darkLogo: String = "",
    @SerializedName("awaySplitSquad") val awaySplitSquad: Boolean? = null,
    @SerializedName("homeSplitSquad") val homeSplitSquad: Boolean? = null,
    @SerializedName("radioLink") val radioLink: String? = null,
    @SerializedName("odds") val odds: List<Odds>? = null,
    @SerializedName("score") val score: Int? = null  // Make score nullable since future games won't have scores
)

data class CommonName(
    @SerializedName("default") val default: String = "",
    @SerializedName("fr") val fr: String? = null
)

data class PlaceName(
    @SerializedName("default") val default: String = "",
    @SerializedName("fr") val fr: String? = null
)

data class PlaceNameWithPreposition(
    @SerializedName("default") val default: String = "",
    @SerializedName("fr") val fr: String? = null
)

data class Odds(
    @SerializedName("providerId") val providerId: Int = 0,
    @SerializedName("value") val value: String = ""
)

data class PeriodDescriptor(
    @SerializedName("number") val number: Int = 0,
    @SerializedName("periodType") val periodType: String = "",
    @SerializedName("maxRegulationPeriods") val maxRegulationPeriods: Int = 3
)
