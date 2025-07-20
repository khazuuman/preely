package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.CategoryFilterAdapter;
import com.example.preely.model.request.CategoryFilterRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.viewmodel.CategoryService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    Button applyBtn, resetBtn;
    RecyclerView cateRecycleView, tagRecycleView, sortRecycleView;
    private final List<CategoryFilterRequest> categoryList = new ArrayList<>();
    private List<String> tagList;
    private List<String> sortList;
    private CategoryFilterAdapter categoryFilterAdapter;
    private CategoryService categoryService;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_filter, container, false);

        applyBtn = view.findViewById(R.id.apply_btn);
        resetBtn = view.findViewById(R.id.reset_btn);
        cateRecycleView = view.findViewById(R.id.cate_recycle_view);
        tagRecycleView = view.findViewById(R.id.tag_recycle_view);
        sortRecycleView = view.findViewById(R.id.sort_recycle_view);

        // category list
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        observeCategoryList();
        categoryService.getCateList();
        cateRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryFilterAdapter = new CategoryFilterAdapter(categoryList);
        cateRecycleView.setAdapter(categoryFilterAdapter);

        applyBtn.setOnClickListener(v -> {


            // Gửi kết quả về Activity nếu muốn
            dismiss();
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
                categoryList.clear();
                for (CategoryResponse categoryResponse : categoryResponses) {
                    categoryList.add(new CategoryFilterRequest(categoryResponse.getId(), categoryResponse.getName(), false));
                }
                categoryFilterAdapter.notifyDataSetChanged();
            }
        });
    }

}
