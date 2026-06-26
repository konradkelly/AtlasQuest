package com.atlasquest.app.data.model

/**
 * A continent — the top level of the map. Tapping one drills into its sub-regions.
 */
enum class Continent(val displayName: String) {
    NORTH_AMERICA("North America"),
    SOUTH_AMERICA("South America"),
    EUROPE("Europe"),
    AFRICA("Africa"),
    ASIA("Asia"),
    OCEANIA("Oceania");

    companion object {
        fun fromDisplayName(name: String): Continent? =
            entries.find { it.displayName.equals(name, ignoreCase = true) }
    }
}

/**
 * A sub-region — the second map level and the unit a quiz is scoped to.
 *
 * [isoCodes] are the ISO 3166-1 alpha-2 codes of the countries in this region; they
 * key into the country paths parsed from the bundled SVG. [id] is the stable key
 * stored in the question database (QuestionEntity.region).
 */
enum class Region(
    val id: String,
    val displayName: String,
    val continent: Continent,
    val isoCodes: List<String>,
) {
    // North America
    NORTHERN_AMERICA("northern_america", "Northern America", Continent.NORTH_AMERICA,
        listOf("US", "CA", "MX", "GL")),
    CENTRAL_AMERICA("central_america", "Central America", Continent.NORTH_AMERICA,
        listOf("GT", "BZ", "SV", "HN", "NI", "CR", "PA")),
    CARIBBEAN("caribbean", "Caribbean", Continent.NORTH_AMERICA,
        listOf("CU", "HT", "DO", "JM", "BS", "TT", "PR")),

    // South America
    SOUTH_AMERICA_ANDEAN("south_america_andean", "Andean States", Continent.SOUTH_AMERICA,
        listOf("CO", "VE", "EC", "PE", "BO")),
    SOUTH_AMERICA_SOUTHERN_CONE("south_america_southern_cone", "Southern Cone", Continent.SOUTH_AMERICA,
        listOf("AR", "CL", "UY", "PY")),
    SOUTH_AMERICA_BRAZIL("south_america_brazil", "Brazil & Guianas", Continent.SOUTH_AMERICA,
        listOf("BR", "GY", "SR", "GF")),

    // Europe
    WESTERN_EUROPE("western_europe", "Western Europe", Continent.EUROPE,
        listOf("FR", "DE", "NL", "BE", "LU", "AT", "CH", "IE", "GB")),
    NORTHERN_EUROPE("northern_europe", "Northern Europe", Continent.EUROPE,
        listOf("SE", "NO", "FI", "DK", "IS", "EE", "LV", "LT")),
    SOUTHERN_EUROPE("southern_europe", "Southern Europe", Continent.EUROPE,
        listOf("ES", "PT", "IT", "GR", "MT")),
    EASTERN_EUROPE("eastern_europe", "Eastern Europe", Continent.EUROPE,
        listOf("PL", "CZ", "SK", "HU", "RO", "BG", "UA", "BY", "MD", "RU")),
    BALKANS("balkans", "The Balkans", Continent.EUROPE,
        listOf("HR", "SI", "BA", "RS", "ME", "MK", "AL", "XK")),

    // Africa
    NORTH_AFRICA("north_africa", "North Africa", Continent.AFRICA,
        listOf("EG", "LY", "TN", "DZ", "MA", "SD", "EH")),
    WEST_AFRICA("west_africa", "West Africa", Continent.AFRICA,
        listOf("NG", "GH", "CI", "SN", "ML", "BF", "NE", "GN", "BJ", "TG", "SL", "LR", "MR", "GM", "GW")),
    EAST_AFRICA("east_africa", "East Africa", Continent.AFRICA,
        listOf("ET", "KE", "TZ", "UG", "RW", "BI", "SO", "SS", "ER", "DJ")),
    CENTRAL_AFRICA("central_africa", "Central Africa", Continent.AFRICA,
        listOf("CD", "CG", "CM", "CF", "TD", "GA", "GQ")),
    SOUTHERN_AFRICA("southern_africa", "Southern Africa", Continent.AFRICA,
        listOf("ZA", "AO", "ZM", "ZW", "MZ", "BW", "NA", "MW", "MG", "LS", "SZ")),

    // Asia
    MIDDLE_EAST("middle_east", "Middle East", Continent.ASIA,
        listOf("TR", "SA", "IR", "IQ", "SY", "JO", "IL", "LB", "AE", "OM", "YE", "KW", "QA", "BH")),
    CENTRAL_ASIA("central_asia", "Central Asia", Continent.ASIA,
        listOf("KZ", "UZ", "TM", "KG", "TJ", "AF")),
    SOUTH_ASIA("south_asia", "South Asia", Continent.ASIA,
        listOf("IN", "PK", "BD", "LK", "NP", "BT", "MV")),
    EAST_ASIA("east_asia", "East Asia", Continent.ASIA,
        listOf("CN", "JP", "KR", "KP", "MN", "TW")),
    SOUTHEAST_ASIA("southeast_asia", "Southeast Asia", Continent.ASIA,
        listOf("ID", "TH", "VN", "PH", "MY", "MM", "KH", "LA", "SG", "BN", "TL")),

    // Oceania
    AUSTRALASIA("australasia", "Australasia", Continent.OCEANIA,
        listOf("AU", "NZ", "PG")),
    PACIFIC_ISLANDS("pacific_islands", "Pacific Islands", Continent.OCEANIA,
        listOf("FJ", "SB", "VU", "WS", "TO", "KI", "FM", "PW", "MH", "NR", "TV"));

    companion object {
        fun forContinent(continent: Continent): List<Region> =
            entries.filter { it.continent == continent }

        fun fromId(id: String): Region? = entries.find { it.id == id }

        /** ISO alpha-2 code -> Region. Built once. */
        private val byIso: Map<String, Region> = buildMap {
            Region.entries.forEach { region -> region.isoCodes.forEach { iso -> put(iso, region) } }
        }

        fun forCountry(isoCode: String): Region? = byIso[isoCode.uppercase()]
    }
}
