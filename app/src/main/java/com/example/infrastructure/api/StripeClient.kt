package com.example.infrastructure.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class StripeTokenResult {
    data class Success(val tokenId: String) : StripeTokenResult()
    data class Failure(val errorMessage: String) : StripeTokenResult()
}

sealed class StripePaymentResult {
    data class Success(val paymentIntentId: String, val status: String, val isSandbox: Boolean) : StripePaymentResult()
    data class Failure(val errorMessage: String) : StripePaymentResult()
}

object StripeClient {
    private const val TAG = "StripeClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getPublishableKey(): String {
        return try {
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun getSecretKey(): String {
        return try {
            BuildConfig.STRIPE_SECRET_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun isSandboxMode(): Boolean {
        val pubKey = getPublishableKey()
        val secKey = getSecretKey()
        return pubKey.isEmpty() || pubKey == "pk_test_placeholder" || secKey.isEmpty() || secKey == "sk_test_placeholder"
    }

    /**
     * Creates a Stripe single-use Card Token using Card details securely.
     */
    suspend fun createCardToken(
        cardNumber: String,
        expMonth: String,
        expYear: String,
        cvc: String,
        name: String
    ): StripeTokenResult = withContext(Dispatchers.IO) {
        val pubKey = getPublishableKey()
        if (isSandboxMode()) {
            Log.d(TAG, "Stripe in Sandbox Mode. Simulating token generation.")
            return@withContext StripeTokenResult.Success("tok_sandbox_${System.currentTimeMillis()}")
        }

        try {
            val formBody = FormBody.Builder()
                .add("card[number]", cardNumber.replace(" ", ""))
                .add("card[exp_month]", expMonth)
                .add("card[exp_year]", expYear)
                .add("card[cvc]", cvc)
                .add("card[name]", name)
                .build()

            val request = Request.Builder()
                .url("https://api.stripe.com/v1/tokens")
                .header("Authorization", "Bearer $pubKey")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val tokenId = json.getString("id")
                    StripeTokenResult.Success(tokenId)
                } else {
                    val errorMsg = parseStripeError(bodyStr)
                    StripeTokenResult.Failure(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token creation failed", e)
            StripeTokenResult.Failure(e.message ?: "Network error during Stripe tokenization.")
        }
    }

    /**
     * Executes the charge by creating a Stripe PaymentIntent with the generated Token.
     */
    suspend fun processPayment(
        amountPkr: Double,
        token: String,
        description: String
    ): StripePaymentResult = withContext(Dispatchers.IO) {
        val secKey = getSecretKey()
        if (isSandboxMode()) {
            Log.d(TAG, "Stripe in Sandbox Mode. Simulating charge of PKR $amountPkr.")
            return@withContext StripePaymentResult.Success(
                paymentIntentId = "pi_sandbox_${System.currentTimeMillis()}",
                status = "succeeded",
                isSandbox = true
            )
        }

        try {
            // Stripe expects amount in cents. For PKR, we multiply by 100 to convert to cents/subunits.
            val amountCents = (amountPkr * 100).toLong()

            val formBody = FormBody.Builder()
                .add("amount", amountCents.toString())
                .add("currency", "pkr")
                .add("payment_method_data[type]", "card")
                .add("payment_method_data[card][token]", token)
                .add("description", description)
                .add("confirm", "true")
                .build()

            val request = Request.Builder()
                .url("https://api.stripe.com/v1/payment_intents")
                .header("Authorization", "Bearer $secKey")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val intentId = json.getString("id")
                    val status = json.getString("status")
                    StripePaymentResult.Success(intentId, status, false)
                } else {
                    val errorMsg = parseStripeError(bodyStr)
                    StripePaymentResult.Failure(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Payment intent creation failed", e)
            StripePaymentResult.Failure(e.message ?: "Network error during Stripe charge processing.")
        }
    }

    private fun parseStripeError(jsonStr: String): String {
        return try {
            val json = JSONObject(jsonStr)
            val errorObj = json.getJSONObject("error")
            errorObj.optString("message", "Stripe API error.")
        } catch (e: Exception) {
            "Payment failed. Please verify your card details and try again."
        }
    }

    /**
     * Luhn Algorithm validation for credit cards.
     */
    fun validateCardNumber(number: String): Boolean {
        val cleanNumber = number.replace(" ", "")
        if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = Character.getNumericValue(cleanNumber[i])
            if (n < 0 || n > 9) return false
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = n % 10 + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }
}
