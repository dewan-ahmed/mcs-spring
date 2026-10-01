package io.github.dewanahmed.notify;

import java.util.Locale;
import java.util.Set;

public final class NotificationStatus {

    public static final String QUEUED = "queued";
    public static final String SENT = "sent";
    public static final String FAILED = "failed";
    public static final String DELIVERED = "delivered";

    private static final Set<String> ALLOWED = Set.of(QUEUED, SENT, FAILED, DELIVERED);

    private NotificationStatus() {
    }

    public static boolean isAllowed(String status) {
        return status != null && ALLOWED.contains(status.trim().toLowerCase(Locale.ROOT));
    }

    public static String normalize(String status) {
        return status.trim().toLowerCase(Locale.ROOT);
    }
}
