package com.example.preely.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.adapter.CategoryAdapter;
import com.example.preely.adapter.ImageAdapter;
import com.example.preely.adapter.PostAdapter;
import com.example.preely.adapter.TagAdapter;
import com.example.preely.adapter.TransactionAdapter;
import com.example.preely.adapter.UserAdapter;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Image;
import com.example.preely.model.entities.Post;
import com.example.preely.model.entities.Tag;
import com.example.preely.model.entities.Transaction;
import com.example.preely.model.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFilterUtil {

    public interface SearchFilterCallback<T> {
        void onFiltered(List<T> filteredList);
    }

    public static void setupUserSearch(EditText searchEditText, List<User> originalList, 
                                     UserAdapter adapter, SearchFilterCallback<User> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<User> filteredList = originalList.stream()
                    .filter(user -> user.getFull_name() != null && 
                                  user.getFull_name().toLowerCase().contains(query) ||
                                  user.getEmail() != null && 
                                  user.getEmail().toLowerCase().contains(query) ||
                                  user.getUsername() != null && 
                                  user.getUsername().toLowerCase().contains(query) ||
                                  user.getPhone_number() != null && 
                                  user.getPhone_number().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setUserList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupPostSearch(EditText searchEditText, List<Post> originalList, 
                                     PostAdapter adapter, SearchFilterCallback<Post> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Post> filteredList = originalList.stream()
                    .filter(post -> post.getTitle() != null && 
                                  post.getTitle().toLowerCase().contains(query) ||
                                  post.getDescription() != null && 
                                  post.getDescription().toLowerCase().contains(query) ||
                                  post.getStatus() != null && 
                                  post.getStatus().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setPostList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupTransactionSearch(EditText searchEditText, List<Transaction> originalList, 
                                            TransactionAdapter adapter, SearchFilterCallback<Transaction> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Transaction> filteredList = originalList.stream()
                    .filter(transaction -> transaction.getId() != null && 
                                         transaction.getId().toLowerCase().contains(query) ||
                                         transaction.getStatus() != null && 
                                         transaction.getStatus().toLowerCase().contains(query) ||
                                         transaction.getGiver_id() != null && 
                                         transaction.getGiver_id().toLowerCase().contains(query) ||
                                         transaction.getRequester_id() != null && 
                                         transaction.getRequester_id().toLowerCase().contains(query) ||
                                         transaction.getPost_id() != null && 
                                         transaction.getPost_id().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setTransactionList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupCategorySearch(EditText searchEditText, List<Category> originalList, 
                                         CategoryAdapter adapter, SearchFilterCallback<Category> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Category> filteredList = originalList.stream()
                    .filter(category -> category.getName() != null && 
                                      category.getName().toLowerCase().contains(query) ||
                                      category.getParent_category_id() != null && 
                                      category.getParent_category_id().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setCategoryList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupTagSearch(EditText searchEditText, List<Tag> originalList, 
                                    TagAdapter adapter, SearchFilterCallback<Tag> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Tag> filteredList = originalList.stream()
                    .filter(tag -> tag.getName() != null && 
                                 tag.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setTagList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupImageSearch(EditText searchEditText, List<Image> originalList, 
                                      ImageAdapter adapter, SearchFilterCallback<Image> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Image> filteredList = originalList.stream()
                    .filter(image -> image.getPost_id() != null && 
                                   image.getPost_id().toLowerCase().contains(query) ||
                                   image.getLink() != null && 
                                   image.getLink().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setImageList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Filter by status for transactions
    public static List<Transaction> filterTransactionsByStatus(List<Transaction> originalList, String status) {
        if (status == null || status.isEmpty() || status.equalsIgnoreCase("All")) {
            return originalList;
        }
        return originalList.stream()
            .filter(transaction -> transaction.getStatus() != null && 
                                 transaction.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    // Filter by price range for posts
    public static List<Post> filterPostsByPriceRange(List<Post> originalList, double minPrice, double maxPrice) {
        return originalList.stream()
            .filter(post -> post.getPrice() != null && 
                          post.getPrice() >= minPrice && 
                          post.getPrice() <= maxPrice)
            .collect(Collectors.toList());
    }

    // Filter by category for posts
    public static List<Post> filterPostsByCategory(List<Post> originalList, String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return originalList;
        }
        return originalList.stream()
            .filter(post -> post.getCategoryId() != null && 
                          post.getCategoryId().equals(categoryId))
            .collect(Collectors.toList());
    }

    // Sort functions
    public static List<User> sortUsersByName(List<User> users, boolean ascending) {
        List<User> sortedList = new ArrayList<>(users);
        if (ascending) {
            sortedList.sort((u1, u2) -> {
                String name1 = u1.getFull_name() != null ? u1.getFull_name() : "";
                String name2 = u2.getFull_name() != null ? u2.getFull_name() : "";
                return name1.compareToIgnoreCase(name2);
            });
        } else {
            sortedList.sort((u1, u2) -> {
                String name1 = u1.getFull_name() != null ? u1.getFull_name() : "";
                String name2 = u2.getFull_name() != null ? u2.getFull_name() : "";
                return name2.compareToIgnoreCase(name1);
            });
        }
        return sortedList;
    }

    public static List<Post> sortPostsByDate(List<Post> posts, boolean ascending) {
        List<Post> sortedList = new ArrayList<>(posts);
        if (ascending) {
            sortedList.sort((p1, p2) -> {
                String date1 = p1.getCreate_at() != null ? p1.getCreate_at().toString() : "";
                String date2 = p2.getCreate_at() != null ? p2.getCreate_at().toString() : "";
                return date1.compareTo(date2);
            });
        } else {
            sortedList.sort((p1, p2) -> {
                String date1 = p1.getCreate_at() != null ? p1.getCreate_at().toString() : "";
                String date2 = p2.getCreate_at() != null ? p2.getCreate_at().toString() : "";
                return date2.compareTo(date1);
            });
        }
        return sortedList;
    }

    public static List<Post> sortPostsByPrice(List<Post> posts, boolean ascending) {
        List<Post> sortedList = new ArrayList<>(posts);
        if (ascending) {
            sortedList.sort((p1, p2) -> {
                Double price1 = p1.getPrice() != null ? p1.getPrice() : 0.0;
                Double price2 = p2.getPrice() != null ? p2.getPrice() : 0.0;
                return price1.compareTo(price2);
            });
        } else {
            sortedList.sort((p1, p2) -> {
                Double price1 = p1.getPrice() != null ? p1.getPrice() : 0.0;
                Double price2 = p2.getPrice() != null ? p2.getPrice() : 0.0;
                return price2.compareTo(price1);
            });
        }
        return sortedList;
    }
} 