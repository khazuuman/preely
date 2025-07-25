package com.example.preely.util;

public class Constraints {

    public interface CollectionName {
        String USERS = "user";
        String CATEGORIES = "category";
        String MESSAGES = "messages";
        String NOTIFICATIONS = "notification";
        String TRANSACTION = "transactions";
        String SKILL = "skill";
        String SERVICE = "service";
        String SAVED_SERVICE = "saved_service";
        String BOOKING = "booking";
    }

    public interface NotificationType {
        int SUCCESS = 1;
        int ERROR = 2;
        int INFO = 3;
    }

    public interface SortType {
        int DATE_ASC = 1;
        int DATE_DESC = 2;
        int MOST_REVIEW = 3;
        int PRICE_ASC = 4;
        int PRICE_DESC = 5;
    }

    public interface BookingStatus {
        String PENDING = "Pending";
        String CLAIMED = "Claimed";
        String COMPLETED = "Completed";
        String CANCELLED = "Cancelled";
    }

    public enum Availability {
        WEEKENDS("Weekends"),
        WEEKDAYS("Weekdays"),
        MON_FRI_MORNINGS("Mon-Fri mornings"),
        MON_FRI_AFTERNOONS("Mon-Fri afternoons"),
        MON_FRI_EVENINGS("Mon-Fri evenings"),
        EVENINGS_ONLY("Evenings only"),
        MORNINGS_ONLY("Mornings only"),
        AFTERNOONS_ONLY("Afternoons only"),
        FLEXIBLE("Flexible"),
        FULL_TIME("Full-time"),
        PART_TIME("Part-time"),
        ON_DEMAND("On demand"),
        BY_APPOINTMENT("By appointment"),
        NOT_AVAILABLE("Not available");

        private final String label;

        Availability(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
