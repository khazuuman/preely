package com.example.preely;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class PreelyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dpsgcdrlx");
        config.put("api_key", "959226593637423");
        config.put("api_secret", "p54qEoP00iChUMofQX9SoeLuOsk");
        MediaManager.init(this, config);
    }
}