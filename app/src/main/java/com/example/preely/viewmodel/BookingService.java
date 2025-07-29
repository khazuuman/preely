package com.example.preely.viewmodel;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BookingService extends ViewModel {

    public final MainRepository<Booking> bookingMainRepository = new MainRepository<>(Booking.class, Constraints.CollectionName.BOOKING);

    private final MutableLiveData<Boolean> bookingResult = new MutableLiveData<>();
    public LiveData<Boolean> getBookingResult() {
        return bookingResult;
    }

    private final MutableLiveData<String> errorMessageNotification = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessageDay = new MutableLiveData<>();

    public LiveData<String> getErrorMessageDay() {
        return errorMessageDay;
    }

    private final MutableLiveData<String> errorMessageTime = new MutableLiveData<>();

    public LiveData<String> getErrorMessageTime() {
        return errorMessageTime;
    }

    private final MutableLiveData<String> errorMessageUnit = new MutableLiveData<>();

    public LiveData<String> getErrorMessageUnit() {
        return errorMessageUnit;
    }

    public void insertBooking(BookingRequest request) throws IllegalAccessException, InstantiationException {
        Booking booking = DataUtil.mapObj(request, Booking.class);
        booking.setCreate_at(Timestamp.now());
        booking.setStatus(Constraints.BOOKING_STATUS_PENDING);
        booking.setBooking_time(Timestamp.now());
        booking.setService_id(request.getService_id());
        booking.setSeeker_id(request.getSeeker_id());
        booking.setTotal_price(request.getTotal_price());

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

    public boolean validateBooking(BookingRequest request, Constraints.Availability availability) {
        errorMessageDay.setValue(null);
        errorMessageTime.setValue(null);
        errorMessageUnit.setValue(null);
        boolean isValid = true;
        if (request.getTime_slot() != null) {
            Log.i("TIME SLOT", request.getTime_slot());
            String[] parts = request.getTime_slot().split(" - ");
            String timePart = parts[0];
            String datePart = parts[1];

            int dateCheck = DataUtil.isFutureDate(datePart);
            if (dateCheck == 0) {
                errorMessageDay.setValue("Date must in future");
                isValid = false;
            } else if (dateCheck == 2) {
                if (!DataUtil.isAtLeast3HoursFromNow(request.getTime_slot())) {
                    errorMessageTime.setValue("Time must be at least 3 hours from now");
                    isValid = false;
                }
            }

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
            try {
                Date fullDate = dateFormat.parse(request.getTime_slot());
                assert fullDate != null;
                calendar.setTime(fullDate);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                Log.i("HOUR + DAY", String.valueOf(hour) + " - " + String.valueOf(dayOfWeek));
                Log.i("AVAILABLE", String.valueOf(availability));

                switch (availability) {
                    case WEEKENDS:
                        if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                            errorMessageDay.setValue("Service not available on weekends");
                            isValid = false;
                        }
                        break;
                    case WEEKDAYS:
                        if (!(dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY)) {
                            errorMessageDay.setValue("Service not available on weekdays");
                            isValid = false;
                        }
                        break;
                    case MON_FRI_MORNINGS:
                        if (!(dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) || !(hour >= 6 && hour < 12)) {
                            errorMessageDay.setValue("Service not available on Mon-Fri mornings");
                            isValid = false;
                        }
                        break;
                    case MON_FRI_AFTERNOONS:
                        if (!(dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) || !(hour >= 12 && hour < 18)) {
                            errorMessageDay.setValue("Service not available on Mon-Fri afternoons");
                            isValid = false;
                        }
                        break;
                    case MON_FRI_EVENINGS:
                        if (!(dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) || !(hour >= 18 && hour < 22)) {
                            errorMessageDay.setValue("Service not available on Mon-Fri evenings");
                            isValid = false;
                        }
                        break;
                    case EVENINGS_ONLY:
                        if (!(hour >= 18 && hour < 22)) {
                            errorMessageDay.setValue("Service not available on evenings only");
                            isValid = false;
                        }
                        break;
                    case MORNINGS_ONLY:
                        if (!(hour >= 6 && hour < 12)) {
                            errorMessageDay.setValue("Service not available on mornings only");
                            isValid = false;
                        }
                        break;
                    case AFTERNOONS_ONLY:
                        if (!(hour >= 12 && hour < 18)) {
                            errorMessageDay.setValue("Service not available on afternoons only");
                            isValid = false;
                        }
                        break;
                    case FLEXIBLE:
                        break;
                    case FULL_TIME:
                        break;
                    case PART_TIME:
                        break;
                    case ON_DEMAND:
                        break;
                    case BY_APPOINTMENT:
                        break;
                    case NOT_AVAILABLE:
                        break;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        if (request.getUnit_count() != null) {
            if (request.getUnit_count() <= 0) {
                errorMessageUnit.setValue("Unit count must be greater than 0");
                isValid = false;
            }
        }
        return isValid;
    }


}
