package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkeyProvincesTest {

    @Test
    fun `katalog 81 ili tam ve turkce karakter duyarsiz icerir`() {
        assertEquals(81, TurkeyProvinces.names.size)
        assertTrue(TurkeyProvinces.contains("İstanbul"))
        assertTrue(TurkeyProvinces.contains("Hakkari"))
        assertFalse(TurkeyProvinces.contains("İstanbulboğazı"))
    }
}
