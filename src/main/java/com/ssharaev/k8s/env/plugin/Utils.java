package com.ssharaev.k8s.env.plugin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static final String CONFIGMAP_DELIMITER = ";";

    public static String joinIfNotNull(List<String> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }
        return String.join(Utils.CONFIGMAP_DELIMITER, names);
    }

    public static List<String> splitIfNotEmpty(String string) {
        return string == null ? List.of() : List.of(string.split(Utils.CONFIGMAP_DELIMITER));
    }
}
