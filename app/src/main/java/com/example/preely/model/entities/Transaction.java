package com.example.preely.model.entities;

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
public class Transaction extends BaseEntity{

    String giver_id;
    String post_id;
    String requester_id;
    Number amount;
    String status;
    String transaction_date;

}
