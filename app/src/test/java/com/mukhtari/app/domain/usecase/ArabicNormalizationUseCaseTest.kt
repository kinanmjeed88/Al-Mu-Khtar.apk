package com.mukhtari.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArabicNormalizationUseCaseTest {

    private lateinit var useCase: ArabicNormalizationUseCase

    @Before
    fun setup() {
        useCase = ArabicNormalizationUseCase()
    }

    @Test
    fun testDiacriticsRemoval() {
        assertEquals("محمد", useCase("مُحَمَّد"))
    }

    @Test
    fun testTatweelRemoval() {
        assertEquals("احمد", useCase("احمـــــد"))
    }

    @Test
    fun testAlefUnification() {
        assertEquals("احمد", useCase("أحمد"))
        assertEquals("ايمان", useCase("إيمان"))
        assertEquals("ادم", useCase("آدم"))
    }

    @Test
    fun testTaaMarboutaUnification() {
        assertEquals("فاطمه", useCase("فاطمة"))
    }

    @Test
    fun testYaaUnification() {
        assertEquals("علي", useCase("على"))
        assertEquals("مصطفي", useCase("مصطفى"))
    }

    @Test
    fun testSpaceUnification() {
        assertEquals("علي احمد سعيد", useCase("علي   احمد    سعيد"))
    }

    @Test
    fun testCombinedNormalization() {
        assertEquals("مصطفي احمد", useCase("  مُصْطَفَى   أَحْمَد  "))
    }
}
