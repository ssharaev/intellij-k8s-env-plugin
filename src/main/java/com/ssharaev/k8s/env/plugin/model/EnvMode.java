package com.ssharaev.k8s.env.plugin.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvMode {
    CONFIGMAP_AND_SECRET("Configmaps and secrets"),
    POD_ENV("Pod environment"),
    POD_VAULT("Pod vault environment");

    private final String textName;

}
