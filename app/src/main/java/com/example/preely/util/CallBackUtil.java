package com.example.preely.util;

import com.google.firebase.firestore.DocumentReference;

public class CallBackUtil {
    public interface OnInsertCallback {
        void onSuccess(DocumentReference documentReference);
        void onFailure(Exception e);
    }

    public interface OnInsertManyCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnDeleteCallBack {
        void onSuccess();
        void onFailure(Exception e);
    }


}
