package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.task.ExecuteRunConfigurationTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.build.MavenExecutionEnvironmentProvider;

public class MavenK8sEnvExecutionEnvironmentProvider implements MavenExecutionEnvironmentProvider {
    @Override
    public boolean isApplicable(@NotNull ExecuteRunConfigurationTask task) {
        return task.getRunProfile() instanceof ApplicationConfiguration;
    }

    @Override
    public @Nullable ExecutionEnvironment createExecutionEnvironment(@NotNull Project project, @NotNull ExecuteRunConfigurationTask task, @Nullable Executor executor) {
        return null;
    }
}
