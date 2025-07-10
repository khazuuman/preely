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
public class Savedpost {

    @DocumentId
    String id;
    String post_id;
    String save_date;
    String user_id;

}
