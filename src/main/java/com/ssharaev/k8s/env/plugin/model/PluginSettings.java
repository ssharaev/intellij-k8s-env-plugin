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

    @Builder.Default
    private boolean enabled = true;

    @NotNull
    @Builder.Default
    private EnvMode envMode = EnvMode.CONFIGMAP_AND_SECRET;
    @Nullable
    private String namespace;
    @NotNull
    @Builder.Default
    private List<String> configmapNames = List.of();
    @NotNull
    @Builder.Default
    private List<String> secretNames = List.of();
    @Nullable
    private String podName;

    @NotNull
    @Builder.Default
    private List<ReplacementEntity> replacementEntities = List.of();

}
