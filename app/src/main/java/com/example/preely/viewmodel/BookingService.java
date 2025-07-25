package com.example.preely.viewmodel;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Booking;
import com.example.preely.model.entities.Category;
import com.example.preely.model.request.BookingRequest;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.Constraints;
import com.example.preely.util.DataUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class BookingService extends ViewModel {

    public final MainRepository<Booking> bookingMainRepository = new MainRepository<>(Booking.class, Constraints.CollectionName.BOOKING);

    private final MutableLiveData<Boolean> bookingResult = new MutableLiveData<>();

    public LiveData<Boolean> getBookingResult() {
        return bookingResult;
    }
    public void insertBooking(BookingRequest request) throws IllegalAccessException, InstantiationException {
        Booking booking = DataUtil.mapObj(request, Booking.class);
        booking.setCreate_at(Timestamp.now());
        booking.setStatus(Constraints.BookingStatus.PENDING);
        booking.setBooking_time(Timestamp.now());
        booking.setService_id(request.getService_id());
        booking.setSeeker_id(request.getSeeker_id());

        booking.getService_id().get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DocumentReference providerRef = documentSnapshot.getDocumentReference("provider_id");

                        if (providerRef != null) {
                            booking.setProvider_id(providerRef);

                            bookingMainRepository.add(booking, Constraints.CollectionName.BOOKING, new CallBackUtil.OnInsertCallback() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    bookingResult.setValue(true);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    bookingResult.setValue(false);
                                }
                            });
                        } else {
                            Log.d("FIREBASE", "Provider reference is null");
                            bookingResult.setValue(false);
                        }
                    } else {
                        Log.d("FIREBASE", "Service not found");
                        bookingResult.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error getting service document", e);
                    bookingResult.setValue(false);
                });
    }


}
