package com.kasperovich.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LangManager class.
 */
public class LangManagerTest {

    // Store the original locale to restore it after tests
    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        // Save the original locale
        originalLocale = LangManager.getLocale();
    }

    @AfterEach
    void tearDown() {
        // Restore the original locale after each test
        LangManager.setLocale(originalLocale);
    }

    @Test
    void testDefaultLocaleIsEnglish() {
        // The default locale should be English
        assertEquals(Locale.ENGLISH, LangManager.getLocale());
    }

    @Test
    void testSetLocale() {
        // Arrange
        Locale russianLocale = new Locale("ru", "RU");
        
        // Act
        LangManager.setLocale(russianLocale);
        
        // Assert
        assertEquals(russianLocale, LangManager.getLocale());
    }

    @Test
    void testGetBundle() {
        // Arrange
        ResourceBundle bundle = LangManager.getBundle();
        
        // Assert
        assertNotNull(bundle);
        // Check that the bundle contains some expected keys
        assertTrue(bundle.containsKey("login.title"));
    }

    @Test
    void testBundleChangesWithLocale() {
        // Arrange
        Locale russianLocale = new Locale("ru", "RU");
        ResourceBundle englishBundle = LangManager.getBundle();
        String englishTitle = englishBundle.getString("login.title");
        
        // Act
        LangManager.setLocale(russianLocale);
        ResourceBundle russianBundle = LangManager.getBundle();
        String russianTitle = russianBundle.getString("login.title");
        
        // Assert
        assertNotNull(russianBundle);
        assertNotEquals(englishTitle, russianTitle);
    }

    @Test
    void testLocalizationKeys() {
        // Get the bundle
        ResourceBundle bundle = LangManager.getBundle();
        
        // Test some important keys exist
        String[] importantKeys = {
            "login.title",
            "login.username",
            "login.password",
            "login.button",
            "error.title",
            "info.title"
        };
        
        for (String key : importantKeys) {
            assertTrue(bundle.containsKey(key), "Bundle should contain key: " + key);
            assertNotNull(bundle.getString(key), "Value for key should not be null: " + key);
            assertFalse(bundle.getString(key).isEmpty(), "Value for key should not be empty: " + key);
        }
    }
}
