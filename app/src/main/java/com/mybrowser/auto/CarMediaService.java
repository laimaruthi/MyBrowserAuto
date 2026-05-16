package com.mybrowser.auto;

import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.Locale;
import java.util.ArrayList;

public class CarMediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "CarMediaService";
    private static final String NOTIFICATION_CHANNEL_ID = "media_channel";
    private static final int NOTIFICATION_ID = 100;

    private TextToSpeech tts = null;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Android Auto media playback");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public MediaBrowserServiceCompat.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result result) {
        result.sendResult(new ArrayList<>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("play_text")) {
            String text = intent.getStringExtra("play_text");
            if (text != null) playTextAsMedia(text);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playTextAsMedia(String text) {
        Log.i(TAG, "playTextAsMedia: " + text);
        if (tts == null) {
            tts = new TextToSpeech(getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null, "media_read_aloud");
                }
            });
        } else {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "media_read_aloud");
        }
    }
}
