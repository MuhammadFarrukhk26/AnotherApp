package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
import com.example.infrastructure.api.StripeClient

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun stripeCardValidation_isCorrect() {
    // Standard Stripe test card is valid Luhn
    assertTrue(StripeClient.validateCardNumber("4242 4242 4242 4242"))
    assertTrue(StripeClient.validateCardNumber("4242424242424242"))
    
    // Invalid Luhn cards
    assertFalse(StripeClient.validateCardNumber("4242 4242 4242 4243"))
    assertFalse(StripeClient.validateCardNumber("123456789"))
    assertFalse(StripeClient.validateCardNumber(""))
  }
}
