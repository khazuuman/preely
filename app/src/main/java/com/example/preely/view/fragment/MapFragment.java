package com.example.preely.view.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.preely.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private GeoPoint initialLocation;
    private LatLng pickedLatLng;
    private boolean pickEnabled = false;
    private OnLocationPickedListener listener;

    public interface OnLocationPickedListener {
        void onLocationPicked(GeoPoint newLocation);
    }

    public static MapFragment newInstance(GeoPoint location, boolean pickEnabled) {
        MapFragment frag = new MapFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", location != null ? location.getLatitude() : 0);
        args.putDouble("lng", location != null ? location.getLongitude() : 0);
        args.putBoolean("pickEnabled", pickEnabled);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle a = getArguments();
        if (a != null) {
            initialLocation = new GeoPoint(a.getDouble("lat"), a.getDouble("lng"));
            pickEnabled = a.getBoolean("pickEnabled", false);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFrag = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.support_map_fragment);
        mapFrag.getMapAsync(this);
        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng center = initialLocation != null
                ? new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude())
                : new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));

        if (initialLocation != null) {
            mMap.addMarker(new MarkerOptions().position(center).title("Hiện tại"));
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (pickEnabled) {
            mMap.setOnMapClickListener(latlng -> {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latlng));
                pickedLatLng = latlng;
                if (listener != null) {
                    listener.onLocationPicked(
                            new GeoPoint(latlng.latitude, latlng.longitude)
                    );
                }
            });
        }
    }

    public void setOnLocationPickedListener(OnLocationPickedListener l) {
        this.listener = l;
    }
}
