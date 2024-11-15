package com.ssharaev.k8s.env.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginSettings {

    @Builder.Default
    private EnvMode envMode = EnvMode.CONFIGMAP_AND_SECRET;
    @Builder.Default
    private String namespace = "default";
    @Builder.Default
    private List<String> configmapNames = List.of();
    @Builder.Default
    private List<String> secretNames = List.of();
    private String podName;

}
