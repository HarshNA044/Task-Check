package com.example.data.model

import java.time.LocalDate
import java.time.Month
import java.util.concurrent.ConcurrentHashMap

data class Holiday(
    val name: String,
    val date: LocalDate,
    val category: String = "National Holiday",
    val emoji: String = "🇮🇳",
    val description: String = ""
)

object HolidayProvider {

    private val cache = ConcurrentHashMap<Int, Map<LocalDate, Holiday>>()

    fun getHolidaysForYear(year: Int): Map<LocalDate, Holiday> {
        return cache.computeIfAbsent(year) { y ->
            buildHolidaysForYear(y)
        }
    }

    fun getHoliday(date: LocalDate): Holiday? {
        return getHolidaysForYear(date.year)[date]
    }

    private fun buildHolidaysForYear(year: Int): Map<LocalDate, Holiday> {
        val list = mutableListOf<Holiday>()

        // 1. National Gazetted Holidays (Fixed Dates in India)
        list.add(
            Holiday(
                name = "Republic Day",
                date = LocalDate.of(year, Month.JANUARY, 26),
                category = "National Holiday (Gazetted)",
                emoji = "🇮🇳",
                description = "Celebrates the Constitution of India coming into effect in 1950"
            )
        )
        list.add(
            Holiday(
                name = "Independence Day",
                date = LocalDate.of(year, Month.AUGUST, 15),
                category = "National Holiday (Gazetted)",
                emoji = "🇮🇳",
                description = "Commemorates the nation's independence in 1947"
            )
        )
        list.add(
            Holiday(
                name = "Mahatma Gandhi Jayanti",
                date = LocalDate.of(year, Month.OCTOBER, 2),
                category = "National Holiday (Gazetted)",
                emoji = "🇮🇳",
                description = "Birth anniversary of the Father of the Nation, Mahatma Gandhi & Lal Bahadur Shastri"
            )
        )

        // 2. Fixed Date Annual Observances & Festivals across India
        list.add(
            Holiday(
                name = "New Year's Day",
                date = LocalDate.of(year, Month.JANUARY, 1),
                category = "Public Holiday",
                emoji = "🎆",
                description = "Welcoming the beginning of the Gregorian new calendar year"
            )
        )
        list.add(
            Holiday(
                name = "Lohri",
                date = LocalDate.of(year, Month.JANUARY, 13),
                category = "Harvest Festival",
                emoji = "🔥",
                description = "Punjabi folk harvest festival marking the end of winter solstice"
            )
        )
        list.add(
            Holiday(
                name = "Makar Sankranti / Pongal / Maghi",
                date = LocalDate.of(year, Month.JANUARY, 14),
                category = "Harvest Festival",
                emoji = "🪁",
                description = "Solar transition into Makara Rashi; grand harvest feast across India (Pongal, Uttarayan, Bihu)"
            )
        )
        list.add(
            Holiday(
                name = "Netaji Subhas Chandra Bose Jayanti",
                date = LocalDate.of(year, Month.JANUARY, 23),
                category = "Parakram Diwas",
                emoji = "🎖️",
                description = "Honoring the legendary freedom fighter Netaji Subhas Chandra Bose"
            )
        )
        list.add(
            Holiday(
                name = "Chhatrapati Shivaji Maharaj Jayanti",
                date = LocalDate.of(year, Month.FEBRUARY, 19),
                category = "State Public Holiday",
                emoji = "⚔️",
                description = "Birth anniversary of the Great Maratha Emperor Chhatrapati Shivaji"
            )
        )
        list.add(
            Holiday(
                name = "Dr. B.R. Ambedkar Jayanti",
                date = LocalDate.of(year, Month.APRIL, 14),
                category = "Public Holiday (Gazetted)",
                emoji = "⚖️",
                description = "Birth anniversary of Dr. Bhimrao Ramji Ambedkar, Chief Architect of the Indian Constitution"
            )
        )
        list.add(
            Holiday(
                name = "Baisakhi / Vishu / Puthandu",
                date = LocalDate.of(year, Month.APRIL, 14),
                category = "Traditional New Year",
                emoji = "🌾",
                description = "Celebration of solar New Year across Punjab (Baisakhi), Kerala (Vishu), and Tamil Nadu (Puthandu)"
            )
        )
        list.add(
            Holiday(
                name = "Maharashtra Day / International Labour Day",
                date = LocalDate.of(year, Month.MAY, 1),
                category = "Public Holiday",
                emoji = "🛠️",
                description = "Commemoration of Maharashtra statehood, Gujarat Day & May Day"
            )
        )
        list.add(
            Holiday(
                name = "Rabindranath Tagore Jayanti",
                date = LocalDate.of(year, Month.MAY, 8),
                category = "Cultural Holiday",
                emoji = "✍️",
                description = "Birth anniversary of Nobel Laureate Gurudev Rabindranath Tagore"
            )
        )
        list.add(
            Holiday(
                name = "International Day of Yoga",
                date = LocalDate.of(year, Month.JUNE, 21),
                category = "Observance",
                emoji = "🧘",
                description = "Global celebration of ancient Indian yoga for physical and mental wellness"
            )
        )
        list.add(
            Holiday(
                name = "Shaheed Bhagat Singh Jayanti",
                date = LocalDate.of(year, Month.SEPTEMBER, 28),
                category = "National Remembrance",
                emoji = "🕊️",
                description = "Tribute to iconic young revolutionary martyr Bhagat Singh"
            )
        )
        list.add(
            Holiday(
                name = "Goa Liberation Day",
                date = LocalDate.of(year, Month.DECEMBER, 19),
                category = "Regional Holiday",
                emoji = "🌴",
                description = "Commemoration of the liberation of Goa in 1961"
            )
        )
        list.add(
            Holiday(
                name = "Christmas Eve",
                date = LocalDate.of(year, Month.DECEMBER, 24),
                category = "Observance",
                emoji = "🎄",
                description = "Eve of Christmas celebrations and midnight mass"
            )
        )
        list.add(
            Holiday(
                name = "Christmas Day",
                date = LocalDate.of(year, Month.DECEMBER, 25),
                category = "Gazetted Holiday",
                emoji = "🎅",
                description = "Celebration of the birth of Jesus Christ"
            )
        )
        list.add(
            Holiday(
                name = "Veer Baal Diwas",
                date = LocalDate.of(year, Month.DECEMBER, 26),
                category = "National Observance",
                emoji = "⚔️",
                description = "Honoring the supreme sacrifice of the Sahibzadas, young sons of Guru Gobind Singh Ji"
            )
        )
        list.add(
            Holiday(
                name = "New Year's Eve",
                date = LocalDate.of(year, Month.DECEMBER, 31),
                category = "Observance",
                emoji = "🥂",
                description = "Festive evening concluding the calendar year"
            )
        )

        // 3. Indian Lunar & Multi-Year Festivals
        addVariableIndianFestivals(year, list)

        return list.associateBy { it.date }
    }

