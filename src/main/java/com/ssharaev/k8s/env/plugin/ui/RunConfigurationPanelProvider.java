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
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

import static com.ssharaev.k8s.env.plugin.Utils.joinIfNotNull;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;

public class RunConfigurationPanelProvider {

    private final JBTextField namespaceTextField;
    private final ComboBox<String> envModeComboBox;

    private final InputTextPanel configmapsPanel;
    private final InputTextPanel secretsPanel;
    private final InputTextPanel podsPanel;
    @Getter
    private final JPanel panel;


    public RunConfigurationPanelProvider() {
        this.namespaceTextField = new JBTextField("default");
        this.configmapsPanel = new InputTextPanel("Configmaps names:", "Separate names with semicolon: configmap1;configmap2");
        this.secretsPanel = new InputTextPanel("Secrets names:", "Separate names with semicolon: secret1;secret2");
        this.podsPanel = new InputTextPanel("Pods names:", "Separate names with semicolon: pod1;pod2");
        this.envModeComboBox = new ComboBox<>(EnvMode.beautyNames());
        envModeComboBox.addItemListener(e -> updatePanel());
        JBLabel panelLabel = new JBLabel("K8s env");
        panelLabel.setFont(JBFont.h4().asBold());

        FormBuilder builder = FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(panelLabel)
                .addLabeledComponent(new JBLabel("Environment mode:"), envModeComboBox, 1, false)
                .addLabeledComponent(new JBLabel("Namespace:"), namespaceTextField, 1, false);
        configmapsPanel.addToBuilder(builder);
        secretsPanel.addToBuilder(builder);
        podsPanel.addToBuilder(builder);
        this.panel = builder
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        updatePanel();
    }

    public PluginSettings getState() {
        EnvMode mode = EnvMode.values()[envModeComboBox.getSelectedIndex()];
        String namespace = StringUtils.trimToNull(namespaceTextField.getText());
        List<String> configmapNames = splitIfNotEmpty(configmapsPanel.getText());
        List<String> secretNames = splitIfNotEmpty(secretsPanel.getText());
        String podName = StringUtils.trimToNull(podsPanel.getText());
        return PluginSettings.builder()
                .envMode(mode)
                .namespace(namespace)
                .configmapNames(configmapNames)
                .secretNames(secretNames)
                .podName(podName)
                .build();
    }

    public void setState(PluginSettings pluginSettings) {
        String envModeName = Optional.ofNullable(pluginSettings.getEnvMode())
                .orElse(EnvMode.CONFIGMAP_AND_SECRET)
                .getBeautyName();
        envModeComboBox.setItem(envModeName);
        namespaceTextField.setText(Optional.ofNullable(pluginSettings.getNamespace()).orElse("default"));
        configmapsPanel.setText(joinIfNotNull(pluginSettings.getConfigmapNames()));
        secretsPanel.setText(joinIfNotNull(pluginSettings.getSecretNames()));
        podsPanel.setText(pluginSettings.getPodName());
    }

    public void updatePanel() {
        EnvMode mode = EnvMode.values()[envModeComboBox.getSelectedIndex()];
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

    public static class InputTextPanel {
        private final JTextField textField;
        private final JBLabel label;
        private final JBLabel tooltip;

        public InputTextPanel(String labelText, String tooltipText) {
            this.textField = new JBTextField();
            this.label = new JBLabel(labelText);
            this.tooltip = new JBLabel(tooltipText, UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER);
            this.tooltip.setBorder(JBUI.Borders.emptyLeft(10));
        }

        public void addToBuilder(FormBuilder builder) {
            builder.addLabeledComponent(label, textField, 1, false)
                    .addComponentToRightColumn(tooltip, 1)
                    .addComponentFillVertically(new JPanel(), 0);
        }

        public void hide() {
            this.textField.setVisible(false);
            this.label.setVisible(false);
            this.tooltip.setVisible(false);
        }

        public void show() {
            this.textField.setVisible(true);
            this.label.setVisible(true);
            this.tooltip.setVisible(true);
        }

        public String getText() {
            return this.textField.getText();
        }

        public void setText(String text) {
            this.textField.setText(text);
        }
    }
}
