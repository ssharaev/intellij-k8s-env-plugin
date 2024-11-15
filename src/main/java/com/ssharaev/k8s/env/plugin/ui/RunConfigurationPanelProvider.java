package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import lombok.Getter;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

import static com.ssharaev.k8s.env.plugin.Utils.joinIfNotNull;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;

public class RunConfigurationPanelProvider {

    private final JBTextField namespaceTextField;
    private final InputTextPanel configmapsPanel;
    private final InputTextPanel secretsPanel;
    private final InputTextPanel podsPanel;
    private final ComboBox<EnvMode> envModeList;
    @Getter
    private final JPanel panel;


    public RunConfigurationPanelProvider() {
        this.namespaceTextField = new JBTextField("default");
        this.configmapsPanel = new InputTextPanel("Configmaps names:", "Separate names with semicolon: configmap1;configmap2");
        this.secretsPanel = new InputTextPanel("Secrets names:", "Separate names with semicolon: secret1;secret2");
        this.podsPanel = new InputTextPanel("Pods names:", "Separate names with semicolon: pod1;pod2");
        this.envModeList = new ComboBox<>(EnvMode.values());
        envModeList.addItemListener(e -> updatePanel());
        JBLabel panelLabel = new JBLabel("K8s env");
        panelLabel.setFont(JBFont.h4().asBold());

        this.panel =  FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(panelLabel)
                .addLabeledComponent(new JBLabel("Environment mode:"), envModeList, 1, false)
                .addLabeledComponent(new JBLabel("Namespace:"), namespaceTextField, 1, false)
                .addComponent(configmapsPanel.getPanel())
                .addComponent(secretsPanel.getPanel())
                .addComponent(podsPanel.getPanel())
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        updatePanel();
    }

    public PluginSettings getState() {
        EnvMode mode = EnvMode.values()[envModeList.getSelectedIndex()];
        String namespace = namespaceTextField.getText();
        List<String> configmapNames = splitIfNotEmpty(configmapsPanel.getText());
        List<String> secretNames = splitIfNotEmpty(secretsPanel.getText());
        String podName = podsPanel.getText();
        return PluginSettings.builder()
                .envMode(mode)
                .namespace(namespace)
                .configmapNames(configmapNames)
                .secretNames(secretNames)
                .podName(podName)
                .build();
    }

    public void setState(PluginSettings pluginSettings) {
        envModeList.setItem(Optional.ofNullable(pluginSettings.getEnvMode()).orElse(EnvMode.CONFIGMAP_AND_SECRET));
        namespaceTextField.setText(Optional.ofNullable(pluginSettings.getNamespace()).orElse("default"));
        configmapsPanel.setText(joinIfNotNull(pluginSettings.getConfigmapNames()));
        secretsPanel.setText(joinIfNotNull(pluginSettings.getSecretNames()));
        podsPanel.setText(pluginSettings.getPodName());
    }

    public void updatePanel() {
        EnvMode mode = EnvMode.values()[envModeList.getSelectedIndex()];
        if (mode == EnvMode.CONFIGMAP_AND_SECRET) {
            this.podsPanel.hide();
            this.secretsPanel.show();
            this.configmapsPanel.show();
            panel.repaint();
        }
        if (mode != EnvMode.CONFIGMAP_AND_SECRET) {
            this.podsPanel.show();
            this.secretsPanel.hide();
            this.configmapsPanel.hide();
            panel.repaint();
        }
    }

    public List<JComponent> getComponentsForWatcher() {
        return List.of(envModeList, configmapsPanel.textField, secretsPanel.textField, podsPanel.textField);
    }


    @Getter
    public static class InputTextPanel {
        private final JTextField textField;
        private final JBLabel label;
        private final JBLabel tooltip;
        private final JPanel panel;

        public InputTextPanel(String labelText, String tooltipText) {
            this.textField = new JBTextField();
            this.label = new JBLabel(labelText);
            this.tooltip = new JBLabel(tooltipText, UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER);
            this.tooltip.setBorder(JBUI.Borders.emptyLeft(10));
            this.panel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(label, textField, 1, false)
                    .addComponentToRightColumn(tooltip, 1)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();
        }

        public void hide() {
            this.panel.setVisible(false);
        }

        public void show() {
            this.panel.setVisible(true);
        }

        public String getText() {
            return this.textField.getText();
        }

        public void setText(String text) {
            this.textField.setText(text);
        }
    }
}
