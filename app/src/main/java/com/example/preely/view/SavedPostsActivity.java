package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.databinding.ActivitySavedPostsBinding;
import com.example.preely.databinding.ItemSavedPostBinding;
import com.example.preely.model.dto.SavedPostDTO;
import com.example.preely.model.entities.Post;
import com.example.preely.viewmodel.SavedPostService;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.preely.authentication.SessionManager;
import android.util.Log;

public class SavedPostsActivity extends AppCompatActivity {
    private ActivitySavedPostsBinding binding;
    private SavedPostService savedPostService;
    private SavedPostsAdapter adapter;
    private boolean isLoading = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(getApplicationContext());
        setupViews();
        setupViewModel();
        setupObservers();
        loadInitialData();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search coming soon", Toast.LENGTH_SHORT).show();
        });

        // Setup RecyclerView
        adapter = new SavedPostsAdapter(new ArrayList<>(), this::onSaveClick);
        binding.recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.recyclerView.setLayoutManager(layoutManager);

        // Setup scroll listener for pagination
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !savedPostService.isLastPage()) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreData();
                    }
                }
            }
        });

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void setupViewModel() {
        savedPostService = new ViewModelProvider(this).get(SavedPostService.class);
    }

    private void setupObservers() {
        // Observe saved posts
        savedPostService.getAllSavedPosts().observe(this, posts -> {
            Log.d("DEBUG", "[SavedPostsActivity] userId: " + getCurrentUserId());
            Log.d("DEBUG", "[SavedPostsActivity] Saved posts size: " + (posts == null ? "null" : posts.size()));
            if (posts != null && !posts.isEmpty()) {
                List<SavedPostDTO> dtoList = convertToDTO(posts);
                Log.d("DEBUG", "[SavedPostsActivity] DTO list size: " + (dtoList == null ? "null" : dtoList.size()));
                adapter.updateData(dtoList);
                showContent();
            } else {
                showEmptyState();
            }
        });

        // Observe loading state
        savedPostService.getIsLoading().observe(this, loading -> {
            isLoading = loading;
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Observe errors
        savedPostService.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe status messages
        savedPostService.getStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInitialData() {
        String userId = getCurrentUserId(); // TODO: Implement get current user ID
        savedPostService.getListSavedPosts(userId, true);
    }

    private void loadMoreData() {
        String userId = getCurrentUserId();
        savedPostService.getListSavedPosts(userId, false);
    }

    private void refreshData() {
        String userId = getCurrentUserId();
        savedPostService.getListSavedPosts(userId, true);
    }

    private void onSaveClick(SavedPostDTO post) {
        String userId = getCurrentUserId();
        if (post.isSaved()) {
            savedPostService.unsavePost(userId, post.getId());
        } else {
            savedPostService.savePost(userId, post.getId());
        }
    }

    private void showContent() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private String getCurrentUserId() {
        // Trả về userId đúng với bản ghi Firestore để test
        return "aK9Fun5gK5DDVYYpLpiU";
    }

    private List<SavedPostDTO> convertToDTO(List<Post> posts) {
        Log.d("DEBUG", "[SavedPostsActivity] convertToDTO input size: " + (posts == null ? "null" : posts.size()));
        List<SavedPostDTO> dtoList = new ArrayList<>();
        for (Post post : posts) {
            SavedPostDTO dto = new SavedPostDTO();
            dto.setId(post.getId().getId());
            dto.setTitle(post.getTitle());
//            try { dto.setImageUrl(post.getImageUrl()); } catch (Exception ignored) {}
            try { dto.setPrice(post.getPrice()); } catch (Exception ignored) {}
            dto.setSaved(true); // Since these are saved posts
            dtoList.add(dto);
        }
        Log.d("DEBUG", "[SavedPostsActivity] convertToDTO output size: " + dtoList.size());
        return dtoList;
    }

    // Adapter class
    private static class SavedPostsAdapter extends RecyclerView.Adapter<SavedPostsAdapter.ViewHolder> {
        private List<SavedPostDTO> posts;
        private final OnSaveClickListener saveClickListener;
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        public SavedPostsAdapter(List<SavedPostDTO> posts, OnSaveClickListener listener) {
            this.posts = posts;
            this.saveClickListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSavedPostBinding binding = ItemSavedPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SavedPostDTO post = posts.get(position);
            
            // Load image
//            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
//                Picasso.get()
//                        .load(post.getImageUrl())
//                        .placeholder(R.drawable.img)
//                        .error(R.drawable.img)
//                        .into(holder.binding.imageView);
//            }

            // Set text
            holder.binding.txtTitle.setText(post.getTitle());
            holder.binding.txtRating.setText(String.format(Locale.getDefault(), "%.1f", post.getRating()));
            holder.binding.txtRatingCount.setText(String.format(Locale.getDefault(), "(%d)", post.getRatingCount()));
            holder.binding.txtPrice.setText(currencyFormat.format(post.getPrice()));
            
            // Set rating
            holder.binding.ratingBar.setRating((float) post.getRating());

            // Set save button state and click listener
            holder.binding.btnSave.setImageResource(
                    post.isSaved() ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_add
            );
            holder.binding.btnSave.setOnClickListener(v -> {
                post.setSaved(!post.isSaved());
                notifyItemChanged(position);
                saveClickListener.onSaveClick(post);
            });
        }

        @Override
        public int getItemCount() {
            return posts != null ? posts.size() : 0;
        }

        public void updateData(List<SavedPostDTO> newPosts) {
            this.posts = newPosts;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemSavedPostBinding binding;

            ViewHolder(ItemSavedPostBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        interface OnSaveClickListener {
            void onSaveClick(SavedPostDTO post);
        }
    }
} 