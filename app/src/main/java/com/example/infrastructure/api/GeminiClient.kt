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
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String // base64 string
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseModalities: List<String>? = null,
    val speechConfig: GeminiSpeechConfig? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSpeechConfig(
    val voiceConfig: GeminiVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiVoiceConfig(
    val prebuiltVoiceConfig: GeminiPrebuiltVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPrebuiltVoiceConfig(
    val voiceName: String // e.g. "Puck", "Charon", "Kore", "Fenrir", "Aoede"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

data class VoiceResponseResult(
    val text: String,
    val audioBase64: String? // Null if offline or failed, in which case we fallback to local TTS
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val DEFAULT_MODEL = "gemini-3.5-flash"
    private const val LIVE_MODEL = "gemini-3.1-flash-live-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$DEFAULT_MODEL:generateContent"
    private const val LIVE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$LIVE_MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
     * Sends optional audio input and text history to gemini-3.1-flash-live-preview (Live API)
     * and requests back BOTH text and voice audio.
     */
    suspend fun getVoiceResponse(
        audioBase64: String?,
        textPrompt: String?,
        history: List<GeminiContent> = emptyList()
    ): VoiceResponseResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty, using offline voice response")
            return@withContext getOfflineVoiceResponse(textPrompt ?: "hello")
        }

        // We build the list of contents for the request
        val requestContents = mutableListOf<GeminiContent>()
        
        // Add history first
        requestContents.addAll(history)

        // Build the current user turn parts
        val parts = mutableListOf<GeminiPart>()
        if (audioBase64 != null) {
            parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "audio/mp3", data = audioBase64)))
        }
        if (textPrompt != null) {
            parts.add(GeminiPart(text = textPrompt))
        }

        if (parts.isNotEmpty()) {
            requestContents.add(GeminiContent(role = "user", parts = parts))
        }

        val systemPrompt = """
            You are "Hazir Live Audio Companion", a real-time smart home services assistant for the Hazir App in Pakistan.
            The user is speaking or typing to you in real-time.
            Keep your spoken response extremely short, conversational, and friendly (max 2 sentences), as it will be spoken back to the user.
            You can guide them on electrician, plumbing, painting, cleaning, or carpenter tasks. Encourage them to use Hazir.
            Always maintain a helpful, warm Pakistani hospitality tone. You can use standard Urdu-English phrases like "Asalam-o-Alaikum", "g bilkul", "ji", "haji", etc.
        """.trimIndent()

        val requestBodyData = GeminiRequest(
            contents = requestContents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                responseModalities = listOf("TEXT", "AUDIO"),
                speechConfig = GeminiSpeechConfig(
                    voiceConfig = GeminiVoiceConfig(
                        prebuiltVoiceConfig = GeminiPrebuiltVoiceConfig(voiceName = "Puck")
                    )
                ),
                temperature = 0.7f
            )
        )

        try {
            val jsonString = requestAdapter.toJson(requestBodyData)
            val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

            val liveUrl = "$LIVE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(liveUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string()
                    Log.e(TAG, "Live Voice API HTTP Error: ${response.code} - ${response.message} - $errBody")
                    return@withContext getOfflineVoiceResponse(textPrompt ?: "hello")
                }

                val responseBodyStr = response.body?.string() ?: return@withContext VoiceResponseResult("I couldn't hear you clearly. Could you repeat?", null)
                val geminiResponse = responseAdapter.fromJson(responseBodyStr)
                
                val partsResponse = geminiResponse?.candidates?.firstOrNull()?.content?.parts
                var outText = ""
                var outAudioBase64: String? = null

                partsResponse?.forEach { part ->
                    if (part.text != null) {
                        outText += part.text
                    }
                    if (part.inlineData != null && part.inlineData.mimeType.contains("audio")) {
                        outAudioBase64 = part.inlineData.data
                    }
                }

                if (outText.isEmpty()) {
                    outText = "I'm ready to help you with any home repair diagnostics."
                }

                VoiceResponseResult(outText, outAudioBase64)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Live Voice API call failed: ${e.message}", e)
            getOfflineVoiceResponse(textPrompt ?: "hello")
        }
    }

    private fun getOfflineVoiceResponse(queryText: String): VoiceResponseResult {
        val query = queryText.lowercase()
        val text = when {
            query.contains("hello") || query.contains("hi") || query.contains("aao") || query.contains("asalam") -> {
                "Asalam-o-Alaikum! Welcome to Hazir Live Assistant. How can I help you with your home services today?"
            }
            query.contains("wire") || query.contains("electric") || query.contains("spark") || query.contains("fan") || query.contains("light") -> {
                "It sounds like an electrician issue. For safety, please do not touch any bare wires. Would you like me to book a professional electrician for you?"
            }
            query.contains("leak") || query.contains("water") || query.contains("pipe") || query.contains("tap") || query.contains("toilet") || query.contains("sink") -> {
                "Ah, water leaks can cause damage. We can send an expert plumber to fix your taps and pipes. Let me know if you want to book one."
            }
            query.contains("ac") || query.contains("cool") || query.contains("heat") -> {
                "Your air conditioner might need cleaning or a gas refill. Our certified AC technicians are ready to assist. Should I arrange one?"
            }
            query.contains("clean") || query.contains("wash") || query.contains("dust") -> {
                "A clean home is a happy home! We have top-rated deep cleaning services. I can book a deep cleaning slot for you right away."
            }
            query.contains("paint") || query.contains("wall") || query.contains("color") -> {
                "Wall painting can refresh your space! Our professional painters can do beautiful touch-ups or fill wall paint. Let me know!"
            }
            else -> {
                "I understand! Hazir has Islamabad's best technicians for plumbing, electrical, cleaning, and more. What service can we assist with?"
            }
        }
        return VoiceResponseResult(text, null)
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
