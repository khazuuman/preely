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
    }

    public interface NotificationType {
        int SUCCESS = 1;
        int ERROR = 2;

    }

}
