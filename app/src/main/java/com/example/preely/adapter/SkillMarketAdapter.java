package com.example.preely.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.SkillResponse;
import com.example.preely.view.ServiceListActivity;

import java.util.List;

public class SkillMarketAdapter extends RecyclerView.Adapter<SkillMarketAdapter.SkillViewHolder> {
    private final List<SkillResponse> skillList;

    public SkillMarketAdapter(List<SkillResponse> skillList) {
        this.skillList = skillList;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill_market, parent, false);
        return new SkillMarketAdapter.SkillViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        SkillResponse skill = skillList.get(position);
        Log.i("SKILL", skill.toString());
        String skillName = skill.getName();
        holder.tvSkill.setText(skillName);

        if (position < skillList.size() - 1) {
            holder.tvSkill.setText(skillName + ", ");
        } else {
            holder.tvSkill.setText(skillName);
        }
    }

    @Override
    public int getItemCount() {
        return skillList == null ? 0 : skillList.size();
    }

    public static class SkillViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkill;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSkill = itemView.findViewById(R.id.tv_skill);
        }
    }
}
