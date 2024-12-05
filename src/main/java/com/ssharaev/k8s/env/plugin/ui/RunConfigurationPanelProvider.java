package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.MutableCollectionComboBoxModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.ListTableModel;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import com.ssharaev.k8s.env.plugin.ui.replacement.RegexpTableColumnInfo;
import com.ssharaev.k8s.env.plugin.ui.replacement.ReplacementTableColumnInfo;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.ssharaev.k8s.env.plugin.Utils.joinIfNotNull;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

public class RunConfigurationPanelProvider {

    private final ComboBox<String> envModeComboBox;

    private final InputTextPanel configmapsPanel;
    private final InputTextPanel secretsPanel;
    private final InputTextPanel podsPanel;
    private final ListTableModel<ReplacementEntity> replacementModel;
    private final MutableCollectionComboBoxModel<String> namespaceComboBoxModel;

    @Getter
    private final JPanel panel;

    public RunConfigurationPanelProvider() {
        this.configmapsPanel = new InputTextPanel("Configmaps names:", "Separate names with semicolon: configmap1;configmap2");
        this.secretsPanel = new InputTextPanel("Secrets names:", "Separate names with semicolon: secret1;secret2");
        this.podsPanel = new InputTextPanel("Pod name prefix:", "E.g. you can use \"nginx\" instead of \"nginx-554b9c67f9-c5cv4\"");
        this.envModeComboBox = new ComboBox<>(EnvMode.beautyNames());
        this.envModeComboBox.addItemListener(e -> updatePanel());
        this.replacementModel = new ListTableModel<>(
                new RegexpTableColumnInfo("Regexp"),
                new ReplacementTableColumnInfo("Replacement"));
        this.namespaceComboBoxModel = new MutableCollectionComboBoxModel<>(new ArrayList<>());
        ComboBox<String> namespaceComboBox = new ComboBox<>(namespaceComboBoxModel);
        JBTable replacementEntityTable = new JBTable(replacementModel);

        ToolbarDecorator decorator = ToolbarDecorator
                .createDecorator(replacementEntityTable)
                .disableUpDownActions()
                .setAddAction(a -> this.replacementModel.addRow(new ReplacementEntity()));
        replacementEntityTable.getEmptyText().setText("No replacement created");

        JBLabel panelLabel = new JBLabel("Environment from Kubernetes");
        panelLabel.setFont(JBFont.regular().asBold());

        FormBuilder builder = FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(panelLabel)
                .addLabeledComponent(new JBLabel("Environment mode:"), envModeComboBox, 1, false)
                .addLabeledComponent(new JBLabel("Namespace:"), namespaceComboBox, 1, false);
        configmapsPanel.addToBuilder(builder);
        secretsPanel.addToBuilder(builder);
        podsPanel.addToBuilder(builder);
        this.panel = builder
                .addLabeledComponent("Replace:", decorator.createPanel(), 1, false)
                .addTooltip("You can use capture groups. E.g. (\\w*)(-dev) and $1-local replace \"env-dev\" to \"env-local\"")
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        updatePanel();
    }

    public PluginSettings getState() {
        EnvMode mode = EnvMode.values()[envModeComboBox.getSelectedIndex()];
        String namespace = StringUtils.trimToNull(namespaceComboBoxModel.getSelected());
        List<String> configmapNames = splitIfNotEmpty(configmapsPanel.getText());
        List<String> secretNames = splitIfNotEmpty(secretsPanel.getText());
        String podName = StringUtils.trimToNull(podsPanel.getText());
        List<ReplacementEntity> replacementEntities = emptyIfNull(replacementModel.getItems()).stream()
                .filter(e -> StringUtils.isNotBlank(e.getReplacement()))
                .toList();
        return PluginSettings.builder()
                .envMode(mode)
                .namespace(namespace)
                .configmapNames(configmapNames)
                .secretNames(secretNames)
                .podName(podName)
                .replacementEntities(replacementEntities)
                .build();
    }

    public void setState(PluginSettings pluginSettings) {
        KubernetesService kubernetesService = ApplicationManager.getApplication().getService(KubernetesService.class);
        String envModeName = pluginSettings.getEnvMode().getBeautyName();
        namespaceComboBoxModel.update(kubernetesService.getNamespaces());
        namespaceComboBoxModel.setSelectedItem(pluginSettings.getNamespace());
        envModeComboBox.setItem(envModeName);
        configmapsPanel.setText(joinIfNotNull(pluginSettings.getConfigmapNames()));
        secretsPanel.setText(joinIfNotNull(pluginSettings.getSecretNames()));
        podsPanel.setText(pluginSettings.getPodName());
        emptyIfNull(pluginSettings.getReplacementEntities()).forEach(replacementModel::addRow);
    }

    private void updatePanel() {
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
}
