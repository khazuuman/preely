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
public class Transaction {

    @DocumentId
    String id;
    String giver_id;
    String post_id;
    String requester_id;
    Number amount;
    String status;
    String transaction_date;
    String update_at;
    String create_at;

}
