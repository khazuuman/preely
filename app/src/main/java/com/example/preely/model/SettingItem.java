package com.example.preely.model;

public class SettingItem {
    private int iconResId;
    private String title;
    private String subtitle;
    private boolean hasArrow;
    private boolean hasSwitch;
    private boolean switchState;
    private int itemType; // 0: normal, 1: switch, 2: logout
    
    public SettingItem(int iconResId, String title, String subtitle, boolean hasArrow) {
        this.iconResId = iconResId;
        this.title = title;
        this.subtitle = subtitle;
        this.hasArrow = hasArrow;
        this.hasSwitch = false;
        this.switchState = false;
        this.itemType = 0;
    }
    
    public SettingItem(int iconResId, String title, boolean hasSwitch, boolean switchState) {
        this.iconResId = iconResId;
        this.title = title;
        this.subtitle = "";
        this.hasArrow = false;
        this.hasSwitch = hasSwitch;
        this.switchState = switchState;
        this.itemType = 1;
    }
    
    public SettingItem(int iconResId, String title, int itemType) {
        this.iconResId = iconResId;
        this.title = title;
        this.subtitle = "";
        this.hasArrow = false;
        this.hasSwitch = false;
        this.switchState = false;
        this.itemType = itemType;
    }
    
    // Getters and Setters
    public int getIconResId() {
        return iconResId;
    }
    
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public boolean hasArrow() {
        return hasArrow;
    }
    
    public void setHasArrow(boolean hasArrow) {
        this.hasArrow = hasArrow;
    }
    
    public boolean hasSwitch() {
        return hasSwitch;
    }
    
    public void setHasSwitch(boolean hasSwitch) {
        this.hasSwitch = hasSwitch;
    }
    
    public boolean getSwitchState() {
        return switchState;
    }
    
    public void setSwitchState(boolean switchState) {
        this.switchState = switchState;
    }
    
    public int getItemType() {
        return itemType;
    }
    
    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
} 