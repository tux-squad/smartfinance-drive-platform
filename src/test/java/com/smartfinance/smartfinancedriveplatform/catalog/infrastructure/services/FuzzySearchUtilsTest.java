package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuzzySearchUtilsTest {

    @Test
    void testLevenshteinDistanceExactMatch() {
        assertEquals(0, FuzzySearchUtils.calculateLevenshteinDistance("Toyota", "Toyota"));
        assertEquals(0, FuzzySearchUtils.calculateLevenshteinDistance("toyota", "TOYOTA"));
    }

    @Test
    void testLevenshteinDistanceTypos() {
        assertEquals(1, FuzzySearchUtils.calculateLevenshteinDistance("toyta", "toyota"));
        assertEquals(1, FuzzySearchUtils.calculateLevenshteinDistance("crolla", "corolla"));
        assertTrue(FuzzySearchUtils.calculateLevenshteinDistance("honda", "hyundai") >= 3);
    }

    @Test
    void testTrigramSimilarity() {
        double similarityExact = FuzzySearchUtils.calculateTrigramSimilarity("Toyota", "Toyota");
        assertEquals(1.0, similarityExact, 0.001);

        double similarityTypo = FuzzySearchUtils.calculateTrigramSimilarity("toyta", "toyota");
        assertTrue(similarityTypo > 0.5, "Similarity for typo should be > 0.5");
    }

    @Test
    void testIsFuzzyMatch() {
        assertTrue(FuzzySearchUtils.isFuzzyMatch("toyta", "Toyota"));
        assertTrue(FuzzySearchUtils.isFuzzyMatch("crolla", "Corolla"));
        assertTrue(FuzzySearchUtils.isFuzzyMatch("nissn", "Nissan"));
        assertFalse(FuzzySearchUtils.isFuzzyMatch("Ferrari", "Toyota"));
    }
}
