package com.example.preely.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillResponse extends CommonResponse implements Parcelable {
    String name;

    protected SkillResponse(Parcel in) {
        name = in.readString();
    }

    public static final Creator<SkillResponse> CREATOR = new Creator<SkillResponse>() {
        @Override
        public SkillResponse createFromParcel(Parcel in) {
            return new SkillResponse(in);
        }

        @Override
        public SkillResponse[] newArray(int size) {
            return new SkillResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
    }
}
