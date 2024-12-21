# Kubernetes Run Configuration Env

![Build](https://github.com/ssharaev/intelij-k8s-env-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

This plugin allows you to add environment variables to your run configuration from a k8s cluster. It uses the current context from `$KUBECONFIG` or `$HOME/.kube/config` file.

It supports several modes:
- Configmap and secrets - fetch variables from multiple configmap and/or secrets
- Pod environment - fetch all environment variables from the selected pod
- Pod Vault environment - fetch all Vault environment variables from the selected pod.

Also, you can replace your variable values using regexp.

![Screenshot](/doc/img/screenshot.png)

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Kubernetes Run Configuration Env"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/ssharaev/intelij-k8s-env-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

# License

Copyright (c) 2024 Sviatoslav Sharaev. See the [LICENSE](./LICENSE) file for license rights and limitations (MIT).

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
