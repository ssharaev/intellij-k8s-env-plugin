package com.ssharaev.k8s.env.plugin.services;

import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public final class ReplacementService {

    public Map<String, String> proceedReplacement(@NotNull Map<String, String> env,
                                                  @NotNull List<ReplacementEntity> replacementEntities) {
        List<CompiledReplacementEntity> entities = replacementEntities.stream()
                .map(entry -> new CompiledReplacementEntity(entry.getReplacement(), Pattern.compile(entry.getRegexp())))
                .toList();
        return env.entrySet().stream()
                .map(entry -> replace(entry, entities))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, String> replace(Map.Entry<String, String> entry,
                                              List<CompiledReplacementEntity> entities) {
        String resultValue = entry.getValue();
        for(CompiledReplacementEntity entity : entities) {
            if (entity.pattern().matcher(entry.getValue()).find()) {
                resultValue = entity.pattern().matcher(entry.getValue()).replaceFirst(entity.replacement());
            }
        }
        return Map.entry(entry.getKey(), resultValue);
    }

    record CompiledReplacementEntity(String replacement, Pattern pattern) { };
}
