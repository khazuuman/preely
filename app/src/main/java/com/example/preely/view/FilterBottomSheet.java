package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.CategoryFilterAdapter;
import com.example.preely.adapter.SortFilterAdapter;
import com.example.preely.model.request.CategoryFilterRequest;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.request.SortFilterRequest;
import com.example.preely.model.response.CategoryResponse;
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
    RecyclerView cateRecycleView, tagRecycleView, sortRecycleView;
    ProgressBar progressBar;
    LinearLayout mainLayout;
    private final List<CategoryFilterRequest> categoryList = new ArrayList<>();
    private final List<SortFilterRequest> sortList = new ArrayList<>(
            Arrays.asList(
                    new SortFilterRequest(0, "Most View", false),
                    new SortFilterRequest(2, "Newest", false),
                    new SortFilterRequest(1, "Oldest", false)
            )
    );
    private CategoryFilterAdapter categoryFilterAdapter;
    private CategoryService categoryService;
    private SortFilterAdapter sortFilterAdapter;
    List<String> category_id;
    Integer sortType;
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

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        mainLayout = view.findViewById(R.id.mainLayout);
        mainLayout.setVisibility(View.GONE);
        applyBtn = view.findViewById(R.id.apply_btn);
        resetBtn = view.findViewById(R.id.reset_btn);
        cateRecycleView = view.findViewById(R.id.cate_recycle_view);
        tagRecycleView = view.findViewById(R.id.tag_recycle_view);
        sortRecycleView = view.findViewById(R.id.sort_recycle_view);

        // category list
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        observeCategoryList();
        if (categoryList.isEmpty()) {
            categoryService.getCateList();
        }
        cateRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryFilterAdapter = new CategoryFilterAdapter(categoryList);
        categoryFilterAdapter.setRecyclerView(cateRecycleView);
        cateRecycleView.setAdapter(categoryFilterAdapter);

        // sort list
        sortRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        sortFilterAdapter = new SortFilterAdapter(sortList);
        sortRecycleView.setAdapter(sortFilterAdapter);

        applyBtn.setOnClickListener(v -> {
            category_id = categoryFilterAdapter.getIdSelectedItems();
            sortType = sortFilterAdapter.getSelectedItem();
            Log.i("CATE_ID", category_id == null ? "null" : category_id.toString());
            Log.i("SORT_TYPE", sortType == null ? "null" : sortType.toString());
//            serviceFilterRequest = new ServiceFilterRequest(null, category_id, sortType);
            if (filterApplyListener != null) {
                filterApplyListener.onFilterApplied(serviceFilterRequest);
            }
            dismiss();
        });

        resetBtn.setOnClickListener(v -> {
            for (CategoryFilterRequest category : categoryList) {
                category.setChecked(false);
            }
            categoryFilterAdapter.notifyDataSetChanged();

            for (SortFilterRequest sort : sortList) {
                sort.setChecked(false);
            }
            sortFilterAdapter.notifyDataSetChanged();
            category_id = null;
            sortType = null;
        });

        return view;
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