    private fun addVariableIndianFestivals(year: Int, list: mutableListHoliday) {
        when (year) {
            2024 -> {
                list.add(Holiday("Guru Gobind Singh Jayanti", LocalDate.of(2024, Month.JANUARY, 17), "Religious Holiday", "👳", "Prakash Parv of 10th Sikh Guru"))
                list.add(Holiday("Vasant Panchami / Saraswati Puja", LocalDate.of(2024, Month.FEBRUARY, 14), "Hindu Festival", "🌸", "Worship of Goddess Saraswati and welcoming spring"))
                list.add(Holiday("Maha Shivratri", LocalDate.of(2024, Month.MARCH, 8), "Gazetted Holiday", "🔱", "Grand night of Lord Shiva devotion and meditation"))
                list.add(Holiday("Holika Dahan", LocalDate.of(2024, Month.MARCH, 24), "Hindu Festival", "🔥", "Victory of good over evil with sacred bonfires"))
                list.add(Holiday("Holi (Festival of Colors)", LocalDate.of(2024, Month.MARCH, 25), "Gazetted Holiday", "🎨", "Vibrant carnival of colors, joy, and unity"))
                list.add(Holiday("Good Friday", LocalDate.of(2024, Month.MARCH, 29), "Gazetted Holiday", "✝️", "Christian day of solemn prayer commemorating the Passion"))
                list.add(Holiday("Gudi Padwa / Ugadi", LocalDate.of(2024, Month.APRIL, 9), "Traditional New Year", "🪔", "New Year celebration in Maharashtra, Karnataka, and Andhra"))
                list.add(Holiday("Eid-ul-Fitr (Ramzan Eid)", LocalDate.of(2024, Month.APRIL, 11), "Gazetted Holiday", "🌙", "Islamic celebration culminating the holy month of Ramadan"))
                list.add(Holiday("Ram Navami", LocalDate.of(2024, Month.APRIL, 17), "Gazetted Holiday", "🏹", "Celebration of the birth of Lord Rama"))
                list.add(Holiday("Mahavir Jayanti", LocalDate.of(2024, Month.APRIL, 21), "Gazetted Holiday", "🕊️", "Birth anniversary of Lord Mahavira, 24th Jain Tirthankara"))
                list.add(Holiday("Buddha Purnima", LocalDate.of(2024, Month.MAY, 23), "Gazetted Holiday", "🪷", "Celebrating the birth, enlightenment, and parinirvana of Gautama Buddha"))
                list.add(Holiday("Bakrid / Eid al-Adha", LocalDate.of(2024, Month.JUNE, 17), "Gazetted Holiday", "🐑", "Feast of the Sacrifice observed by Muslims"))
                list.add(Holiday("Muharram (Ashura)", LocalDate.of(2024, Month.JULY, 17), "Gazetted Holiday", "🕌", "Holy month of remembrance in Islam"))
                list.add(Holiday("Raksha Bandhan", LocalDate.of(2024, Month.AUGUST, 19), "Hindu Festival", "🧵", "Celebration of sibling love and protective sacred thread"))
                list.add(Holiday("Krishna Janmashtami", LocalDate.of(2024, Month.AUGUST, 26), "Gazetted Holiday", "🪈", "Festive celebration of the divine birth of Lord Krishna"))
                list.add(Holiday("Ganesh Chaturthi", LocalDate.of(2024, Month.SEPTEMBER, 7), "Gazetted Holiday", "🐘", "Arrival of Lord Ganesha with grand pandals and celebrations"))
                list.add(Holiday("Onam (Thiruvonam)", LocalDate.of(2024, Month.SEPTEMBER, 15), "Harvest Festival", "🌼", "Kerala's harvest festival honoring King Mahabali"))
                list.add(Holiday("Milad-un-Nabi (Eid-e-Milad)", LocalDate.of(2024, Month.SEPTEMBER, 16), "Gazetted Holiday", "🌙", "Commemoration of the birth of Prophet Muhammad"))
                list.add(Holiday("Maha Navami (Durga Puja)", LocalDate.of(2024, Month.OCTOBER, 11), "Gazetted Holiday", "🔱", "Grand ninth day of Durga Puja and Ayudha Puja"))
                list.add(Holiday("Dussehra / Vijayadashami", LocalDate.of(2024, Month.OCTOBER, 12), "Gazetted Holiday", "🏹", "Triumph of Lord Rama over Ravana and Goddess Durga over Mahishasura"))
                list.add(Holiday("Karwa Chauth", LocalDate.of(2024, Month.OCTOBER, 20), "Hindu Festival", "🌕", "Traditional fasting for spouse health and longevity"))
                list.add(Holiday("Dhanteras", LocalDate.of(2024, Month.OCTOBER, 29), "Festival of Wealth", "🪙", "Auspicious first day of Diwali festivities honoring Lord Dhanvantari"))
                list.add(Holiday("Diwali / Deepavali", LocalDate.of(2024, Month.OCTOBER, 31), "Gazetted Holiday", "🪔", "Festival of Lights celebrating prosperity, light, and Goddess Lakshmi"))
                list.add(Holiday("Govardhan Puja / New Year", LocalDate.of(2024, Month.NOVEMBER, 2), "Hindu Festival", "🏔️", "Worship of Mount Govardhan and Annakut"))
                list.add(Holiday("Bhai Dooj", LocalDate.of(2024, Month.NOVEMBER, 3), "Hindu Festival", "🎁", "Auspicious celebration of the bond between brothers and sisters"))
                list.add(Holiday("Chhath Puja", LocalDate.of(2024, Month.NOVEMBER, 7), "Sun Worship Festival", "🌅", "Sacred Vedic rituals dedicated to Sun God Surya and Chhathi Maiya"))
                list.add(Holiday("Guru Nanak Jayanti", LocalDate.of(2024, Month.NOVEMBER, 15), "Gazetted Holiday", "👳", "Prakash Utsav celebrating the founder of Sikhism, Guru Nanak Dev Ji"))
            }
            2025 -> {
                list.add(Holiday("Guru Gobind Singh Jayanti", LocalDate.of(2025, Month.JANUARY, 6), "Religious Holiday", "👳", "Prakash Parv of 10th Sikh Guru"))
                list.add(Holiday("Vasant Panchami / Saraswati Puja", LocalDate.of(2025, Month.FEBRUARY, 2), "Hindu Festival", "🌸", "Worship of Goddess Saraswati and welcoming spring"))
                list.add(Holiday("Maha Shivratri", LocalDate.of(2025, Month.FEBRUARY, 26), "Gazetted Holiday", "🔱", "Grand night of Lord Shiva devotion and meditation"))
                list.add(Holiday("Holika Dahan", LocalDate.of(2025, Month.MARCH, 13), "Hindu Festival", "🔥", "Victory of good over evil with sacred bonfires"))
                list.add(Holiday("Holi (Festival of Colors)", LocalDate.of(2025, Month.MARCH, 14), "Gazetted Holiday", "🎨", "Vibrant carnival of colors, joy, and unity"))
                list.add(Holiday("Gudi Padwa / Ugadi", LocalDate.of(2025, Month.MARCH, 30), "Traditional New Year", "🪔", "New Year celebration in Maharashtra, Karnataka, and Andhra"))
                list.add(Holiday("Eid-ul-Fitr (Ramzan Eid)", LocalDate.of(2025, Month.MARCH, 31), "Gazetted Holiday", "🌙", "Islamic celebration culminating the holy month of Ramadan"))
                list.add(Holiday("Ram Navami", LocalDate.of(2025, Month.APRIL, 6), "Gazetted Holiday", "🏹", "Celebration of the birth of Lord Rama"))
                list.add(Holiday("Mahavir Jayanti", LocalDate.of(2025, Month.APRIL, 10), "Gazetted Holiday", "🕊️", "Birth anniversary of Lord Mahavira, 24th Jain Tirthankara"))
                list.add(Holiday("Good Friday", LocalDate.of(2025, Month.APRIL, 18), "Gazetted Holiday", "✝️", "Christian day of solemn prayer commemorating the Passion"))
                list.add(Holiday("Buddha Purnima", LocalDate.of(2025, Month.MAY, 12), "Gazetted Holiday", "🪷", "Celebrating the birth, enlightenment, and parinirvana of Gautama Buddha"))
                list.add(Holiday("Bakrid / Eid al-Adha", LocalDate.of(2025, Month.JUNE, 7), "Gazetted Holiday", "🐑", "Feast of the Sacrifice observed by Muslims"))
                list.add(Holiday("Muharram (Ashura)", LocalDate.of(2025, Month.JULY, 6), "Gazetted Holiday", "🕌", "Holy month of remembrance in Islam"))
                list.add(Holiday("Raksha Bandhan", LocalDate.of(2025, Month.AUGUST, 9), "Hindu Festival", "🧵", "Celebration of sibling love and protective sacred thread"))
                list.add(Holiday("Krishna Janmashtami", LocalDate.of(2025, Month.AUGUST, 16), "Gazetted Holiday", "🪈", "Festive celebration of the divine birth of Lord Krishna"))
                list.add(Holiday("Ganesh Chaturthi", LocalDate.of(2025, Month.AUGUST, 27), "Gazetted Holiday", "🐘", "Arrival of Lord Ganesha with grand pandals and celebrations"))
                list.add(Holiday("Onam (Thiruvonam)", LocalDate.of(2025, Month.SEPTEMBER, 5), "Harvest Festival", "🌼", "Kerala's harvest festival honoring King Mahabali"))
                list.add(Holiday("Milad-un-Nabi (Eid-e-Milad)", LocalDate.of(2025, Month.SEPTEMBER, 5), "Gazetted Holiday", "🌙", "Commemoration of the birth of Prophet Muhammad"))
                list.add(Holiday("Maha Navami (Durga Puja)", LocalDate.of(2025, Month.OCTOBER, 1), "Gazetted Holiday", "🔱", "Grand ninth day of Durga Puja and Ayudha Puja"))
                list.add(Holiday("Dussehra / Vijayadashami", LocalDate.of(2025, Month.OCTOBER, 2), "Gazetted Holiday", "🏹", "Triumph of Lord Rama over Ravana and Goddess Durga over Mahishasura"))
                list.add(Holiday("Karwa Chauth", LocalDate.of(2025, Month.OCTOBER, 10), "Hindu Festival", "🌕", "Traditional fasting for spouse health and longevity"))
                list.add(Holiday("Dhanteras", LocalDate.of(2025, Month.OCTOBER, 18), "Festival of Wealth", "🪙", "Auspicious first day of Diwali festivities honoring Lord Dhanvantari"))
                list.add(Holiday("Diwali / Deepavali", LocalDate.of(2025, Month.OCTOBER, 20), "Gazetted Holiday", "🪔", "Festival of Lights celebrating prosperity, light, and Goddess Lakshmi"))
                list.add(Holiday("Govardhan Puja / New Year", LocalDate.of(2025, Month.OCTOBER, 22), "Hindu Festival", "🏔️", "Worship of Mount Govardhan and Annakut"))
                list.add(Holiday("Bhai Dooj", LocalDate.of(2025, Month.OCTOBER, 23), "Hindu Festival", "🎁", "Auspicious celebration of the bond between brothers and sisters"))
                list.add(Holiday("Chhath Puja", LocalDate.of(2025, Month.OCTOBER, 28), "Sun Worship Festival", "🌅", "Sacred Vedic rituals dedicated to Sun God Surya and Chhathi Maiya"))
                list.add(Holiday("Guru Nanak Jayanti", LocalDate.of(2025, Month.NOVEMBER, 5), "Gazetted Holiday", "👳", "Prakash Utsav celebrating the founder of Sikhism, Guru Nanak Dev Ji"))
            }
            2026 -> {
                list.add(Holiday("Guru Gobind Singh Jayanti", LocalDate.of(2026, Month.JANUARY, 25), "Religious Holiday", "👳", "Prakash Parv of 10th Sikh Guru"))
                list.add(Holiday("Vasant Panchami / Saraswati Puja", LocalDate.of(2026, Month.JANUARY, 23), "Hindu Festival", "🌸", "Worship of Goddess Saraswati and welcoming spring"))
                list.add(Holiday("Maha Shivratri", LocalDate.of(2026, Month.FEBRUARY, 15), "Gazetted Holiday", "🔱", "Grand night of Lord Shiva devotion and meditation"))
                list.add(Holiday("Holika Dahan", LocalDate.of(2026, Month.MARCH, 3), "Hindu Festival", "🔥", "Victory of good over evil with sacred bonfires"))
                list.add(Holiday("Holi (Festival of Colors)", LocalDate.of(2026, Month.MARCH, 4), "Gazetted Holiday", "🎨", "Vibrant carnival of colors, joy, and unity"))
                list.add(Holiday("Gudi Padwa / Ugadi", LocalDate.of(2026, Month.MARCH, 19), "Traditional New Year", "🪔", "New Year celebration in Maharashtra, Karnataka, and Andhra"))
                list.add(Holiday("Eid-ul-Fitr (Ramzan Eid)", LocalDate.of(2026, Month.MARCH, 20), "Gazetted Holiday", "🌙", "Islamic celebration culminating the holy month of Ramadan"))
                list.add(Holiday("Ram Navami", LocalDate.of(2026, Month.MARCH, 27), "Gazetted Holiday", "🏹", "Celebration of the birth of Lord Rama"))
                list.add(Holiday("Mahavir Jayanti", LocalDate.of(2026, Month.MARCH, 31), "Gazetted Holiday", "🕊️", "Birth anniversary of Lord Mahavira, 24th Jain Tirthankara"))
                list.add(Holiday("Good Friday", LocalDate.of(2026, Month.APRIL, 3), "Gazetted Holiday", "✝️", "Christian day of solemn prayer commemorating the Passion"))
                list.add(Holiday("Buddha Purnima", LocalDate.of(2026, Month.MAY, 1), "Gazetted Holiday", "🪷", "Celebrating the birth, enlightenment, and parinirvana of Gautama Buddha"))
                list.add(Holiday("Bakrid / Eid al-Adha", LocalDate.of(2026, Month.MAY, 27), "Gazetted Holiday", "🐑", "Feast of the Sacrifice observed by Muslims"))
                list.add(Holiday("Muharram (Ashura)", LocalDate.of(2026, Month.JUNE, 25), "Gazetted Holiday", "🕌", "Holy month of remembrance in Islam"))
                list.add(Holiday("Milad-un-Nabi (Eid-e-Milad)", LocalDate.of(2026, Month.AUGUST, 25), "Gazetted Holiday", "🌙", "Commemoration of the birth of Prophet Muhammad"))
                list.add(Holiday("Raksha Bandhan", LocalDate.of(2026, Month.AUGUST, 28), "Hindu Festival", "🧵", "Celebration of sibling love and protective sacred thread"))
                list.add(Holiday("Krishna Janmashtami", LocalDate.of(2026, Month.SEPTEMBER, 4), "Gazetted Holiday", "🪈", "Festive celebration of the divine birth of Lord Krishna"))
                list.add(Holiday("Ganesh Chaturthi", LocalDate.of(2026, Month.SEPTEMBER, 14), "Gazetted Holiday", "🐘", "Arrival of Lord Ganesha with grand pandals and celebrations"))
                list.add(Holiday("Onam (Thiruvonam)", LocalDate.of(2026, Month.SEPTEMBER, 25), "Harvest Festival", "🌼", "Kerala's harvest festival honoring King Mahabali"))
                list.add(Holiday("Maha Navami (Durga Puja)", LocalDate.of(2026, Month.OCTOBER, 19), "Gazetted Holiday", "🔱", "Grand ninth day of Durga Puja and Ayudha Puja"))
                list.add(Holiday("Dussehra / Vijayadashami", LocalDate.of(2026, Month.OCTOBER, 20), "Gazetted Holiday", "🏹", "Triumph of Lord Rama over Ravana and Goddess Durga over Mahishasura"))
                list.add(Holiday("Karwa Chauth", LocalDate.of(2026, Month.OCTOBER, 29), "Hindu Festival", "🌕", "Traditional fasting for spouse health and longevity"))
                list.add(Holiday("Dhanteras", LocalDate.of(2026, Month.NOVEMBER, 6), "Festival of Wealth", "🪙", "Auspicious first day of Diwali festivities honoring Lord Dhanvantari"))
                list.add(Holiday("Diwali / Deepavali", LocalDate.of(2026, Month.NOVEMBER, 8), "Gazetted Holiday", "🪔", "Festival of Lights celebrating prosperity, light, and Goddess Lakshmi"))
                list.add(Holiday("Govardhan Puja / New Year", LocalDate.of(2026, Month.NOVEMBER, 9), "Hindu Festival", "🏔️", "Worship of Mount Govardhan and Annakut"))
                list.add(Holiday("Bhai Dooj", LocalDate.of(2026, Month.NOVEMBER, 10), "Hindu Festival", "🎁", "Auspicious celebration of the bond between brothers and sisters"))
                list.add(Holiday("Chhath Puja", LocalDate.of(2026, Month.NOVEMBER, 15), "Sun Worship Festival", "🌅", "Sacred Vedic rituals dedicated to Sun God Surya and Chhathi Maiya"))
                list.add(Holiday("Guru Nanak Jayanti", LocalDate.of(2026, Month.NOVEMBER, 24), "Gazetted Holiday", "👳", "Prakash Utsav celebrating the founder of Sikhism, Guru Nanak Dev Ji"))
            }
            2027 -> {
                list.add(Holiday("Maha Shivratri", LocalDate.of(2027, Month.MARCH, 6), "Gazetted Holiday", "🔱", "Grand night of Lord Shiva devotion and meditation"))
                list.add(Holiday("Eid-ul-Fitr (Ramzan Eid)", LocalDate.of(2027, Month.MARCH, 10), "Gazetted Holiday", "🌙", "Islamic celebration culminating the holy month of Ramadan"))
                list.add(Holiday("Holika Dahan", LocalDate.of(2027, Month.MARCH, 22), "Hindu Festival", "🔥", "Victory of good over evil with sacred bonfires"))
                list.add(Holiday("Holi (Festival of Colors)", LocalDate.of(2027, Month.MARCH, 23), "Gazetted Holiday", "🎨", "Vibrant carnival of colors, joy, and unity"))
                list.add(Holiday("Good Friday", LocalDate.of(2027, Month.MARCH, 26), "Gazetted Holiday", "✝️", "Christian day of solemn prayer commemorating the Passion"))
                list.add(Holiday("Gudi Padwa / Ugadi", LocalDate.of(2027, Month.APRIL, 7), "Traditional New Year", "🪔", "New Year celebration in Maharashtra, Karnataka, and Andhra"))
                list.add(Holiday("Ram Navami", LocalDate.of(2027, Month.APRIL, 15), "Gazetted Holiday", "🏹", "Celebration of the birth of Lord Rama"))
                list.add(Holiday("Mahavir Jayanti", LocalDate.of(2027, Month.APRIL, 19), "Gazetted Holiday", "🕊️", "Birth anniversary of Lord Mahavira, 24th Jain Tirthankara"))
                list.add(Holiday("Bakrid / Eid al-Adha", LocalDate.of(2027, Month.MAY, 17), "Gazetted Holiday", "🐑", "Feast of the Sacrifice observed by Muslims"))
                list.add(Holiday("Buddha Purnima", LocalDate.of(2027, Month.MAY, 20), "Gazetted Holiday", "🪷", "Celebrating the birth, enlightenment, and parinirvana of Gautama Buddha"))
                list.add(Holiday("Muharram (Ashura)", LocalDate.of(2027, Month.JUNE, 15), "Gazetted Holiday", "🕌", "Holy month of remembrance in Islam"))
                list.add(Holiday("Milad-un-Nabi (Eid-e-Milad)", LocalDate.of(2027, Month.AUGUST, 15), "Gazetted Holiday", "🌙", "Commemoration of the birth of Prophet Muhammad"))
                list.add(Holiday("Raksha Bandhan", LocalDate.of(2027, Month.AUGUST, 17), "Hindu Festival", "🧵", "Celebration of sibling love and protective sacred thread"))
                list.add(Holiday("Krishna Janmashtami", LocalDate.of(2027, Month.AUGUST, 25), "Gazetted Holiday", "🪈", "Festive celebration of the divine birth of Lord Krishna"))
                list.add(Holiday("Ganesh Chaturthi", LocalDate.of(2027, Month.SEPTEMBER, 4), "Gazetted Holiday", "🐘", "Arrival of Lord Ganesha with grand pandals and celebrations"))
                list.add(Holiday("Dussehra / Vijayadashami", LocalDate.of(2027, Month.OCTOBER, 9), "Gazetted Holiday", "🏹", "Triumph of Lord Rama over Ravana and Goddess Durga over Mahishasura"))
                list.add(Holiday("Karwa Chauth", LocalDate.of(2027, Month.OCTOBER, 18), "Hindu Festival", "🌕", "Traditional fasting for spouse health and longevity"))
                list.add(Holiday("Diwali / Deepavali", LocalDate.of(2027, Month.OCTOBER, 29), "Gazetted Holiday", "🪔", "Festival of Lights celebrating prosperity, light, and Goddess Lakshmi"))
                list.add(Holiday("Govardhan Puja / New Year", LocalDate.of(2027, Month.OCTOBER, 30), "Hindu Festival", "🏔️", "Worship of Mount Govardhan and Annakut"))
                list.add(Holiday("Bhai Dooj", LocalDate.of(2027, Month.OCTOBER, 31), "Hindu Festival", "🎁", "Auspicious celebration of the bond between brothers and sisters"))
                list.add(Holiday("Chhath Puja", LocalDate.of(2027, Month.NOVEMBER, 4), "Sun Worship Festival", "🌅", "Sacred Vedic rituals dedicated to Sun God Surya and Chhathi Maiya"))
                list.add(Holiday("Guru Nanak Jayanti", LocalDate.of(2027, Month.NOVEMBER, 14), "Gazetted Holiday", "👳", "Prakash Utsav celebrating the founder of Sikhism, Guru Nanak Dev Ji"))
            }
            else -> {
                // Approximate fallback for any other year
                val easterSunday = computeEasterSunday(year)
                val goodFriday = easterSunday.minusDays(2)
                list.add(Holiday("Good Friday", goodFriday, "Gazetted Holiday", "✝️", "Christian day of solemn prayer commemorating the Passion"))
                list.add(Holiday("Maha Shivratri", LocalDate.of(year, Month.FEBRUARY, 24), "Gazetted Holiday", "🔱", "Grand night of Lord Shiva devotion and meditation"))
                list.add(Holiday("Holi (Festival of Colors)", LocalDate.of(year, Month.MARCH, 20), "Gazetted Holiday", "🎨", "Vibrant carnival of colors, joy, and unity"))
                list.add(Holiday("Ram Navami", LocalDate.of(year, Month.APRIL, 10), "Gazetted Holiday", "🏹", "Celebration of the birth of Lord Rama"))
                list.add(Holiday("Buddha Purnima", LocalDate.of(year, Month.MAY, 15), "Gazetted Holiday", "🪷", "Celebrating the birth, enlightenment, and parinirvana of Gautama Buddha"))
                list.add(Holiday("Raksha Bandhan", LocalDate.of(year, Month.AUGUST, 20), "Hindu Festival", "🧵", "Celebration of sibling love and protective sacred thread"))
                list.add(Holiday("Krishna Janmashtami", LocalDate.of(year, Month.AUGUST, 28), "Gazetted Holiday", "🪈", "Festive celebration of the divine birth of Lord Krishna"))
                list.add(Holiday("Ganesh Chaturthi", LocalDate.of(year, Month.SEPTEMBER, 10), "Gazetted Holiday", "🐘", "Arrival of Lord Ganesha with grand pandals and celebrations"))
                list.add(Holiday("Dussehra / Vijayadashami", LocalDate.of(year, Month.OCTOBER, 15), "Gazetted Holiday", "🏹", "Triumph of Lord Rama over Ravana and Goddess Durga over Mahishasura"))
                list.add(Holiday("Diwali / Deepavali", LocalDate.of(year, Month.NOVEMBER, 4), "Gazetted Holiday", "🪔", "Festival of Lights celebrating prosperity, light, and Goddess Lakshmi"))
                list.add(Holiday("Guru Nanak Jayanti", LocalDate.of(year, Month.NOVEMBER, 18), "Gazetted Holiday", "👳", "Prakash Utsav celebrating the founder of Sikhism, Guru Nanak Dev Ji"))
            }
        }
    }

    private fun computeEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
private typealias mutableListHoliday = MutableList<Holiday>

