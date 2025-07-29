package com.example.preely.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.preely.util.Constraints;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class ServiceMarketDetailResponse extends CommonResponse implements Parcelable {
    String title;
    String providerName;
    String categoryName;
    String university;
    Constraints.Availability availability;
    Double price;
    String status;
    String description;
    Constraints.PriceUnitType price_unit;
    Float average_rating;
    Integer total_reviews;
    List<String> image_urls;
    GeoPoint location;
    List<SkillResponse> skills;
    Timestamp create_at;
    Timestamp update_at;

    protected ServiceMarketDetailResponse(Parcel in) {
        super(in);
        title = in.readString();
        providerName = in.readString();
        categoryName = in.readString();
        university = in.readString();

        String availabilityStr = in.readString();
        availability = availabilityStr != null ? Constraints.Availability.valueOf(availabilityStr) : null;

        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }

        status = in.readString();
        description = in.readString();

        String priceUnitStr = in.readString();
        price_unit = priceUnitStr != null ? Constraints.PriceUnitType.valueOf(priceUnitStr) : null;

        if (in.readByte() == 0) {
            average_rating = null;
        } else {
            average_rating = in.readFloat();
        }

        if (in.readByte() == 0) {
            total_reviews = null;
        } else {
            total_reviews = in.readInt();
        }

        image_urls = in.createStringArrayList();

        double lat = in.readDouble();
        double lng = in.readDouble();
        location = new GeoPoint(lat, lng);

        skills = in.createTypedArrayList(SkillResponse.CREATOR);

        long createMillis = in.readLong();
        long updateMillis = in.readLong();
        create_at = new Timestamp(new Date(createMillis));
        update_at = new Timestamp(new Date(updateMillis));
    }

    public static final Creator<ServiceMarketDetailResponse> CREATOR = new Creator<ServiceMarketDetailResponse>() {
        @Override
        public ServiceMarketDetailResponse createFromParcel(Parcel in) {
            return new ServiceMarketDetailResponse(in);
        }

        @Override
        public ServiceMarketDetailResponse[] newArray(int size) {
            return new ServiceMarketDetailResponse[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(title);
        dest.writeString(providerName);
        dest.writeString(categoryName);
        dest.writeString(university);
        dest.writeString(availability != null ? availability.name() : null);

        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }

        dest.writeString(status);
        dest.writeString(description);
        dest.writeString(price_unit != null ? price_unit.name() : null);

        if (average_rating == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(average_rating);
        }

        if (total_reviews == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(total_reviews);
        }

        dest.writeStringList(image_urls);

        dest.writeDouble(location.getLatitude());
        dest.writeDouble(location.getLongitude());

        dest.writeTypedList(skills);

        dest.writeLong(create_at.toDate().getTime());
        dest.writeLong(update_at.toDate().getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
