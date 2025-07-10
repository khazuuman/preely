package com.example.preely.util;

import java.util.List;

public interface FirestoreCallback<T> {
    void onCallback(List<T> data);
}
