package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBFont;
import com.ssharaev.k8s.env.plugin.PluginSettings;
import com.ssharaev.k8s.env.plugin.Utils;
import lombok.Getter;

import javax.swing.*;
import java.util.List;

public class RunConfigurationPanelProvider {

    @Getter
    private final JPanel panel;
    private final JTextField namespaceTextField;
    private final JTextField configmapNamesTextField;

    public RunConfigurationPanelProvider() {
        this.namespaceTextField = new JBTextField();
        this.configmapNamesTextField = new JBTextField();
        JBLabel panelLabel = new JBLabel("K8s env");
        panelLabel.setFont(JBFont.h4().asBold());
        this.panel = FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(panelLabel)
                .addLabeledComponent(new JBLabel("Namespace:"), namespaceTextField, 1, false)
                .addLabeledComponent(new JBLabel("Configmap names:"), configmapNamesTextField, 1, false)
                .addTooltip("Separate names with semicolon: configmap1;configmap2")
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
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
