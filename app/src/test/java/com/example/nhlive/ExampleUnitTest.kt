package com.example.nhlive

import com.example.nhlive.dataElements.Game
import com.example.nhlive.dataElements.GameDay
import com.example.nhlive.dataElements.GameSorter
import com.example.nhlive.dataElements.ScheduleResponse
import org.junit.Test

import org.junit.Assert.*
import java.time.ZonedDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test fun gameSorter_ordersCorrectly() {
        val live   = Game(gameState = "LIVE")
        val pre    = Game(gameState = "PRE")
        val future = Game(gameState = "FUT")
        val final  = Game(gameState = "FINAL")
        val unspec = Game(gameState = "XYZ")

        val ordered = GameSorter.sortGamesByStatus(listOf(unspec, final, pre, future, live))
        assertEquals(listOf(live, pre, future, final, unspec), ordered)
    }

    @Test fun formattedDateTime_todayTomorrowAndOther() {
        // pick a fixed instant: 2025-04-23T16:00Z => Eastern = noon on April 23, 2025
        val instant = "2025-04-23T16:00:00Z"
        val game = Game(startTimeUTC = instant)
        val s = game.formattedDateTime
        assertTrue(s.startsWith("Today at"))

        // tomorrow
        val tom = ZonedDateTime.parse("2025-04-24T14:30:00Z")
        val game2 = Game(startTimeUTC = tom.toString())
        assertTrue(game2.formattedDateTime.startsWith("Tomorrow at"))

        // other date
        val other = ZonedDateTime.parse("2025-05-05T18:15:00Z")
        val game3 = Game(startTimeUTC = other.toString())
        assertTrue(game3.formattedDateTime.contains("May"))
    }

    @Test
    fun groupAndSortGames_groupsByDate_and_ordersEachGroup() {
        // Two games on April 23, one LIVE and one FINAL; one PRE on April 24
        val liveOn23  = Game(startTimeUTC = "2025-04-23T10:00:00Z", gameState = "LIVE")
        val finalOn23 = Game(startTimeUTC = "2025-04-23T11:00:00Z", gameState = "FINAL")
        val preOn24   = Game(startTimeUTC = "2025-04-24T12:00:00Z", gameState = "PRE")

        val grouped = GameSorter.groupAndSortGames(listOf(finalOn23, preOn24, liveOn23))

        // should have two keys: "2025-04-23" and "2025-04-24"
        assertTrue(grouped.containsKey("2025-04-23"))
        assertTrue(grouped.containsKey("2025-04-24"))

        // within April 23, LIVE comes before FINAL
        val day23 = grouped["2025-04-23"]!!
        assertEquals(listOf(liveOn23, finalOn23), day23)

        // April 24 only has the PRE game
        val day24 = grouped["2025-04-24"]!!
        assertEquals(listOf(preOn24), day24)
    }

    @Test
    fun formattedDateTime_fallback_returnsSubstringBetweenTandZ() {
        // Force the fallback branch by using a bogus ISO string that still contains T…Z
        val g = Game(startTimeUTC = "prefixTaMiddleZsuffix", gameState = "")
        // substringAfter("T") => "aMiddleZsuffix", then substringBefore("Z") => "aMiddle"
        assertEquals("aMiddle", g.formattedDateTime)
    }

    @Test
    fun formattedDateTimeStacked_fallback_returnsSubstringBetweenTandZ() {
        val g = Game(startTimeUTC = "123TABC123Z456", gameState = "")
        // same logic for the stacked variant
        assertEquals("ABC123", g.formattedDateTimeStacked)
    }

    @Test
    fun sortGames_ordersUnknownStatusesLast() {
        val known = Game(gameState = "LIVE")
        val unknown = Game(gameState = "UNKNOWN")
        val ordered = GameSorter.sortGamesByStatus(listOf(unknown, known))
        assertEquals(listOf(known, unknown), ordered)
    }

    @Test
    fun scheduleResponse_canBeInstantiated_and_gameDayContainsCorrectCount() {
        // even though ScheduleResponse isn't doing much, we can ensure its list behavior
        val games = listOf(
            Game(startTimeUTC = "2025-04-25T10:00:00Z", gameState = "FUT"),
            Game(startTimeUTC = "2025-04-25T12:00:00Z", gameState = "FUT")
        )
        val day = GameDay(date = "2025-04-25", dayAbbrev = "Fri", numberOfGames = 2, games = games)
        val schedule = ScheduleResponse(gameWeek = listOf(day))

        assertEquals(1, schedule.gameWeek.size)
        assertEquals(2, schedule.gameWeek[0].games.size)
        assertEquals(2, schedule.gameWeek[0].numberOfGames)
    }
}