package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class HideableElement<T extends JComponent> {
    private final T component;
    private final JBLabel label;
    private final JBLabel tooltip;

    public HideableElement(String labelText, String tooltipText, T component) {
        this.component = component;
        this.label = new JBLabel(labelText);
        if (tooltipText != null) {
            this.tooltip = new JBLabel(tooltipText, UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER);
            this.tooltip.setBorder(JBUI.Borders.emptyLeft(10));
        } else {
            this.tooltip = null;
        }
    }

    public HideableElement(String labelText, T component) {
        this(labelText, null, component);
    }

    public void addToBuilder(FormBuilder builder) {
        builder.addLabeledComponent(label, component, 1, false)
                .addComponentFillVertically(new JPanel(), 0);
        if (tooltip != null) {
            builder.addComponentToRightColumn(tooltip, 1);
        }
    }

    public void hide() {
        this.component.setVisible(false);
        this.label.setVisible(false);
        if (tooltip != null) {
            this.tooltip.setVisible(false);
        }
    }

    public void show() {
        this.component.setVisible(true);
        this.label.setVisible(true);
        if (tooltip != null) {
            this.tooltip.setVisible(true);
        }
    }

    protected T getComponent() {
        return component;
    }
}
