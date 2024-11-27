package com.ssharaev.k8s.env.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginSettings {

    @NotNull
    @Builder.Default
    private EnvMode envMode = EnvMode.CONFIGMAP_AND_SECRET;
    @Builder.Default
    private String namespace = "default";
    @Builder.Default
    private List<String> configmapNames = List.of();
    @Builder.Default
    private List<String> secretNames = List.of();
    @Nullable
    private String podName;

    @Builder.Default
    private List<ReplacementEntity> replacementEntities = List.of();

}
