package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.dialog.AddEditSkillDialog;
import com.example.preely.model.entities.Skill;
import com.example.preely.repository.MainRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class SkillsManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText etSearch;
    private FloatingActionButton fabAdd;
    private MainRepository<Skill> skillRepository;
    private List<Skill> skillList = new ArrayList<>();
    private SkillsAdapter skillsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_skills_management, container, false);
        recyclerView = view.findViewById(R.id.recycler_skills);
        etSearch = view.findViewById(R.id.et_search_skills);
        fabAdd = view.findViewById(R.id.fab_add_skill);
        skillRepository = new MainRepository<>(Skill.class, "skills");
        skillsAdapter = new SkillsAdapter(skillList, new SkillsAdapter.OnSkillClickListener() {
            @Override
            public void onEdit(Skill skill) {
                showAddEditSkillDialog(skill, true);
            }
            @Override
            public void onDelete(Skill skill) {
                skillRepository.delete(skill.getId(), new com.example.preely.util.CallBackUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        loadSkills();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(skillsAdapter);
        fabAdd.setOnClickListener(v -> {
            showAddEditSkillDialog(null, false);
        });
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSkills(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        loadSkills();
        return view;
    }

    private void loadSkills() {
        skillRepository.getAll(com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("skills")).observe(getViewLifecycleOwner(), skills -> {
            skillList.clear();
            if (skills != null) skillList.addAll(skills);
            skillsAdapter.setSkillList(skillList);
        });
    }

    private void filterSkills(String query) {
        List<Skill> filtered = new ArrayList<>();
        for (Skill s : skillList) {
            if (s.getName() != null && s.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(s);
            }
        }
        skillsAdapter.setSkillList(filtered);
    }

    private void showAddEditSkillDialog(Skill skill, boolean isEdit) {
        AddEditSkillDialog dialog = new AddEditSkillDialog(getContext(), skill, (savedSkill, editMode) -> {
            if (editMode) {
                skillRepository.update(savedSkill, savedSkill.getId(), new com.example.preely.util.CallBackUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                        loadSkills();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                skillRepository.add(savedSkill, "skills", new com.example.preely.util.CallBackUtil.OnInsertCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
                        loadSkills();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Add failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }
} 