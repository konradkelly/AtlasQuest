package com.atlasquest.app.data.model

/**
 * Bridges the two country identifiers AtlasQuest uses:
 *  - [Region] lists countries by ISO 3166-1 **alpha-2** code ("US", "CA").
 *  - The bundled globe data (assets/globe/countries-110m.json) keys features by
 *    ISO 3166-1 **numeric** id ("840", "124") with a `properties.name` fallback.
 *
 * The subregion globe needs numeric ids to color and hit-test countries, so we
 * convert here. [Region] stays the single source of truth for which countries
 * belong to which subregion; this object only translates the codes.
 */
object IsoNumeric {

    /** alpha-2 -> numeric, for every code that appears in [Region]. */
    val alpha2ToNumeric: Map<String, String> = mapOf(
        // North America
        "US" to "840", "CA" to "124", "MX" to "484", "GL" to "304",
        "GT" to "320", "BZ" to "84", "SV" to "222", "HN" to "340",
        "NI" to "558", "CR" to "188", "PA" to "591",
        "CU" to "192", "HT" to "332", "DO" to "214", "JM" to "388",
        "BS" to "44", "TT" to "780", "PR" to "630",
        // South America
        "CO" to "170", "VE" to "862", "EC" to "218", "PE" to "604", "BO" to "68",
        "AR" to "32", "CL" to "152", "UY" to "858", "PY" to "600",
        "BR" to "76", "GY" to "328", "SR" to "740", "GF" to "254",
        // Europe
        "FR" to "250", "DE" to "276", "NL" to "528", "BE" to "56", "LU" to "442",
        "AT" to "40", "CH" to "756", "IE" to "372", "GB" to "826",
        "SE" to "752", "NO" to "578", "FI" to "246", "DK" to "208", "IS" to "352",
        "EE" to "233", "LV" to "428", "LT" to "440",
        "ES" to "724", "PT" to "620", "IT" to "380", "GR" to "300", "MT" to "470",
        "PL" to "616", "CZ" to "203", "SK" to "703", "HU" to "348", "RO" to "642",
        "BG" to "100", "UA" to "804", "BY" to "112", "MD" to "498", "RU" to "643",
        "HR" to "191", "SI" to "705", "BA" to "70", "RS" to "688", "ME" to "499",
        "MK" to "807", "AL" to "8", // XK (Kosovo) has no numeric id -> nameToRegionId
        // Africa
        "EG" to "818", "LY" to "434", "TN" to "788", "DZ" to "12", "MA" to "504",
        "SD" to "729", "EH" to "732",
        "NG" to "566", "GH" to "288", "CI" to "384", "SN" to "686", "ML" to "466",
        "BF" to "854", "NE" to "562", "GN" to "324", "BJ" to "204", "TG" to "768",
        "SL" to "694", "LR" to "430", "MR" to "478", "GM" to "270", "GW" to "624",
        "ET" to "231", "KE" to "404", "TZ" to "834", "UG" to "800", "RW" to "646",
        "BI" to "108", "SO" to "706", "SS" to "728", "ER" to "232", "DJ" to "262",
        "CD" to "180", "CG" to "178", "CM" to "120", "CF" to "140", "TD" to "148",
        "GA" to "266", "GQ" to "226",
        "ZA" to "710", "AO" to "24", "ZM" to "894", "ZW" to "716", "MZ" to "508",
        "BW" to "72", "NA" to "516", "MW" to "454", "MG" to "450", "LS" to "426",
        "SZ" to "748",
        // Asia
        "TR" to "792", "SA" to "682", "IR" to "364", "IQ" to "368", "SY" to "760",
        "JO" to "400", "IL" to "376", "LB" to "422", "AE" to "784", "OM" to "512",
        "YE" to "887", "KW" to "414", "QA" to "634", "BH" to "48",
        "KZ" to "398", "UZ" to "860", "TM" to "795", "KG" to "417", "TJ" to "762",
        "AF" to "4",
        "IN" to "356", "PK" to "586", "BD" to "50", "LK" to "144", "NP" to "524",
        "BT" to "64", "MV" to "462",
        "CN" to "156", "JP" to "392", "KR" to "410", "KP" to "408", "MN" to "496",
        "TW" to "158",
        "ID" to "360", "TH" to "764", "VN" to "704", "PH" to "608", "MY" to "458",
        "MM" to "104", "KH" to "116", "LA" to "418", "SG" to "702", "BN" to "96",
        "TL" to "626",
        // Oceania
        "AU" to "36", "NZ" to "554", "PG" to "598",
        "FJ" to "242", "SB" to "90", "VU" to "548", "WS" to "882", "TO" to "776",
        "KI" to "296", "FM" to "583", "PW" to "585", "MH" to "584", "NR" to "520",
        "TV" to "798",
    )

    /**
     * Features in the 110m dataset that carry no usable numeric id and must be
     * matched by Natural Earth `properties.name`. Maps that name to the
     * [Region.id] it belongs to (mirrors the continent globe's NAME_OVERRIDE).
     */
    val nameToRegionId: Map<String, String> = mapOf(
        "Kosovo" to "balkans",        // XK — no numeric ISO code
        "Somaliland" to "east_africa", // de facto part of Somalia (SO)
        "Puerto Rico" to "caribbean", // belt-and-suspenders if keyed by name
    )
}
