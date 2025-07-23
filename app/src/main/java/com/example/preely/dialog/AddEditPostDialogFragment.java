package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.preely.R;
import com.example.preely.model.entities.Post;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.preely.util.Constraints;
import com.example.preely.model.entities.User;
import com.example.preely.viewmodel.UserService;
import com.google.firebase.firestore.DocumentReference;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.model.response.TagResponse;
import com.example.preely.viewmodel.TagService;

import java.util.ArrayList;
import java.util.List;

public class AddEditPostDialogFragment extends DialogFragment {
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_PRICE = "arg_price";
    private static final String ARG_IMAGES = "arg_images";
    private static final String ARG_SELLER_ID = "arg_seller_id";
    private static final String ARG_CATEGORY_ID = "arg_category_id";
    private static final String ARG_TAG_IDS = "arg_tag_ids";
    private String postId;
    private String title;
    private String description;
    private Double price;
    private ArrayList<String> images;
    private boolean isEditMode;
    private String sellerId;
    private OnPostDialogListener listener;
    private String status;
    private DocumentReference seller_id;
    private AutoCompleteTextView actvCategory;
    private ArrayAdapter<String> categoryAdapter;
    private List<CategoryResponse> categoryList = new ArrayList<>();
    private CategoryService categoryService;
    private DocumentReference selectedCategoryRef;
    private String categoryId;

    private TextInputEditText etTitle, etDescription, etPrice;
    private Spinner spinnerStatus;
    private ArrayAdapter<String> statusAdapter;
    private MaterialButton btnSave, btnCancel, btnChooseImages;
    private RecyclerView rvImagePreview;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private CloudinaryService cloudinaryService;
    private List<String> uploadedImageUrls = new ArrayList<>();
    private boolean isUploadingImages = false;
    private Observer<List<String>> uploadUrlsObserver;
    private Observer<String> uploadErrorObserver;
    private AutoCompleteTextView actvSeller;
    private ArrayAdapter<String> sellerAdapter;
    private List<User> userList = new ArrayList<>();
    private UserService userService;
    private DocumentReference selectedSellerRef;
    private MultiAutoCompleteTextView mactvTags;
    private ArrayAdapter<String> tagAdapter;
    private List<TagResponse> tagList = new ArrayList<>();
    private TagService tagService;
    private List<String> selectedTagIds = new ArrayList<>();
    private List<String> tagIds;
    private TextView tvSellerValue, tvCategoryValue, tvTagsValue;

    public interface OnPostDialogListener {
        void onPostSaved(Post post, boolean isEdit);
    }

