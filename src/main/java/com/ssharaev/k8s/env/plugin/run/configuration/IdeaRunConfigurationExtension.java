package com.ssharaev.k8s.env.plugin.run.configuration;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import com.ssharaev.k8s.env.plugin.services.RunConfigurationEditorService;
import com.ssharaev.k8s.env.plugin.services.providers.CombinedEnvProvider;
import com.ssharaev.k8s.env.plugin.services.providers.EnvProvider;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IdeaRunConfigurationExtension extends RunConfigurationExtension {

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "Kubernetes Run Configuration Env";
    }

    @Nullable
    @Override
    protected <P extends RunConfigurationBase<?>> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new RunConfigurationSettingsEditor<>();
    }

    @NotNull
    @Override
    protected String getSerializationId() {
        return ApplicationManager.getApplication().getService(RunConfigurationEditorService.class).getSerializationId();
    }

    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException {
        ApplicationManager.getApplication().getService(RunConfigurationEditorService.class).writeExternal(runConfiguration, element);
    }

    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException {
        ApplicationManager.getApplication().getService(RunConfigurationEditorService.class).readExternal(runConfiguration, element);
    }

    @Override
    protected void validateConfiguration(@NotNull RunConfigurationBase configuration, boolean isExecution) {
        RunConfigurationEditorService runConfigurationEditorService =
                ApplicationManager.getApplication().getService(RunConfigurationEditorService.class);
            runConfigurationEditorService.validateConfiguration(configuration, isExecution);
    }

    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(
            @NotNull final T configuration,
            @NotNull final JavaParameters params,
            final RunnerSettings runnerSettings
    ) {
        EnvProvider envProvider = ApplicationManager.getApplication().getService(CombinedEnvProvider.class);
        Map<String, String> env = envProvider.getEnv(PluginSettingsProvider.getPluginSetting(configuration));
        params.getEnv().putAll(env);
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }
}
