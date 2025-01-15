package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.MutableCollectionComboBoxModel;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.ListTableModel;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import com.ssharaev.k8s.env.plugin.ui.replacement.RegexpTableColumnInfo;
import com.ssharaev.k8s.env.plugin.ui.replacement.ReplacementTableColumnInfo;
import lombok.Getter;

import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import java.util.ArrayList;
import java.util.List;

import static com.ssharaev.k8s.env.plugin.Utils.emptyIfNull;
import static com.ssharaev.k8s.env.plugin.Utils.getKubernetesService;
import static com.ssharaev.k8s.env.plugin.Utils.joinIfNotNull;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;
import static com.ssharaev.k8s.env.plugin.Utils.trimToNull;

public class RunConfigurationPanelProvider {

    private final ComboBox<String> envModeComboBox;

    private final InputTextElement configmapsElement;
    private final InputTextElement secretsElement;
    private final InputTextElement podsElement;
    private final HideableElement<ComboBox<String>> namespacesElement;
    private final HideableElement<JPanel> replaceTableElement;
    private final ListTableModel<ReplacementEntity> replacementModel;
    private final MutableCollectionComboBoxModel<String> namespaceComboBoxModel;

    @Getter
    private final JPanel panel;

    public RunConfigurationPanelProvider() {
        this.configmapsElement = new InputTextElement("Configmaps names:", "Separate names with semicolon: configmap1;configmap2", new JBTextField());
        this.secretsElement = new InputTextElement("Secrets names:", "Separate names with semicolon: secret1;secret2", new JBTextField());
        this.podsElement = new InputTextElement("Pod name prefix:", "E.g. you can use \"nginx\" instead of \"nginx-554b9c67f9-c5cv4\"", new JBTextField());
        this.envModeComboBox = new ComboBox<>(EnvMode.beautyNames());
        this.envModeComboBox.addItemListener(e -> updatePanel());
        this.replacementModel = new ListTableModel<>(
                new RegexpTableColumnInfo("Search"),
                new ReplacementTableColumnInfo("Replace"));
        this.namespaceComboBoxModel = new MutableCollectionComboBoxModel<>(new ArrayList<>());
        ComboBox<String> namespaceComboBox = new ComboBox<>(namespaceComboBoxModel);
        namespaceComboBox.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                updateNamespaceComboBoxModel();
            }
        });
        this.namespacesElement = new HideableElement<>("Namespace:", namespaceComboBox);
        JBTable replacementEntityTable = new JBTable(replacementModel);

        ToolbarDecorator decorator = ToolbarDecorator
                .createDecorator(replacementEntityTable)
                .disableUpDownActions()
                .setAddAction(a -> this.replacementModel.addRow(new ReplacementEntity()));
        replacementEntityTable.getEmptyText().setText("No replacement created");

        JBLabel panelLabel = new JBLabel("Environment from Kubernetes");
        panelLabel.setFont(JBFont.regular().asBold());

        this.replaceTableElement = new HideableElement<>("Replace:",
                "You can use capture groups. E.g. (\\w*)(-dev) and $1-local replace \"env-dev\" to \"env-local\"",
                decorator.createPanel());

        FormBuilder builder = FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(panelLabel)
                .addLabeledComponent(new JBLabel("Environment mode:"), envModeComboBox, 1, false);
        namespacesElement.addToBuilder(builder);
        configmapsElement.addToBuilder(builder);
        secretsElement.addToBuilder(builder);
        podsElement.addToBuilder(builder);
        replaceTableElement.addToBuilder(builder);

        this.panel = builder.getPanel();
        updatePanel();
    }

    public PluginSettings getState() {
        EnvMode mode = EnvMode.values()[envModeComboBox.getSelectedIndex()];
        String namespace = trimToNull(namespaceComboBoxModel.getSelected());
        List<String> configmapNames = splitIfNotEmpty(configmapsElement.getText());
        List<String> secretNames = splitIfNotEmpty(secretsElement.getText());
        String podName = trimToNull(podsElement.getText());
        List<ReplacementEntity> replacementEntities = emptyIfNull(replacementModel.getItems()).stream()
                .filter(e -> e.getReplacement() != null && !e.getReplacement().isBlank())
                .toList();
        return PluginSettings.builder()
                .envMode(mode)
                .enabled(mode != EnvMode.DISABLED)
                .namespace(namespace)
                .configmapNames(configmapNames)
                .secretNames(secretNames)
                .podName(podName)
                .replacementEntities(replacementEntities)
                .build();
    }

    public void setState(PluginSettings pluginSettings) {
        EnvMode envMode = pluginSettings.getEnvMode();
        String envModeName = envMode.getBeautyName();
        envModeComboBox.setItem(envModeName);
        if (envMode == EnvMode.DISABLED) {
            return;
        }
        updateNamespaceComboBoxModel();
        namespaceComboBoxModel.setSelectedItem(pluginSettings.getNamespace());
        configmapsElement.setText(joinIfNotNull(pluginSettings.getConfigmapNames()));
        secretsElement.setText(joinIfNotNull(pluginSettings.getSecretNames()));
        podsElement.setText(pluginSettings.getPodName());
        emptyIfNull(pluginSettings.getReplacementEntities()).forEach(replacementModel::addRow);
    }

    private void updateNamespaceComboBoxModel() {
        String selected = namespaceComboBoxModel.getSelected();
        namespaceComboBoxModel.update(getKubernetesService().getNamespaces());
        namespaceComboBoxModel.setSelectedItem(selected);
    }

    private void updatePanel() {
        EnvMode mode = EnvMode.values()[envModeComboBox.getSelectedIndex()];
        if (mode == EnvMode.DISABLED) {
            this.podsElement.hide();
            this.secretsElement.hide();
            this.configmapsElement.hide();
            this.namespacesElement.hide();
            this.replaceTableElement.hide();
            panel.repaint();
            return;
        }
        updateNamespaceComboBoxModel();
        this.namespacesElement.show();
        this.replaceTableElement.show();
        if (mode == EnvMode.CONFIGMAP_AND_SECRET) {
            this.podsElement.hide();
            this.secretsElement.show();
            this.configmapsElement.show();
            panel.repaint();
            return;
        }
        this.podsElement.show();
        this.secretsElement.hide();
        this.configmapsElement.hide();
        panel.repaint();
    }
}
