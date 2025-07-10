package com.example.preely.model;

import com.google.firebase.firestore.DocumentId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @DocumentId
    String id;
    String user_id;
    String content;
    boolean is_read;
    String url;
    String create_at;
    String update_at;

}
