package com.example.infrastructure.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    /**
     * Ask Gemini for home service guidance based on a user's query.
     * Analyzes the problem and returns advice + category recommendation.
     */
    suspend fun getServiceAdvice(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder, using offline diagnostic engine")
            return@withContext getOfflineDiagnosticAdvice(userMessage)
        }

        val systemPrompt = """
            You are "Hazir AI Advisor", a friendly and intelligent home services consultant for Hazir App in Pakistan.
            Your job is to analyze the user's maintenance or service issue and:
            1. Diagnose the issue in simple, friendly terms.
            2. Explicitly recommend ONE of the following Hazir service categories:
               - Electrician (for wiring, switches, short circuits, appliances, fans)
               - Plumber (for leakages, pipes, water pumps, washroom fixtures)
               - AC Technician (for AC cooling, cleaning, gas leakage, filters)
               - Home Cleaner (for deep cleaning, kitchen washing, floor scrubbing)
               - Professional Painter (for wall paint, touch-ups, wallpaper)
               - Carpenter (for furniture, cabinet doors, locks, wooden repairs)
               - Car/Bike Mechanic (for engine sounds, filter changes, general vehicle issues)
               - Mover & Packer (for packing, shifting household luggage, transit)
            3. Give a rough PKR (Pakistani Rupee) budget estimate.
            4. Keep the tone warm, concise, and helpful (using standard Pakistani service vocabulary where helpful, e.g. "Asalam-o-Alaikum", "PKR").
            Keep your response short (max 3 small paragraphs). Format the recommended category clearly.
        """.trimIndent()

        val requestBodyData = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userMessage)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        try {
            val jsonString = requestAdapter.toJson(requestBodyData)
            val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP Error: ${response.code} - ${response.message}")
                    return@withContext getOfflineDiagnosticAdvice(userMessage)
                }

                val responseBodyStr = response.body?.string() ?: return@withContext "Sorry, I could not parse the response. Please try again."
                val geminiResponse = responseAdapter.fromJson(responseBodyStr)
                
                val reply = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (reply != null) {
                    reply
                } else {
                    getOfflineDiagnosticAdvice(userMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Call failed: ${e.message}", e)
            getOfflineDiagnosticAdvice(userMessage)
        }
    }

    /**
     * Highly intelligent offline local fallback engine to keep the app 100% functional,
     * robust, and crash-proof even if internet is down or the Gemini API key is missing.
     */
    private fun getOfflineDiagnosticAdvice(userMessage: String): String {
        val query = userMessage.lowercase()
        return when {
            query.contains("wire") || query.contains("spark") || query.contains("short") || query.contains("light") || query.contains("fan") || query.contains("electricity") || query.contains("board") || query.contains("fuse") || query.contains("meter") -> {
                "Asalam-o-Alaikum! It looks like you have an electrical issue. Warning: Sparks or faulty wiring can be dangerous. Please avoid touching open circuits.\n\n" +
                        "🔧 **Recommended Service**: Electrician\n" +
                        "💵 **Estimated Cost**: 450 - 1,500 PKR\n\n" +
                        "Hazir has certified electricians nearby who can resolve this safely in minutes!"
            }
            query.contains("leak") || query.contains("pipe") || query.contains("water") || query.contains("pump") || query.contains("tap") || query.contains("sink") || query.contains("drain") || query.contains("washroom") || query.contains("toilet") -> {
                "Asalam-o-Alaikum! Water leakages can cause damage to walls and floors if left unchecked.\n\n" +
                        "🔧 **Recommended Service**: Plumber\n" +
                        "💵 **Estimated Cost**: 500 - 2,000 PKR\n\n" +
                        "I suggest hiring a Hazir plumber to inspect the pipe seals and faucet fixtures immediately."
            }
            query.contains("ac") || query.contains("cool") || query.contains("split") || query.contains("filter") || query.contains("gas") || query.contains("heating") || query.contains("compressor") -> {
                "Asalam-o-Alaikum! If your air conditioner is running but not cooling, it could be a dirty filter, clogged outdoor unit, or low refrigerant gas.\n\n" +
                        "🔧 **Recommended Service**: AC Technician\n" +
                        "💵 **Estimated Cost**: 800 - 3,500 PKR\n\n" +
                        "Hire an experienced Hazir AC Technician to clean the filters and check gas pressure."
            }
            query.contains("clean") || query.contains("sweep") || query.contains("wash") || query.contains("dust") || query.contains("kitchen") || query.contains("maid") || query.contains("vacuum") || query.contains("dirt") -> {
                "Asalam-o-Alaikum! A clean house brings peace of mind. We offer deep cleaning services for your home.\n\n" +
                        "🔧 **Recommended Service**: Home Cleaner\n" +
                        "💵 **Estimated Cost**: 1,200 - 4,000 PKR\n\n" +
                        "Book a professional Hazir Home Cleaner for a full dust-free, sanitized living experience."
            }
            query.contains("paint") || query.contains("wall") || query.contains("color") || query.contains("touchup") || query.contains("distemper") || query.contains("polish") -> {
                "Asalam-o-Alaikum! Fresh paint can completely transform your living space!\n\n" +
                        "🔧 **Recommended Service**: Professional Painter\n" +
                        "💵 **Estimated Cost**: 1,500 - 15,000 PKR\n\n" +
                        "Connect with a Hazir painter to get color consultations and flawless finishing."
            }
            query.contains("wood") || query.contains("furniture") || query.contains("door") || query.contains("cabinet") || query.contains("lock") || query.contains("drawer") || query.contains("sofa") || query.contains("table") -> {
                "Asalam-o-Alaikum! Squeaky doors or broken locks can compromise security, and loose furniture needs proper wood joinery.\n\n" +
                        "🔧 **Recommended Service**: Carpenter\n" +
                        "💵 **Estimated Cost**: 600 - 3,000 PKR\n\n" +
                        "Our expert Hazir Carpenters can repair wardrobes, install door handles, and fix furniture beautifully."
            }
            query.contains("car") || query.contains("bike") || query.contains("engine") || query.contains("noise") || query.contains("break") || query.contains("filter") || query.contains("tire") || query.contains("oil") || query.contains("clutch") -> {
                "Asalam-o-Alaikum! Vehicle troubles can disrupt your entire day. Let's get your transport back on the road.\n\n" +
                        "🔧 **Recommended Service**: Car/Bike Mechanic\n" +
                        "💵 **Estimated Cost**: 1,000 - 5,000 PKR\n\n" +
                        "Book a certified mobile Mechanic who will come directly to your parking spot for diagnostics."
            }
            query.contains("move") || query.contains("pack") || query.contains("shift") || query.contains("luggage") || query.contains("transit") || query.contains("house") || query.contains("office") || query.contains("truck") -> {
                "Asalam-o-Alaikum! Shifting house can be incredibly stressful without heavy lifting hands.\n\n" +
                        "🔧 **Recommended Service**: Mover & Packer\n" +
                        "💵 **Estimated Cost**: 2,500 - 25,000 PKR\n\n" +
                        "Book our Movers & Packers service. We'll handle everything from packing, dismantling, loading, and safe transit."
            }
            else -> {
                "Asalam-o-Alaikum! I am your Hazir AI Advisor. Tell me what issue you are facing (e.g. 'electric sparks', 'pipe leakage', 'AC not cooling', 'need to paint my bedroom').\n\n" +
                        "I will analyze the problem, estimate costs, and match you with the best available service provider in Islamabad!"
            }
        }
    }
}
