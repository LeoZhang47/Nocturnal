package com.example.nocturnal

import com.mapbox.geojson.Point
import org.junit.Test
import kotlin.test.assertEquals
import com.example.nocturnal.data.model.distanceTo


class PointUnitTest {

    @Test
    fun distanceTo_isCorrect() {
        // Create two Point objects
        val point1 = Point.fromLngLat(-81.6944, 41.4993) // Cleveland, OH
        val point2 = Point.fromLngLat(-83.0458, 42.3314) // Detroit, MI

        // Calculate distance in miles
        val distanceMiles = point1.distanceTo(point2)
        // Verify the distance with a tolerance for floating-point arithmetic
        assertEquals(90.18, distanceMiles, 0.1)
    }
}
