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
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.execution.build.MavenExecutionEnvironmentProvider;

import java.util.Map;
import java.util.Optional;


public class MavenK8sEnvExecutionEnvironmentProvider implements MavenExecutionEnvironmentProvider {

    @Override
    public boolean isApplicable(@NotNull ExecuteRunConfigurationTask task) {
        return task.getRunProfile() instanceof ApplicationConfiguration;
    }

    @Override
    public @Nullable ExecutionEnvironment createExecutionEnvironment(@NotNull Project project, @NotNull ExecuteRunConfigurationTask task, @Nullable Executor executor) {
        final ExecutionEnvironment environment = delegateProvider(task)
                .map(provider -> provider.createExecutionEnvironment(project, task, executor))
                .orElse(null);

        if (environment != null && environment.getRunProfile() instanceof MavenRunConfiguration targetConfig) {
            final ApplicationConfiguration sourceConfig = (ApplicationConfiguration) task.getRunProfile();
            EnvProvider envProvider = ApplicationManager.getApplication().getService(CombinedEnvProvider.class);
            Map<String, String> env = envProvider.getEnv(PluginSettingsProvider.getPluginSetting(sourceConfig));
            Optional.ofNullable(targetConfig.getRunnerSettings())
                    .map(MavenRunnerSettings::getEnvironmentProperties)
                    .ifPresent(properties -> properties.putAll(env));
        }
        return environment;
    }

    private Optional<MavenExecutionEnvironmentProvider> delegateProvider(
            final ExecuteRunConfigurationTask executeRunConfigurationTask
    ) {
        return MavenExecutionEnvironmentProvider.EP_NAME.getExtensionList().stream()
                .filter(provider -> provider != this && provider.isApplicable(executeRunConfigurationTask))
                .findFirst();
    }
}
