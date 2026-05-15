package com.mybrowser.auto;

import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media.session.MediaSessionCompat;
import androidx.media.session.PlaybackStateCompat;
import androidx.media.app.NotificationCompat;
import androidx.core.app.NotificationCompat;
import java.util.Locale;
import java.util.ArrayList;

public class CarMediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "CarMediaService";
    private static final String NOTIFICATION_CHANNEL_ID = "media_channel";
    private static final int NOTIFICATION_ID = 100;

    private TextToSpeech tts = null;
    private MediaSessionCompat mediaSession;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initMediaSession();
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

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                isPlaying = true;
                updatePlaybackState();
            }

            @Override
            public void onPause() {
                isPlaying = false;
                updatePlaybackState();
            }

            @Override
            public void onStop() {
                isPlaying = false;
                updatePlaybackState();
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mediaSession.getSessionToken());
        updatePlaybackState();
    }

    private void updatePlaybackState() {
        long position = 0;
        long duration = 0;
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        
        PlaybackStateCompat.Builder builder =
                new PlaybackStateCompat.Builder()
                        .setActions(PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP)
                        .setState(state, position, 1.0f);
        mediaSession.setPlaybackState(builder.build());
        updateMediaNotification();
    }

    private void updateMediaNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("MyBrowserAuto")
                .setContentText(isPlaying ? "Playing..." : "Paused")
                .setContentIntent(pi)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1))
                .addAction(android.R.drawable.ic_media_play, "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY))
                .addAction(android.R.drawable.ic_media_pause, "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE));

        startForeground(NOTIFICATION_ID, builder.build());
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
        isPlaying = true;
        updatePlaybackState();
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
