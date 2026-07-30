package com.locallens.utils

import com.locallens.data.model.Country

object CountriesRepository {

    val all: List<Country> = listOf(
        // Africa
        Country("South Africa", "za"),
        Country("Nigeria", "ng"),
        Country("Kenya", "ke"),
        Country("Egypt", "eg"),
        Country("Ghana", "gh"),
        Country("Ethiopia", "et"),
        Country("Morocco", "ma"),
        Country("Tanzania", "tz"),
        Country("Uganda", "ug"),
        Country("Senegal", "sn"),

        // Americas
        Country("United States", "us"),
        Country("Canada", "ca"),
        Country("Brazil", "br"),
        Country("Mexico", "mx"),
        Country("Argentina", "ar"),
        Country("Colombia", "co"),
        Country("Chile", "cl"),
        Country("Peru", "pe"),

        // Europe
        Country("United Kingdom", "gb"),
        Country("Germany", "de"),
        Country("France", "fr"),
        Country("Italy", "it"),
        Country("Spain", "es"),
        Country("Netherlands", "nl"),
        Country("Sweden", "se"),
        Country("Norway", "no"),
        Country("Poland", "pl"),
        Country("Ukraine", "ua"),
        Country("Russia", "ru"),

        // Asia
        Country("India", "in"),
        Country("Japan", "jp"),
        Country("China", "cn"),
        Country("South Korea", "kr"),
        Country("Indonesia", "id"),
        Country("Thailand", "th"),
        Country("Vietnam", "vn"),
        Country("Malaysia", "my"),
        Country("Singapore", "sg"),
        Country("Saudi Arabia", "sa"),
        Country("UAE", "ae"),
        Country("Israel", "il"),
        Country("Turkey", "tr"),

        // Oceania
        Country("Australia", "au"),
        Country("New Zealand", "nz"),

        // Middle East
        Country("Iran", "ir"),
        Country("Iraq", "iq"),
        Country("Pakistan", "pk"),
        Country("Bangladesh", "bd"),
    )

    fun findByCode(code: String): Country? = all.find { it.code.equals(code, ignoreCase = true) }

    fun searchByName(query: String): List<Country> =
        all.filter { it.name.contains(query, ignoreCase = true) }
}
