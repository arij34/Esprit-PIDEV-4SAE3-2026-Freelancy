package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.freelancy.skillmanagement.service.SimilarityService;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityServiceTest {

    private SimilarityService similarityService;

    @BeforeEach
    void setUp() {
        similarityService = new SimilarityService();
    }

    @Test
    void testNormalize_ShouldReturnLowerCase() {
        String result = similarityService.normalize("JavaScript");
        assertEquals("javascript", result);
    }

    @Test
    void testNormalize_ShouldRemoveSpecialChars() {
        String result = similarityService.normalize("C++");
        assertEquals("c", result);
    }

    @Test
    void testCalculateSimilarity_ExactMatch_ShouldReturn1() {
        double result = similarityService.calculateSimilarity("java", "java");
        assertEquals(1.0, result);
    }

    @Test
    void testCalculateSimilarity_ContainsInput_ShouldReturnHigh() {
        double result = similarityService.calculateSimilarity("java", "javascript");
        assertTrue(result >= 0.85);
    }

    @Test
    void testCalculateSimilarity_CompletelyDifferent_ShouldReturnLow() {
        double result = similarityService.calculateSimilarity("java", "xyz");
        assertTrue(result < 0.5);
    }

    @Test
    void testCalculateJaroWinkler_SimilarStrings_ShouldReturnHigh() {
        double result = similarityService.calculateJaroWinkler("python", "python");
        assertEquals(1.0, result, 0.01);
    }

    @Test
    void testCalculateJaroWinkler_DifferentStrings_ShouldReturnLow() {
        double result = similarityService.calculateJaroWinkler("java", "python");
        assertTrue(result < 0.8);
    }
}