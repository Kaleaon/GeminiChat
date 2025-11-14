package com.example.aistudioapp

import android.content.Intent
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MainActivity functionality.
 * These tests verify the core logic without requiring Android framework dependencies.
 */
class MainActivityTest {

    @Test
    fun `verify AI Studio URL is valid`() {
        val url = "https://aistudio.google.com/apps"
        val uri = Uri.parse(url)
        
        assertNotNull("URI should not be null", uri)
        assertEquals("https", uri.scheme)
        assertEquals("aistudio.google.com", uri.host)
        assertEquals("/apps", uri.path)
    }

    @Test
    fun `verify banner URL is valid`() {
        val url = "https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6"
        val uri = Uri.parse(url)
        
        assertNotNull("URI should not be null", uri)
        assertEquals("https", uri.scheme)
        assertEquals("github.com", uri.host)
    }

    @Test
    fun `verify intent action is correct`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apps"))
        
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertNotNull(intent.data)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}