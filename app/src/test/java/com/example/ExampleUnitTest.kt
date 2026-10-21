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

  @Test
  fun testRevenueCatManagerApiKey() {
    val apiKey = RevenueCatManager.getApiKeyFromBuildConfig()
    assertNotNull(apiKey)
    assertTrue(apiKey.isNotEmpty())
    assertTrue(apiKey.startsWith("test_"))
  }

  @Test
  fun testRevenueCatManagerInstance() {
    val manager = RevenueCatManager.getInstance()
    assertNotNull(manager)
    assertNull(manager.getCurrentOffering())
  }

  @Test
  fun testAiAppRecommendationFromProfile() {
    val service = com.example.data.service.GeminiOptimizerService()
    val testBio = com.example.data.model.UserBiography(
        birthYear = 1996,
        jobTitle = "Software Engineer",
        hobbies = "Bouldering & Cocktails",
        favoriteFood = "Tacos",
        datingGoals = "Long-term relationship",
        datingVibe = "Flirtatious, and funny"
    )
    val rec = service.recommendDatingSite(
        biography = testBio,
        gender = com.example.data.model.UserGender.MAN,
        interestedIn = com.example.data.model.InterestedIn.WOMEN
    )
    assertNotNull(rec)
    assertTrue(rec.appName.isNotBlank())
    assertTrue(rec.matchScore.contains("%"))
    assertTrue(rec.playStorePackage.isNotBlank())
    assertTrue(rec.whyItMatchesProfile.isNotEmpty())
    // Verify it gathered the profile's vibe
    val mentionsVibeOrCareer = rec.whyItMatchesProfile.any { it.contains("Flirtatious") || it.contains("banter") || it.contains("Software Engineer") || it.contains("career") }
    assertTrue(mentionsVibeOrCareer)
  }

  @Test
  fun testPhotoRetouchEditsInRepository() {
    val repo = ProfileRepository()
    repo.addPhotos(listOf("content://local/photo1.jpg", "content://local/photo2.jpg"))
    assertEquals(2, repo.userPhotos.value.size)

    val firstPhotoId = repo.userPhotos.value.first().id
    assertFalse(repo.userPhotos.value.first().edits.isEdited)

    val edits = com.example.data.model.PhotoEdits(
        skinSoftening = 0.75f,
        jawWidth = 0.60f,
        skinWarmth = 0.40f,
        eyeClarity = 0.50f,
        jawContour = 0.45f
    )
    repo.updatePhotoEdits(firstPhotoId, edits)

    val updatedFirst = repo.userPhotos.value.first()
    assertTrue(updatedFirst.edits.isEdited)
    assertEquals(0.75f, updatedFirst.edits.skinSoftening, 0.001f)
    assertEquals(0.60f, updatedFirst.edits.jawWidth, 0.001f)
    val badges = updatedFirst.edits.summaryBadges()
    assertTrue(badges.any { it.contains("Skin Softened") })
    assertTrue(badges.any { it.contains("Jaw Widened") })
  }
}

