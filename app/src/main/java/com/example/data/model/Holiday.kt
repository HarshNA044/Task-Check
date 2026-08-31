package com.example.data.model

import java.time.LocalDate
import java.time.Month

data class Holiday(
    val name: String,
    val date: LocalDate,
    val category: String = "Public Holiday",
    val emoji: String = "🎉",
    val description: String = ""
)

object HolidayProvider {
    fun getHolidaysForYear(year: Int): Map<LocalDate, Holiday> {
        val holidays = mutableListOf<Holiday>()

        // Fixed date holidays
        holidays.add(Holiday("New Year's Day", LocalDate.of(year, Month.JANUARY, 1), "Public Holiday", "🎆", "Global celebration welcoming the new calendar year"))
        holidays.add(Holiday("Valentine's Day", LocalDate.of(year, Month.FEBRUARY, 14), "Observance", "💖", "Celebration of love and affection"))
        holidays.add(Holiday("International Women's Day", LocalDate.of(year, Month.MARCH, 8), "Observance", "🌸", "Commemoration of women's achievements"))
        holidays.add(Holiday("St. Patrick's Day", LocalDate.of(year, Month.MARCH, 17), "Cultural Holiday", "🍀", "Traditional feast of Saint Patrick"))
        holidays.add(Holiday("Earth Day", LocalDate.of(year, Month.APRIL, 22), "Observance", "🌍", "Worldwide environmental support event"))
        holidays.add(Holiday("Labor Day / May Day", LocalDate.of(year, Month.MAY, 1), "Public Holiday", "🛠️", "International Workers' Day"))
        holidays.add(Holiday("World Environment Day", LocalDate.of(year, Month.JUNE, 5), "Observance", "🌿", "Global awareness for environmental protection"))
        holidays.add(Holiday("Juneteenth", LocalDate.of(year, Month.JUNE, 19), "National Holiday", "🕊️", "Commemoration of the end of slavery in the US"))
        holidays.add(Holiday("Independence Day", LocalDate.of(year, Month.JULY, 4), "National Holiday", "🎆", "US National Independence Day"))
        holidays.add(Holiday("International Youth Day", LocalDate.of(year, Month.AUGUST, 12), "Observance", "✨", "Celebrating youth potential and development"))
        holidays.add(Holiday("International Peace Day", LocalDate.of(year, Month.SEPTEMBER, 21), "Observance", "🕊️", "Dedicated to world peace and non-violence"))
        holidays.add(Holiday("Halloween", LocalDate.of(year, Month.OCTOBER, 31), "Cultural Holiday", "🎃", "Night of costumes, lanterns, and trick-or-treating"))
        holidays.add(Holiday("Veterans / Remembrance Day", LocalDate.of(year, Month.NOVEMBER, 11), "National Holiday", "🎖️", "Honoring military veterans"))
        holidays.add(Holiday("Christmas Eve", LocalDate.of(year, Month.DECEMBER, 24), "Observance", "🎄", "Evening preceding Christmas Day"))
        holidays.add(Holiday("Christmas Day", LocalDate.of(year, Month.DECEMBER, 25), "Public Holiday", "🎅", "Worldwide Christian holiday celebrating the birth of Jesus"))
        holidays.add(Holiday("Boxing Day", LocalDate.of(year, Month.DECEMBER, 26), "Public Holiday", "🎁", "Traditional holiday following Christmas Day"))
        holidays.add(Holiday("New Year's Eve", LocalDate.of(year, Month.DECEMBER, 31), "Observance", "🥂", "Celebration marking the end of the year"))

        // Common floating holidays for standard years (approximate/popular)
        try {
            // Martin Luther King Jr. Day (3rd Monday of Jan)
            val mlk = getNthDayOfWeek(year, Month.JANUARY, java.time.DayOfWeek.MONDAY, 3)
            holidays.add(Holiday("MLK Jr. Day", mlk, "Public Holiday", "⚖️", "Honoring civil rights leader Dr. Martin Luther King Jr."))

            // Presidents' Day (3rd Monday of Feb)
            val pres = getNthDayOfWeek(year, Month.FEBRUARY, java.time.DayOfWeek.MONDAY, 3)
            holidays.add(Holiday("Presidents' Day", pres, "Public Holiday", "🏛️", "Honoring American presidents"))

            // Memorial Day (Last Monday of May)
            val mem = getLastDayOfWeek(year, Month.MAY, java.time.DayOfWeek.MONDAY)
            holidays.add(Holiday("Memorial Day", mem, "Public Holiday", "🎖️", "Honoring fallen service members"))

            // Labor Day (1st Monday of Sep)
            val lab = getNthDayOfWeek(year, Month.SEPTEMBER, java.time.DayOfWeek.MONDAY, 1)
            holidays.add(Holiday("Labor Day", lab, "Public Holiday", "⚒️", "Celebrating laborers and working people"))

            // Thanksgiving (4th Thursday of Nov)
            val thx = getNthDayOfWeek(year, Month.NOVEMBER, java.time.DayOfWeek.THURSDAY, 4)
            holidays.add(Holiday("Thanksgiving Day", thx, "National Holiday", "🦃", "National day of giving thanks and family feasts"))
        } catch (e: Exception) {
            // fallback
        }

        return holidays.associateBy { it.date }
    }

    fun getHoliday(date: LocalDate): Holiday? {
        val yearHolidays = getHolidaysForYear(date.year)
        return yearHolidays[date]
    }

    private fun getNthDayOfWeek(year: Int, month: Month, dayOfWeek: java.time.DayOfWeek, n: Int): LocalDate {
        var count = 0
        var date = LocalDate.of(year, month, 1)
        while (date.month == month) {
            if (date.dayOfWeek == dayOfWeek) {
                count++
                if (count == n) return date
            }
            date = date.plusDays(1)
        }
        return LocalDate.of(year, month, 1)
    }

    private fun getLastDayOfWeek(year: Int, month: Month, dayOfWeek: java.time.DayOfWeek): LocalDate {
        var date = LocalDate.of(year, month, month.length(java.time.Year.isLeap(year.toLong())))
        while (date.dayOfWeek != dayOfWeek) {
            date = date.minusDays(1)
        }
        return date
    }
}
