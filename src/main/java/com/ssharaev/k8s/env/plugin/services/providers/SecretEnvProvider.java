package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.PluginSettings;

import java.util.Map;

public class SecretEnvProvider implements EnvProvider {

  @Override
  public Map<String, String> getEnv(PluginSettings pluginSettings) {
    return Map.of();
  }
}
