package com.mybrowser.auto;

import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

public class CarMediaService extends MediaBrowserServiceCompat {

    @Override
    public void onCreate() {
        super.onCreate();
        // Minimal skeleton: no playback yet. This declares the service so Auto can list the app as media.
        setSessionToken(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
