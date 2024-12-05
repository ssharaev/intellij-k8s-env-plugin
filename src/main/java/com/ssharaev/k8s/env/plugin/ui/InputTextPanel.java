package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class InputTextPanel {
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
