package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.AvailableFilterAdapter;
import com.example.preely.adapter.CategoryFilterAdapter;
import com.example.preely.adapter.SortFilterAdapter;
import com.example.preely.model.request.AvailableFilterRequest;
import com.example.preely.model.request.CategoryFilterRequest;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.request.SortFilterRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CategoryService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    Button applyBtn, resetBtn;
    RecyclerView cateRecycleView, sortRecycleView, availRecycleView;
    ImageView[] stars = new ImageView[5];
    ProgressBar progressBar;
    LinearLayout mainLayout;
    private List<CategoryFilterRequest> categoryList = new ArrayList<>();
    private List<SortFilterRequest> sortList = new ArrayList<>(
            Arrays.asList(
                    new SortFilterRequest(2, "Newest", false),
                    new SortFilterRequest(1, "Oldest", false),
                    new SortFilterRequest(3, "Most Review", false),
                    new SortFilterRequest(4, "Low to High", false),
                    new SortFilterRequest(5, "High to Low", false)
            )
    );
    private final List<AvailableFilterRequest> availList = new ArrayList<>();
    private CategoryFilterAdapter categoryFilterAdapter;
    private AvailableFilterAdapter availableFilterAdapter;
    private CategoryService categoryService;
    private SortFilterAdapter sortFilterAdapter;
    List<DocumentReference> categoryIdRefs;
    Integer sortType;
    Integer rating;
    ServiceFilterRequest serviceFilterRequest;
    private OnFilterApplyListener filterApplyListener;
    private boolean categoryLoaded = false;

    public interface OnFilterApplyListener {
        void onFilterApplied(ServiceFilterRequest filterRequest);
    }

    public void setOnFilterApplyListener(OnFilterApplyListener listener) {
        this.filterApplyListener = listener;
    }

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_filter, container, false);

        initializeComponents();
        setupView(view);
        setupAvailable();
        setRating();
        setupCategory();
        setupSort();
        setApplyBtn();
        setResetBtn();

        return view;
    }

    public void initializeComponents() {
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
    }

    public void setApplyBtn() {
        applyBtn.setOnClickListener(v -> {
            categoryIdRefs = categoryFilterAdapter.getIdSelectedItems();
            sortType = sortFilterAdapter.getSelectedItem();
            List<String> availStrList = new ArrayList<>();
            for (AvailableFilterRequest avail : availList) {
                if (avail.isChecked()) {
                    availStrList.add(avail.getEnumName());
                }
            }
            Float ratingValue = (rating != null) ? Float.valueOf(rating) : null;
            serviceFilterRequest = new ServiceFilterRequest(null, categoryIdRefs, sortType, ratingValue, availStrList);
            Log.i("FILTER REQUEST", serviceFilterRequest.toString());
            if (filterApplyListener != null) {
                filterApplyListener.onFilterApplied(serviceFilterRequest);
            }
            dismiss();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setResetBtn() {
        resetBtn.setOnClickListener(v -> {
            for (CategoryFilterRequest category : categoryList) {
                category.setChecked(false);
            }
            categoryFilterAdapter.notifyDataSetChanged();

            for (SortFilterRequest sort : sortList) {
                sort.setChecked(false);
            }
            sortFilterAdapter.notifyDataSetChanged();

            for (AvailableFilterRequest avail : availList) {
                avail.setChecked(false);
            }
            availableFilterAdapter.notifyDataSetChanged();
            rating = null;
            categoryIdRefs = null;
            sortType = null;
            updateRating(0);
        });
    }

    public void setupView(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        mainLayout = view.findViewById(R.id.mainLayout);
        mainLayout.setVisibility(View.GONE);
        applyBtn = view.findViewById(R.id.apply_btn);
        resetBtn = view.findViewById(R.id.reset_btn);
        cateRecycleView = view.findViewById(R.id.cate_recycle_view);
        sortRecycleView = view.findViewById(R.id.sort_recycle_view);
        availRecycleView = view.findViewById(R.id.avail_recycle_view);
        stars[0] = view.findViewById(R.id.star1);
        stars[1] = view.findViewById(R.id.star2);
        stars[2] = view.findViewById(R.id.star3);
        stars[3] = view.findViewById(R.id.star4);
        stars[4] = view.findViewById(R.id.star5);
    }

    public void setRating() {
        if (rating != null) {
            updateRating(rating);
        }
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                rating = index + 1;
                updateRating(rating);
            });
        }
    }

    private void updateRating(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_rating_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_rating_outline);
            }
        }
    }

    private void setupCategory() {
        observeCategoryList();
        categoryService.getCateList();
        cateRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryFilterAdapter = new CategoryFilterAdapter(categoryList);
        categoryFilterAdapter.setRecyclerView(cateRecycleView);
        cateRecycleView.setAdapter(categoryFilterAdapter);
    }

    private void setupSort() {
        sortRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        sortFilterAdapter = new SortFilterAdapter(sortList);
        sortRecycleView.setAdapter(sortFilterAdapter);
    }

    private void setupAvailable() {
        if (availList.isEmpty()) {
            for (Constraints.Availability availability : Constraints.Availability.values()) {
                AvailableFilterRequest filter = new AvailableFilterRequest();
                filter.setEnumName(availability.name());
                filter.setName(availability.getLabel());
                filter.setChecked(false);
                availList.add(filter);
            }
        }
        availRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        availableFilterAdapter = new AvailableFilterAdapter(availList);
        availRecycleView.setAdapter(availableFilterAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.7);
            view.setLayoutParams(params);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeCategoryList() {
        categoryService.getCateListResult().observe(this, categoryResponses -> {
            if (categoryResponses != null) {
                Map<String, Boolean> previousCheckedMap = new HashMap<>();
                for (CategoryFilterRequest item : categoryList) {
                    if (item.getId() != null) {
                        previousCheckedMap.put(item.getId(), item.isChecked());
                    } else {
                        previousCheckedMap.put("ALL", item.isChecked());
                    }
                }
                categoryList.clear();
                categoryList.add(new CategoryFilterRequest(null, "All", previousCheckedMap.getOrDefault("ALL", false)));

                for (CategoryResponse categoryResponse : categoryResponses) {
                    boolean isChecked = previousCheckedMap.getOrDefault(categoryResponse.getId(), false);
                    categoryList.add(new CategoryFilterRequest(categoryResponse.getId(), categoryResponse.getName(), isChecked));
                }
                categoryFilterAdapter.notifyDataSetChanged();
            }
            categoryLoaded = true;
            checkAllDataLoaded();
        });
    }

    private void checkAllDataLoaded() {
        if (categoryLoaded) {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

}
