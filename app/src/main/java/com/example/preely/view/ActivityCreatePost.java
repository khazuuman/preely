package com.example.preely.view;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.TagResponse;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.viewmodel.TagService;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.dialog.ImagePreviewAdapter;
import androidx.lifecycle.ViewModelProvider;
import com.example.preely.viewmodel.CloudinaryService;
import android.util.Log;

public class ActivityCreatePost extends AppCompatActivity {
    private TextInputLayout titleTil, locationTil;
    private EditText titleInput, locationInput;
    private AutoCompleteTextView actvCategory;
    private ArrayAdapter<String> categoryAdapter;
    private List<CategoryResponse> categoryList = new ArrayList<>();
    private CategoryService categoryService;
    private String selectedCategoryId;
    private MultiAutoCompleteTextView mactvTags;
    private ArrayAdapter<String> tagAdapter;
    private List<TagResponse> tagList = new ArrayList<>();
    private TagService tagService;
    private List<String> selectedTagIds = new ArrayList<>();
    private Button btnChooseImages;
    private RecyclerView rvImagePreview;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<String> uploadedImageUrls = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 2001;
    private CloudinaryService cloudinaryService;
    private boolean isUploadingImages = false;
    private Button createPostBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Ánh xạ view
        titleTil = findViewById(R.id.title_til);
        locationTil = findViewById(R.id.location_til);

        titleInput = findViewById(R.id.title_input);
        locationInput = findViewById(R.id.location_input);

        actvCategory = findViewById(R.id.actv_category);
        mactvTags = findViewById(R.id.mactv_tags);
        btnChooseImages = findViewById(R.id.btn_choose_images);
        rvImagePreview = findViewById(R.id.rv_image_preview);
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImagePreview.setAdapter(imagePreviewAdapter);
        rvImagePreview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        createPostBtn = findViewById(R.id.createPost);

        // TODO: Load category vào spinner nếu cần
        // loadCategories();

        // Load category/tags
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        tagService = new TagService();
        loadCategories();
        loadTags();
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = categoryList.get(position).getId().getId();
        });
        mactvTags.setOnItemClickListener((parent, view, position, id) -> {
            updateSelectedTagIds();
        });
        mactvTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mactvTags.setOnClickListener(v -> mactvTags.showDropDown());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());

        createPostBtn.setOnClickListener(v -> {
            if (validateInput()) {
                createPost();
            }
        });
        cloudinaryService = new ViewModelProvider(this).get(CloudinaryService.class);
    }

    private boolean validateInput() {
        boolean valid = true;
        if (titleInput.getText().toString().trim().isEmpty()) {
            titleTil.setError("Title is required");
            valid = false;
        } else {
            titleTil.setError(null);
        }
        if (locationInput.getText().toString().trim().isEmpty()) {
            locationTil.setError("Location is required");
            valid = false;
        } else {
            locationTil.setError(null);
        }
        return valid;
    }

    private void createPost() {
        if (isUploadingImages) {
            Toast.makeText(this, "Vui lòng chờ upload ảnh xong!", Toast.LENGTH_SHORT).show();
            createPostBtn.setEnabled(false);
            return;
        }
        String title = titleInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        Log.d("CreatePost", "Bấm tạo post với title=" + title + ", location=" + location + ", categoryId=" + selectedCategoryId + ", tags=" + selectedTagIds + ", images=" + uploadedImageUrls);
        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("location", location);
        post.put("category", selectedCategoryId);
        post.put("tags", new ArrayList<>(selectedTagIds));
        post.put("images", new ArrayList<>(uploadedImageUrls));
        post.put("create_at", System.currentTimeMillis());
        Log.d("CreatePost", "Dữ liệu gửi lên Firestore: " + post);
        FirebaseFirestore.getInstance()
                .collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.i("CreatePost", "Tạo bài viết thành công với id=" + documentReference.getId());
                    Toast.makeText(this, "Tạo bài viết thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreatePost", "Lỗi tạo bài viết: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCategories() {
        Log.d("CreatePost", "Bắt đầu load category");
        categoryService.getCateList();
        categoryService.getCateListResult().observe(this, categories -> {
            Log.d("CreatePost", "categoryService.getCateListResult() trả về: " + (categories == null ? "null" : ("size=" + categories.size())));
            if (categories != null) {
                categoryList.clear();
                categoryList.addAll(categories);
                List<String> displayNames = new ArrayList<>();
                for (CategoryResponse c : categories) {
                    displayNames.add(c.getName());
                }
                categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayNames);
                actvCategory.setAdapter(categoryAdapter);
                Log.d("CreatePost", "Đã load xong category: " + displayNames);
            } else {
                Log.e("CreatePost", "Không load được category (categories == null)");
            }
        });
    }

    private void loadTags() {
        tagService.getAllTag();
        tagService.getTagListResult().observe(this, tags -> {
            if (tags != null) {
                tagList.clear();
                tagList.addAll(tags);
                List<String> displayNames = new ArrayList<>();
                for (TagResponse t : tags) {
                    displayNames.add(t.getName());
                }
                tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayNames);
                mactvTags.setAdapter(tagAdapter);
            }
        });
    }

    private void updateSelectedTagIds() {
        String[] selectedNames = mactvTags.getText().toString().split(",");
        selectedTagIds.clear();
        for (String name : selectedNames) {
            String trimmed = name.trim();
            for (TagResponse t : tagList) {
                if (t.getName().equals(trimmed)) {
                    selectedTagIds.add(t.getId().getId());
                    break;
                }
            }
        }
    }

    private void chooseImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                uris.add(data.getData());
            }
            onImagesSelected(uris);
        }
    }

    private void onImagesSelected(List<Uri> uris) {
        selectedImageUris.clear();
        uploadedImageUrls.clear();
        if (uris != null) {
            selectedImageUris.addAll(uris);
        }
        imagePreviewAdapter.notifyDataSetChanged();
        if (!selectedImageUris.isEmpty()) {
            isUploadingImages = true;
            createPostBtn.setEnabled(false);
            cloudinaryService.clearUploadedUrls();
            cloudinaryService.uploadMultipleImages(selectedImageUris, "posts");
            cloudinaryService.getUploadedUrls().observe(this, urls -> {
                if (urls != null && urls.size() == selectedImageUris.size()) {
                    uploadedImageUrls.clear();
                    uploadedImageUrls.addAll(urls);
                    isUploadingImages = false;
                    createPostBtn.setEnabled(true);
                }
            });
            cloudinaryService.getErrorMessage().observe(this, errorMsg -> {
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    isUploadingImages = false;
                    createPostBtn.setEnabled(true);
                    Toast.makeText(this, "Lỗi upload ảnh: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
} 