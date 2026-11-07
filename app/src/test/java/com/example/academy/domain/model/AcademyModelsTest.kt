package com.example.academy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AcademyModelsTest {
    @Test
    fun defaultProfileUsesZahraAndAfghanDari() {
        val profile = UserProfile()
        assertEquals("زهرا", profile.displayName)
        assertEquals(AcademyLocale.DARI_AF, profile.locale)
    }

    @Test
    fun hintSpendsExactlySixCoins() {
        val wallet = Wallet(xp = 100, coins = 12)
        val afterHint = wallet.spendHint()
        assertEquals(6L, afterHint?.coins)
    }

    @Test
    fun hintCannotBePurchasedWithoutEnoughCoins() {
        assertNull(Wallet(coins = 5).spendHint())
    }

    @Test
    fun profileNameChangeDoesNotResetProgressWallet() {
        val profile = UserProfile(displayName = "زهرا")
        val renamed = profile.withDisplayName("مریم")
        val wallet = Wallet(xp = 750, coins = 22, level = 2)
        assertEquals("مریم", renamed.displayName)
        assertEquals(750L, wallet.xp)
        assertEquals(22L, wallet.coins)
    }
}
