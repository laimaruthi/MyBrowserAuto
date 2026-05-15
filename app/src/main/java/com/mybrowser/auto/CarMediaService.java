package com.mybrowser.auto;

import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import java.util.Locale;
import java.util.ArrayList;

public class CarMediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "CarMediaService";
    private TextToSpeech tts = null;

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

    @Override
    public MediaBrowserServiceCompat.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        // Provide a simple root so clients can connect. 'root' is an arbitrary id.
        return new MediaBrowserServiceCompat.BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result result) {
        // Return an empty list for now. Real media items can be provided here later.
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
