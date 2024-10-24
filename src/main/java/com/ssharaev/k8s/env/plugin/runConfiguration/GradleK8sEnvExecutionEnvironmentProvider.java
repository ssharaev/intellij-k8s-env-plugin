package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.task.ExecuteRunConfigurationTask;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import com.ssharaev.k8s.env.plugin.services.providers.ConfigMapEnvProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.execution.build.GradleExecutionEnvironmentProvider;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;

import java.util.Map;
import java.util.Optional;

public class GradleK8sEnvExecutionEnvironmentProvider implements GradleExecutionEnvironmentProvider {

    private final ConfigMapEnvProvider configMapEnvProvider = new ConfigMapEnvProvider();

    @Override
    public boolean isApplicable(@NotNull ExecuteRunConfigurationTask task) {
        return task.getRunProfile() instanceof ApplicationConfiguration;
    }

    @Override
    public @Nullable ExecutionEnvironment createExecutionEnvironment(@NotNull Project project, @NotNull ExecuteRunConfigurationTask task, @Nullable Executor executor) {
        final ExecutionEnvironment environment = delegateProvider(task)
                .map(provider -> provider.createExecutionEnvironment(project, task, executor))
                .orElse(null);

        if (environment != null && environment.getRunProfile() instanceof GradleRunConfiguration) {
            final ApplicationConfiguration sourceConfig = (ApplicationConfiguration) task.getRunProfile();
            final GradleRunConfiguration targetConfig = (GradleRunConfiguration) environment.getRunProfile();
            Map<String, String> env = configMapEnvProvider.getEnv(PluginSettingsProvider.getEnvFileSetting(sourceConfig));
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
