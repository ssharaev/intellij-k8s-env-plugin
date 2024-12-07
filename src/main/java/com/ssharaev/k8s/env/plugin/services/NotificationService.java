package com.ssharaev.k8s.env.plugin.services;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

public class NotificationService {

    private static final String NOTIFICATION_GROUP = "com.ssharaev.k8sEnv";

    public static void notifyWarn(@NotNull String title, @NotNull String body) {
        Notifications.Bus.notify(new Notification(NOTIFICATION_GROUP, title, body, NotificationType.WARNING));
    }
}
