package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.task.ExecuteRunConfigurationTask;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import com.ssharaev.k8s.env.plugin.services.providers.CombinedEnvProvider;
import com.ssharaev.k8s.env.plugin.services.providers.EnvProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.execution.build.GradleExecutionEnvironmentProvider;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;

import java.util.Map;
import java.util.Optional;

public class GradleK8sEnvExecutionEnvironmentProvider implements GradleExecutionEnvironmentProvider {

    private final EnvProvider envProvider;

    public GradleK8sEnvExecutionEnvironmentProvider() {
        envProvider = ApplicationManager.getApplication().getService(CombinedEnvProvider.class);
    }

    @Override
    public boolean isApplicable(@NotNull ExecuteRunConfigurationTask task) {
        return task.getRunProfile() instanceof ApplicationConfiguration;
    }

    @Override
    public @Nullable ExecutionEnvironment createExecutionEnvironment(@NotNull Project project, @NotNull ExecuteRunConfigurationTask task, @Nullable Executor executor) {
        final ExecutionEnvironment environment = delegateProvider(task)
                .map(provider -> provider.createExecutionEnvironment(project, task, executor))
                .orElse(null);

        if (environment != null && environment.getRunProfile() instanceof GradleRunConfiguration targetConfig) {
            final ApplicationConfiguration sourceConfig = (ApplicationConfiguration) task.getRunProfile();
            Map<String, String> env = envProvider.getEnv(PluginSettingsProvider.getPluginSetting(sourceConfig));
            targetConfig.getSettings().getEnv().putAll(env);
        }

        return environment;
    }

    private Optional<GradleExecutionEnvironmentProvider> delegateProvider(
            final ExecuteRunConfigurationTask executeRunConfigurationTask
    ) {
        return GradleExecutionEnvironmentProvider.EP_NAME.getExtensionList().stream()
                .filter(provider -> provider != this && provider.isApplicable(executeRunConfigurationTask))
                .findFirst();
    }
}
