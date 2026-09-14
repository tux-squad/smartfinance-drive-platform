package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.services;

import java.util.HashSet;
import java.util.Set;

/**
 * Technical utility service for string distance (Levenshtein) and trigram similarity calculations.
 * Located in infrastructure layer as a technical utility algorithm.
 */
public class FuzzySearchUtils {

    private FuzzySearchUtils() {}

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * @param s1 First string.
     * @param s2 Second string.
     * @return The edit distance integer.
     */
    public static int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return (s1 == null && s2 == null) ? 0 : (s1 == null ? s2.length() : s1.length());
        }

        String str1 = s1.toLowerCase().trim();
        String str2 = s2.toLowerCase().trim();

        if (str1.equals(str2)) {
            return 0;
        }
        if (str1.isEmpty()) {
            return str2.length();
        }
        if (str2.isEmpty()) {
            return str1.length();
        }

        int[] costs = new int[str2.length() + 1];
        for (int j = 0; j <= str2.length(); j++) {
            costs[j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= str2.length(); j++) {
                int cj = Math.min(
                        1 + Math.min(costs[j], costs[j - 1]),
                        str1.charAt(i - 1) == str2.charAt(j - 1) ? nw : nw + 1
                );
                nw = costs[j];
                costs[j] = cj;
            }
        }

        return costs[str2.length()];
    }

    /**
     * Calculates Trigram similarity between two strings (0.0 to 1.0).
     *
     * @param s1 First string.
     * @param s2 Second string.
     * @return Similarity score between 0.0 (no match) and 1.0 (exact match).
     */
    public static double calculateTrigramSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            return 0.0;
        }

        String str1 = "  " + s1.toLowerCase().trim() + " ";
        String str2 = "  " + s2.toLowerCase().trim() + " ";

        Set<String> trigrams1 = getTrigrams(str1);
        Set<String> trigrams2 = getTrigrams(str2);

        if (trigrams1.isEmpty() && trigrams2.isEmpty()) {
            return 1.0;
        }
        if (trigrams1.isEmpty() || trigrams2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(trigrams1);
        intersection.retainAll(trigrams2);

        double totalSize = trigrams1.size() + trigrams2.size();
        return (2.0 * intersection.size()) / totalSize;
    }

    /**
     * Checks if target string matches query using Levenshtein distance or Trigram similarity.
     *
     * @param input  User search input (e.g. "toyta").
     * @param target Candidate string (e.g. "Toyota").
     * @return true if candidate is a fuzzy match.
     */
    public static boolean isFuzzyMatch(String input, String target) {
        if (input == null || target == null || input.isBlank() || target.isBlank()) {
            return false;
        }

        String cleanInput = input.toLowerCase().trim();
        String cleanTarget = target.toLowerCase().trim();

        if (cleanTarget.contains(cleanInput) || cleanInput.contains(cleanTarget)) {
            return true;
        }

        int distance = calculateLevenshteinDistance(cleanInput, cleanTarget);
        int maxAllowedDistance = Math.min(2, Math.max(1, cleanInput.length() / 3));

        if (distance <= maxAllowedDistance) {
            return true;
        }

        double similarity = calculateTrigramSimilarity(cleanInput, cleanTarget);
        return similarity >= 0.45;
    }

    private static Set<String> getTrigrams(String str) {
        Set<String> trigrams = new HashSet<>();
        for (int i = 0; i <= str.length() - 3; i++) {
            trigrams.add(str.substring(i, i + 3));
        }
        return trigrams;
    }
}
