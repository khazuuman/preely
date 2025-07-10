package com.example.preely.model.dto;

import com.google.firebase.Timestamp;
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
public class CommonDto {

    @DocumentId
    String id;
    Timestamp update_at;
    Timestamp create_at;

}
