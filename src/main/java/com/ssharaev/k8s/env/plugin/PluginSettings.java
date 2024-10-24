package com.ssharaev.k8s.env.plugin;

import lombok.Data;

import java.util.List;

@Data
public class PluginSettings {

    private final String namespace;
    private final List<String> configmapNames;

}
