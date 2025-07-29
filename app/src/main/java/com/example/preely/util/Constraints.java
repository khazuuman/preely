package com.example.preely.util;

import lombok.Getter;

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

    public interface AccountType {
        String LOCAL = "local";
        String GOOGLE = "google";
        String TWITTER = "twitter";
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

    @Getter
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

    }

    @Getter
    public enum PriceUnitType {
        HOUR("hour"),
        DAY("day"),
        WEEK("week"),
        MONTH("month"),
        ONCE("once");
        private final String label;

        PriceUnitType(String label) {
            this.label = label;
        }
    }

        public static final String BOOKING_STATUS_PENDING = "PENDING";
        public static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
        public static final String BOOKING_STATUS_CANCELLED = "CANCELLED";
        public static final String BOOKING_STATUS_COMPLETED = "COMPLETED";
    }

