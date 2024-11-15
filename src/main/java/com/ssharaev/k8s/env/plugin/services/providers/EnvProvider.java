package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.model.PluginSettings;

import java.util.Map;

public interface EnvProvider {

  boolean isApplicable(PluginSettings pluginSettings);

  Map<String, String> getEnv(PluginSettings pluginSettings);

}
