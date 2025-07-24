package com.example.preely.view.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.model.entities.Skill;
import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.SkillViewHolder> {
    private List<Skill> skillList;
    private final OnSkillClickListener listener;

    public interface OnSkillClickListener {
        void onEdit(Skill skill);
        void onDelete(Skill skill);
    }

    public SkillsAdapter(List<Skill> skillList, OnSkillClickListener listener) {
        this.skillList = skillList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Skill skill = skillList.get(position);
        holder.tvName.setText(skill.getName());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(skill));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(skill));
    }

    @Override
    public int getItemCount() {
        return skillList != null ? skillList.size() : 0;
    }

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
        notifyDataSetChanged();
    }

    static class SkillViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnEdit, btnDelete;
        SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_skill_name);
            btnEdit = itemView.findViewById(R.id.btn_edit_skill);
            btnDelete = itemView.findViewById(R.id.btn_delete_skill);
        }
    }
} 