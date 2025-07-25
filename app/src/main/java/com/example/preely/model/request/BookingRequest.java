package com.example.preely.model.request;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    DocumentReference service_id;
    DocumentReference seeker_id;
    DocumentReference provider_id;

    Timestamp booking_time;
    String time_slot;
    String status;
    String notes;

}
