package com.ssharaev.k8s.env.plugin.services;

import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class ReplacementServiceTest extends TestCase {

    private final ReplacementService replacementService = new ReplacementService();

    public void testReplacement() {
        Map<String, String> env = Map.of("Key", "value-dev");
        List<ReplacementEntity> replacementEntities = List.of(new ReplacementEntity("([a-z\\-]+)(-dev)", "$1-local"));
        Map<String, String> resultEnv = replacementService.proceedReplacement(env, replacementEntities);
        assertEquals("value-local", resultEnv.get("Key"));
    }

    public void testEmptyReplacement() {
        Map<String, String> env = Map.of("Key", "value-dev");
        List<ReplacementEntity> replacementEntities = List.of();
        Map<String, String> resultEnv = replacementService.proceedReplacement(env, replacementEntities);
        assertEquals("value-dev", resultEnv.get("Key"));
    }

    public void testEmptyEnvReplacement() {
        Map<String, String> env = Map.of();
        List<ReplacementEntity> replacementEntities = List.of(new ReplacementEntity("([a-z\\-]+)(-dev)", "$1-local"));
        Map<String, String> resultEnv = replacementService.proceedReplacement(env, replacementEntities);
        assertTrue(resultEnv.isEmpty());
    }


}