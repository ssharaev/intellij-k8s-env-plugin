package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.application.ApplicationManager;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SecretEnvProvider implements EnvProvider {

  @Override
  public boolean isApplicable(PluginSettings pluginSettings) {
    return EnvMode.CONFIGMAP_AND_SECRET == pluginSettings.getEnvMode();
  }

  @Override
  public Map<String, String> getEnv(PluginSettings pluginSettings) {
    return ApplicationManager.getApplication().getService(KubernetesService.class).getEnvFromSecrets(pluginSettings.getNamespace(), pluginSettings.getSecretNames());
  }
}
