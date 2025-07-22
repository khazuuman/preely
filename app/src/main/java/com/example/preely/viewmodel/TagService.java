package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Tag;
import com.example.preely.model.response.TagResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints.*;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class TagService extends ViewModel {
    private final MainRepository<Tag> tagRepository = new MainRepository<>(Tag.class, CollectionName.TAGS);
    private final MutableLiveData<List<TagResponse>> tagList = new MutableLiveData<>();

    public LiveData<List<TagResponse>> getTagListResult() {
        return tagList;
    }

    public void getAllTag() {
        Query query = tagRepository.getDb().collection(CollectionName.TAGS);
        tagRepository.getAll(query).observeForever(result -> {
            if (result != null) {
                List<TagResponse> tagResponseList = new ArrayList<>();
                for (Tag tag: result) {
                    try {
                        tagResponseList.add(DataUtil.mapObj(tag, TagResponse.class));
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
                tagList.setValue(tagResponseList);
            } else {
                tagList.setValue(null);
            }
        });
    }
}


