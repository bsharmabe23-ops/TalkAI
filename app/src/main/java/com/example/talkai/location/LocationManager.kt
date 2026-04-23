package com.example.talkai.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LocationManager(private val context: Context) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val client = OkHttpClient()

    fun getCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Location permission not granted")
            return
        }

        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, null
        )
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    onError("GPS not available. Please enable GPS and go outside.")
                }
            }
            .addOnFailureListener {
                onError("Location error: ${it.message}")
            }
    }

    fun getNearbyPlaces(
        lat: Double,
        lng: Double,
        placeType: String,
        onResult: (String) -> Unit
    ) {
        Thread {
            try {
                val amenity = when {
                    placeType.contains("hospital") ||
                            placeType.contains("doctor") -> "hospital"
                    placeType.contains("pharmacy") ||
                            placeType.contains("medicine") -> "pharmacy"
                    placeType.contains("restaurant") ||
                            placeType.contains("food") -> "restaurant"
                    placeType.contains("shop") ||
                            placeType.contains("store") -> "shop"
                    placeType.contains("bank") -> "bank"
                    placeType.contains("atm") -> "atm"
                    placeType.contains("police") -> "police"
                    placeType.contains("school") -> "school"
                    else -> placeType
                }

                val overpassQuery =
                    "[out:json][timeout:15];" +
                            "(node[\"amenity\"=\"$amenity\"]" +
                            "(around:2000,$lat,$lng);" +
                            "way[\"amenity\"=\"$amenity\"]" +
                            "(around:2000,$lat,$lng););" +
                            "out center 5;"

                val request = Request.Builder()
                    .url("https://overpass-api.de/api/interpreter")
                    .addHeader("User-Agent", "TalkAI-Assistant/1.0")
                    .post(
                        overpassQuery.toRequestBody(
                            "text/plain".toMediaType()
                        )
                    )
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val elements = json.optJSONArray("elements")

                    if (elements != null && elements.length() > 0) {
                        val sb = StringBuilder()
                        val count = minOf(elements.length(), 3)
                        sb.append("I found $count ${amenity}s near you. ")

                        for (i in 0 until count) {
                            val el = elements.getJSONObject(i)
                            val tags = el.optJSONObject("tags")
                            val name = tags?.optString(
                                "name", "Unnamed $amenity"
                            ) ?: "Unnamed $amenity"

                            val placeLat = when {
                                el.has("lat") -> el.getDouble("lat")
                                else -> el.optJSONObject("center")
                                    ?.getDouble("lat") ?: lat
                            }
                            val placeLng = when {
                                el.has("lon") -> el.getDouble("lon")
                                else -> el.optJSONObject("center")
                                    ?.getDouble("lon") ?: lng
                            }

                            val dist = calculateDistance(
                                lat, lng, placeLat, placeLng
                            )
                            val distText = if (dist < 1.0)
                                "${(dist * 1000).toInt()} meters"
                            else
                                "${"%.1f".format(dist)} kilometers"

                            sb.append("${i + 1}. $name, ")
                            sb.append("$distText away. ")

                            val street = tags?.optString("addr:street", "")
                            if (!street.isNullOrEmpty()) {
                                sb.append("On $street. ")
                            }
                        }
                        onResult(sb.toString())

                    } else {
                        onResult(
                            "No $amenity found within 2 kilometers. " +
                                    "Try enabling GPS or moving outdoors."
                        )
                    }
                } else {
                    onResult("Could not connect. Check your internet.")
                }

            } catch (e: Exception) {
                onResult("Search failed. Please try again. ${e.message}")
            }
        }.start()
    }

    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}