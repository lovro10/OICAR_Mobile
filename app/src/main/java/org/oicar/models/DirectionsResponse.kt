package org.oicar.models

data class DirectionResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val duration: Duration,
    val distance: Distance,
    val end_location: Location,
    val start_location: Location
)

data class Duration(
    val text: String,
    val value: Int
)

data class Distance(
    val text: String,
    val value: Int
)

data class Location(
    val lat: Double,
    val lng: Double
)
