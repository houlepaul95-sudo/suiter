package com.example

import com.example.data.model.SubscriptionTier
import com.example.data.repository.ProfileRepository
import com.example.data.service.RevenueCatService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRevenueCatMaskedApiKey() {
    val masked = RevenueCatService.getMaskedApiKey()
    assertTrue(masked.contains("...") || masked == "****")
  }

  @Test
  fun testSubscriptionTiers() {
    assertEquals("free", SubscriptionTier.FREE.id)
    assertEquals("weekly", SubscriptionTier.WEEKLY.id)
    assertEquals("monthly", SubscriptionTier.MONTHLY.id)
    assertEquals("annual", SubscriptionTier.ANNUAL.id)
  }

  @Test
  fun testRepositoryTierTransition() {
    val repo = ProfileRepository()
    assertEquals(SubscriptionTier.FREE, repo.subscriptionTier.value)
    assertFalse(repo.isPremiumUser.value)

    repo.setSubscriptionTier(SubscriptionTier.MONTHLY)
    assertEquals(SubscriptionTier.MONTHLY, repo.subscriptionTier.value)
    assertTrue(repo.isPremiumUser.value)

    repo.deleteEntireAccountAndData()
    assertEquals(SubscriptionTier.FREE, repo.subscriptionTier.value)
    assertFalse(repo.isPremiumUser.value)
  }
}
