package com.example.preely.util;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;

public class PaginationUtil {

    public static final int PAGE_SIZE = 10;
    private static boolean isLoading = false;
    private static boolean isLastPage = false;
    private static int currentPage = 0;

    public interface PaginationCallback<T> {
        void onLoadMore(List<T> newItems, int page);
        void onLoadComplete();
        void onError(String error);
    }

    public static <T> void setupPagination(RecyclerView recyclerView, 
                                         List<T> allItems, 
                                         PaginationCallback<T> callback) {
        
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        
                        loadMoreItems(recyclerView, allItems, callback);
                    }
                }
            }
        });
    }

    private static <T> void loadMoreItems(RecyclerView recyclerView, List<T> allItems, PaginationCallback<T> callback) {
        isLoading = true;
        
        // Simulate network delay
        recyclerView.postDelayed(() -> {
            int startIndex = currentPage * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, allItems.size());
            
            if (startIndex < allItems.size()) {
                List<T> newItems = allItems.subList(startIndex, endIndex);
                currentPage++;
                
                if (callback != null) {
                    callback.onLoadMore(newItems, currentPage);
                }
                
                if (endIndex >= allItems.size()) {
                    isLastPage = true;
                }
            } else {
                isLastPage = true;
            }
            
            isLoading = false;
            
            if (isLastPage && callback != null) {
                callback.onLoadComplete();
            }
        }, 1000); // 1 second delay
    }

    public static <T> List<T> getPageItems(List<T> allItems, int page) {
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allItems.size());
        
        if (startIndex < allItems.size()) {
            return allItems.subList(startIndex, endIndex);
        }
        return new ArrayList<>();
    }

    public static void resetPagination() {
        isLoading = false;
        isLastPage = false;
        currentPage = 0;
    }

    public static boolean isLoading() {
        return isLoading;
    }

    public static boolean isLastPage() {
        return isLastPage;
    }

    public static int getCurrentPage() {
        return currentPage;
    }

    // Firestore pagination helper
    public static class FirestorePagination<T> {
        private Object lastDocument = null;
        private boolean hasMore = true;
        private int pageSize = PAGE_SIZE;

        public FirestorePagination(int pageSize) {
            this.pageSize = pageSize;
        }

        public boolean hasMore() {
            return hasMore;
        }

        public Object getLastDocument() {
            return lastDocument;
        }

        public void setLastDocument(Object lastDocument) {
            this.lastDocument = lastDocument;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void reset() {
            lastDocument = null;
            hasMore = true;
        }
    }
} 