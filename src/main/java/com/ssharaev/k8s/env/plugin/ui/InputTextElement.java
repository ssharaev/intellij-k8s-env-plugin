package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.ui.components.JBTextField;

public class InputTextElement extends HideableElement<JBTextField> {

    public InputTextElement(String labelText, String tooltipText, JBTextField component) {
        super(labelText, tooltipText, component);
    }

    public String getText() {
        return getComponent().getText();
    }

    public void setText(String text) {
        getComponent().setText(text);
    }
}
