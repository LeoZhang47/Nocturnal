package com.example.nocturnal.data.model

import com.mapbox.geojson.Point
import kotlin.math.*

fun Point.distanceTo(other: Point): Double {
    val earthRadius = 3958.8 // Radius of Earth in meters
    val lat1 = this.latitude()
    val lon1 = this.longitude()
    val lat2 = other.latitude()
    val lon2 = other.longitude()

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}
