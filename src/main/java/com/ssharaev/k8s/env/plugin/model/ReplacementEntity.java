package com.ssharaev.k8s.env.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplacementEntity {

    private String regexp;
    private String replacement;
}
