package com.example.preely.util;

public class Constraints {

    public interface CollectionName {
        String USERS = "user";
        String CATEGORIES = "category";
        String POSTS = "post";
        String TAGS = "tag";
        String MESSAGES = "message";
        String NOTIFICATIONS = "notification";
        String SAVED_POST = "savedposts";
        String IMAGE = "image";
        String TRANSACTION = "transactions";
    }

    public interface NotificationType {
        int SUCCESS = 1;
        int ERROR = 2;

    }

    public interface SortType {
        int MOST_VIEW = 0;
        int DATE_ASC = 1;
        int DATE_DESC = 2;
    }

}
