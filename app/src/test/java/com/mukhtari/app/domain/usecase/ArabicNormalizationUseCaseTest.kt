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
    fun testRemoveDiacritics() {
        val input = "مُحَمَّدٌ"
        val expected = "محمد"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testRemoveTatweel() {
        val input = "مــــحــــمــــد"
        val expected = "محمد"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testUnifyAlef() {
        val input = "أحمد إبراهيم آمال ٱختبار"
        val expected = "احمد ابراهيم امال اختبار"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testUnifyTaaMarbouta() {
        val input = "فاطمة"
        val expected = "فاطمه"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testUnifyYaaVariants() {
        val input = "علي سلمى"
        val expected = "علي سلمي"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testRemoveExtraSpaces() {
        val input = "  حسن   محمد    علي  "
        val expected = "حسن محمد علي"
        assertEquals(expected, useCase(input))
    }

    @Test
    fun testCombinedNormalization() {
        val input = "  أَحْمَـــــدٌ   إِبْرَاهِيــــمَ   "
        val expected = "احمد ابراهيم"
        assertEquals(expected, useCase(input))
    }
}
