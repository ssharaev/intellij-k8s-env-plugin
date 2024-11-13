package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.PluginSettings;

import java.util.Map;

public interface EnvProvider {

  Map<String, String> getEnv(PluginSettings pluginSettings);

}
