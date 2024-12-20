package com.ssharaev.k8s.env.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static final String NOTIFICATION_TITLE = "Kubernetes env disabled";
    public static final String DELIMITER = ";";

    public static String joinIfNotNull(List<String> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }
        return String.join(Utils.DELIMITER, names);
    }

    public static List<String> splitIfNotEmpty(String string) {
        return string == null ? List.of() : List.of(string.split(Utils.DELIMITER));
    }

    public static <T> List<T> emptyIfNull(final List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName, E defaultEnum) {
        if (enumName == null) {
            return defaultEnum;
        } else {
            try {
                return Enum.valueOf(enumClass, enumName);
            } catch (IllegalArgumentException var4) {
                return defaultEnum;
            }
        }
    }

    public static String trimToNull(String str) {
        if (str == null) {
            return str;
        }
        String ts = str.trim();
        return str.isEmpty() ? null : ts;
    }

    public static KubernetesService getKubernetesService() {
        return ApplicationManager.getApplication().getService(KubernetesService.class);
    }


}
