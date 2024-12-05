package com.ssharaev.k8s.env.plugin.ui.replacement;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import org.jetbrains.annotations.Nullable;

public class ReplacementTableColumnInfo extends ColumnInfo<ReplacementEntity, String> {

    public ReplacementTableColumnInfo(@NlsContexts.ColumnName String name) {
        super(name);
    }

    @Override
    public @Nullable String valueOf(ReplacementEntity replacementEntity) {
        return replacementEntity.getReplacement();
    }

    @Override
    public void setValue(ReplacementEntity replacementEntity, String value) {
        replacementEntity.setReplacement(value);
    }

    @Override
    public boolean isCellEditable(ReplacementEntity replacementEntity) {
        return true;
    }

}
