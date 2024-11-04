package com.ssharaev.k8s.env.plugin.ui;

import com.ssharaev.k8s.env.plugin.PluginSettings;
import com.ssharaev.k8s.env.plugin.Utils;

import javax.swing.*;
import java.util.List;

public class RunConfigurationPanel extends JPanel {


    private final JTextField namespaceTextField;
    private final JTextField configmapNamesTextField;

    public RunConfigurationPanel() {
        this.namespaceTextField = new JTextField();
        this.configmapNamesTextField = new JTextField();
    }

    public PluginSettings getState() {
        String namespace = namespaceTextField.getText();
        List<String> configmapNames = configmapNamesTextField.getText() == null ? List.of() :
                List.of(configmapNamesTextField.getText().split(Utils.CONFIGMAP_DELIMITER));
        return new PluginSettings(namespace, configmapNames);
    }

    public void setState(PluginSettings pluginSettings) {
        namespaceTextField.setText(pluginSettings.getNamespace());
        configmapNamesTextField.setText(String.join(Utils.CONFIGMAP_DELIMITER, pluginSettings.getConfigmapNames()));
    }
}