    public static AddEditPostDialogFragment newInstance(String postId, String title, String description, Double price, ArrayList<String> images, String status, String sellerId, String categoryId, ArrayList<String> tagIds) {
        AddEditPostDialogFragment fragment = new AddEditPostDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        if (price != null) args.putDouble(ARG_PRICE, price);
        if (images != null) args.putStringArrayList(ARG_IMAGES, images);
        if (status != null) args.putString("arg_status", status);
        if (sellerId != null) args.putString(ARG_SELLER_ID, sellerId);
        if (categoryId != null) args.putString(ARG_CATEGORY_ID, categoryId);
        if (tagIds != null) args.putStringArrayList(ARG_TAG_IDS, tagIds);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPostDialogListener(OnPostDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_edit_post, container, false);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            price = getArguments().containsKey(ARG_PRICE) ? getArguments().getDouble(ARG_PRICE) : null;
            images = getArguments().getStringArrayList(ARG_IMAGES);
            status = getArguments().getString("arg_status");
            sellerId = getArguments().getString(ARG_SELLER_ID);
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            tagIds = getArguments().getStringArrayList(ARG_TAG_IDS);
            isEditMode = postId != null;
        }
        cloudinaryService = new CloudinaryService(requireActivity().getApplication());
        initViews(view);
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
        return view;
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        btnSave = view.findViewById(R.id.btn_save);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{
                Constraints.PostStatus.CLAIMED,
                Constraints.PostStatus.COMPLETED,
                Constraints.PostStatus.CANCELLED,
                Constraints.PostStatus.HIDDEN
        });
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        updateSaveButtonState();
        btnCancel = view.findViewById(R.id.btn_cancel);
        rvImagePreview = view.findViewById(R.id.rv_image_preview);
        btnChooseImages = view.findViewById(R.id.btn_choose_images);
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImagePreview.setAdapter(imagePreviewAdapter);
        rvImagePreview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(isEditMode ? "Edit Post" : "Add New Post");
        actvSeller = view.findViewById(R.id.actv_seller);
        userService = new UserService();
        loadUsersForSeller();
        actvCategory = view.findViewById(R.id.actv_category);
        categoryService = new CategoryService();
        loadCategoriesForCategory();
        mactvTags = view.findViewById(R.id.mactv_tags);
        tagService = new TagService();
        loadTagsForTags();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> savePost());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());
    }

    private void populateFields() {
        etTitle.setText(title != null ? title : "");
        etDescription.setText(description != null ? description : "");
        if (price != null) {
            etPrice.setText(String.valueOf(price));
        }
        // Set status nếu có
        if (status != null) {
            int pos = statusAdapter.getPosition(status);
            if (pos >= 0) spinnerStatus.setSelection(pos);
        }
        // Preview các ảnh cũ nếu có và chưa chọn ảnh mới
        if ((selectedImageUris == null || selectedImageUris.isEmpty()) && images != null && !images.isEmpty()) {
            selectedImageUris.clear();
            for (String url : images) {
                // Dùng Uri.parse để preview ảnh cũ dạng URL
                selectedImageUris.add(Uri.parse(url));
            }
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void chooseImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 2001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == android.app.Activity.RESULT_OK && data != null) {
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

    public void onImagesSelected(List<Uri> uris) {
        Log.d("AddEditPostDialogFragment", "onImagesSelected called, uris size: " + (uris == null ? 0 : uris.size()));
        selectedImageUris.clear(); // Ghi đè ảnh cũ
        uploadedImageUrls.clear();
        if (uris != null) {
            selectedImageUris.addAll(uris);
        }
        imagePreviewAdapter.notifyDataSetChanged();
        if (!selectedImageUris.isEmpty()) {
            isUploadingImages = true;
            btnSave.setEnabled(false);
            updateSaveButtonState();
            if (uploadUrlsObserver != null) {
                cloudinaryService.getUploadedUrls().removeObserver(uploadUrlsObserver);
            }
            if (uploadErrorObserver != null) {
                cloudinaryService.getErrorMessage().removeObserver(uploadErrorObserver);
            }
            Log.d("AddEditPostDialogFragment", "Trigger uploadAllImagesToCloudinary, selectedImageUris size: " + selectedImageUris.size());
            uploadAllImagesToCloudinary();
        }
    }

    private void uploadAllImagesToCloudinary() {
        uploadedImageUrls.clear();
        cloudinaryService.clearUploadedUrls();
        cloudinaryService.uploadMultipleImages(selectedImageUris, "posts");
        uploadUrlsObserver = urls -> {
            if (urls != null && urls.size() == selectedImageUris.size()) {
                uploadedImageUrls.clear();
                uploadedImageUrls.addAll(urls);
                isUploadingImages = false;
                btnSave.setEnabled(true);
                updateSaveButtonState();
            }
        };
        cloudinaryService.getUploadedUrls().observe(getViewLifecycleOwner(), uploadUrlsObserver);
        uploadErrorObserver = errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                isUploadingImages = false;
                btnSave.setEnabled(false);
                updateSaveButtonState();
                Toast.makeText(getContext(), "Lỗi upload ảnh: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        cloudinaryService.getErrorMessage().observe(getViewLifecycleOwner(), uploadErrorObserver);
    }

    private void savePost() {
        if (isUploadingImages) {
            Toast.makeText(getContext(), "Please wait for images upload to finish!", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(false);
            updateSaveButtonState();
            return;
        }
        String titleStr = etTitle.getText().toString().trim();
        String descriptionStr = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        Double priceVal = null;
        if (!priceStr.isEmpty()) {
            try {
                priceVal = Double.parseDouble(priceStr);
                if (priceVal < 0) {
                    etPrice.setError("Price must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                return;
            }
        }
        if (titleStr.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (descriptionStr.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        Post postToSave = new Post();
        // Nếu user chưa chọn lại thì giữ sellerRef cũ
        if (selectedSellerRef == null && sellerId != null) {
            selectedSellerRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(sellerId);
        }
        // Đảm bảo seller_id và category_id là DocumentReference
        if (selectedSellerRef != null) {
            postToSave.setSeller_id(selectedSellerRef);
        } else if (actvSeller.getText() != null && !actvSeller.getText().toString().isEmpty()) {
            // Nếu user chọn text nhưng chưa chọn lại từ dropdown, tìm DocumentReference tương ứng
            for (User u : userList) {
                if (u.getFull_name().equals(actvSeller.getText().toString())) {
                    postToSave.setSeller_id(com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(u.getId().getId()));
                    break;
                }
            }
        }
        if (selectedCategoryRef != null) {
            postToSave.setCategory_id(selectedCategoryRef);
        } else if (actvCategory.getText() != null && !actvCategory.getText().toString().isEmpty()) {
            for (CategoryResponse c : categoryList) {
                if (c.getName().equals(actvCategory.getText().toString())) {
                    postToSave.setCategory_id(com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("category").document(c.getId().getId()));
                    break;
                }
            }
        }
        postToSave.setTag_ids(getSelectedTagIdsFromInput());
        postToSave.setCurrency("$");
        postToSave.setCreate_at(postId == null ? com.google.firebase.Timestamp.now() : null); // chỉ set khi tạo mới
        postToSave.setUpdate_at(com.google.firebase.Timestamp.now());
        postToSave.setTitle(titleStr);
        postToSave.setDescription(descriptionStr);
        postToSave.setPrice(priceVal);
        postToSave.setImages(new ArrayList<>(uploadedImageUrls));
        postToSave.setStatus(spinnerStatus.getSelectedItem().toString());
        if (listener != null) {
            listener.onPostSaved(postToSave, isEditMode);
        }
        dismiss();
    }

    public List<Uri> getSelectedImageUris() {
        return selectedImageUris;
    }

    public List<String> getUploadedImageUrls() {
        return uploadedImageUrls;
    }

    private void updateSaveButtonState() {
        if (btnSave != null) {
            btnSave.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_save_selector));
        }
    }

    private void loadUsersForSeller() {
        com.google.firebase.firestore.Query query = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user");
        new com.example.preely.repository.MainRepository<>(User.class, "user").getAll(query).observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userList.clear();
                userList.addAll(users);
                List<String> displayNames = new ArrayList<>();
                for (User u : users) {
                    displayNames.add(u.getFull_name() + " (" + u.getEmail() + ")");
                }
                sellerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayNames);
                actvSeller.setAdapter(sellerAdapter);
                // Nếu đang edit, set seller hiện tại
                if (isEditMode && sellerId != null) {
                    for (int i = 0; i < users.size(); i++) {
                        if (users.get(i).getId() != null && users.get(i).getId().getId().equals(sellerId)) {
                            actvSeller.setText(displayNames.get(i), false);
                            selectedSellerRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(sellerId);
                            break;
                        }
                        // Nếu sellerId truyền vào là dạng '/user/ID', tách lấy ID để so sánh
                        else if (users.get(i).getId() != null && ("/user/" + users.get(i).getId().getId()).equals(sellerId)) {
                            actvSeller.setText(displayNames.get(i), false);
                            selectedSellerRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(users.get(i).getId().getId());
                            break;
                        }
                    }
                }
            }
        });
        actvSeller.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = sellerAdapter.getItem(position);
            for (User u : userList) {
                String displayName = u.getFull_name() + " (" + u.getEmail() + ")";
                if (displayName.equals(selectedText)) {
                    selectedSellerRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(u.getId().getId());
                    if (tvSellerValue != null) tvSellerValue.setText(displayName);
                    break;
                }
            }
        });
        actvSeller.setOnClickListener(v -> actvSeller.showDropDown());
    }

    private void loadCategoriesForCategory() {
        categoryService.getCateList();
        categoryService.getCateListResult().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryList.clear();
                categoryList.addAll(categories);
                List<String> displayNames = new ArrayList<>();
                for (CategoryResponse c : categories) {
                    displayNames.add(c.getName());
                }
                categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayNames);
                actvCategory.setAdapter(categoryAdapter);
                // Nếu đang edit, set category hiện tại
                if (isEditMode && categoryId != null) {
                    Log.d("CategoryDebug", "categoryId=" + categoryId);
                    for (CategoryResponse c : categories) {
                        Log.d("CategoryDebug", "cat: " + c.getName() + " id=" + (c.getId() != null ? c.getId().getId() : "null") + " path=" + (c.getId() != null ? c.getId().getPath() : "null"));
                    }
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId() == null) continue;
                        if (categories.get(i).getId().getId().equals(categoryId)) {
                            Log.d("CategoryDebug", "MATCH BY ID: " + categories.get(i).getName());
                            actvCategory.setText(displayNames.get(i), false);
                            selectedCategoryRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("category").document(categoryId);
                            break;
                        }
                        // Nếu categoryId truyền vào là dạng 'category/ID', tách lấy ID để so sánh
                        else if (("category/" + categories.get(i).getId().getId()).equals(categoryId)) {
                            Log.d("CategoryDebug", "MATCH BY PATH: " + categories.get(i).getName());
                            actvCategory.setText(displayNames.get(i), false);
                            selectedCategoryRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("category").document(categories.get(i).getId().getId());
                            break;
                        }
                    }
                }
            }
        });
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = categoryAdapter.getItem(position);
            for (CategoryResponse c : categoryList) {
                if (c.getName().equals(selectedText)) {
                    selectedCategoryRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("category").document(c.getId().getId());
                    if (tvCategoryValue != null) tvCategoryValue.setText(selectedText);
                    break;
                }
            }
        });
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
    }

    private void loadTagsForTags() {
        tagService.getAllTag();
        tagService.getTagListResult().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                tagList.clear();
                tagList.addAll(tags);
                List<String> displayNames = new ArrayList<>();
                for (TagResponse t : tags) {
                    displayNames.add(t.getName());
                }
                tagAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayNames);
                mactvTags.setAdapter(tagAdapter);
                mactvTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                // Nếu đang edit, set các tag hiện tại
                if (isEditMode && tagIds != null && !tagIds.isEmpty()) {
                    List<String> selectedNames = new ArrayList<>();
                    for (String tagId : tagIds) {
                        for (TagResponse t : tags) {
                            if (t.getId() != null && t.getId().getId().equals(tagId)) {
                                selectedNames.add(t.getName());
                                break;
                            }
                            // Nếu tagId truyền vào là dạng '/tag/ID', tách lấy ID để so sánh
                            else if (t.getId() != null && ("/tag/" + t.getId().getId()).equals(tagId)) {
                                selectedNames.add(t.getName());
                                break;
                            }
                        }
                    }
                    mactvTags.setText(android.text.TextUtils.join(", ", selectedNames));
                    selectedTagIds.clear();
                    selectedTagIds.addAll(tagIds);
                }
            }
        });
        mactvTags.setOnItemClickListener((parent, view, position, id) -> {
            // Khi chọn tag, cập nhật selectedTagIds
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
        });
        mactvTags.setOnClickListener(v -> mactvTags.showDropDown());
    }

    private List<String> getSelectedTagIdsFromInput() {
        List<String> ids = new ArrayList<>();
        String[] selectedNames = mactvTags.getText().toString().split(",");
        for (String name : selectedNames) {
            String trimmed = name.trim();
            for (TagResponse t : tagList) {
                if (t.getName().equals(trimmed)) {
                    ids.add(t.getId().getId());
                    break;
                }
            }
        }
        return ids;
    }
} 