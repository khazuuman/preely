package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;
import com.example.preely.R;
import com.example.preely.model.entities.Skill;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditSkillDialog extends Dialog {
    private final Context context;
    private final Skill skill;
    private final boolean isEditMode;
    private final OnSkillDialogListener listener;
    private TextInputEditText etName;
    private TextInputLayout tilName;
    private MaterialButton btnSave, btnCancel;

    public interface OnSkillDialogListener {
        void onSkillSaved(Skill skill, boolean isEdit);
    }

    public AddEditSkillDialog(Context context, Skill skill, OnSkillDialogListener listener) {
        super(context);
        this.context = context;
        this.skill = skill != null ? skill : new Skill();
        this.isEditMode = skill != null;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_skill);
        etName = findViewById(R.id.et_skill_name);
        tilName = findViewById(R.id.til_skill_name);
        btnSave = findViewById(R.id.btn_save_skill);
        btnCancel = findViewById(R.id.btn_cancel_skill);
        if (isEditMode) {
            etName.setText(skill.getName());
        }
        btnSave.setOnClickListener(v -> saveSkill());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveSkill() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            return;
        }
        tilName.setError(null);
        skill.setName(name);
        if (listener != null) {
            listener.onSkillSaved(skill, isEditMode);
        }
        dismiss();
    }
} 