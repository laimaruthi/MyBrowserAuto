package com.mybrowser.auto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class ReadAloudReceiver extends BroadcastReceiver {

    private static TextToSpeech ttsInstance = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String text = intent.getStringExtra("text");
        if (text == null) return;
        // Try to forward to media service for Android Auto playback
        Intent svc = new Intent(context, CarMediaService.class);
        svc.putExtra("play_text", text);
        try {
            context.startService(svc);
            return;
        } catch (Exception e) {
            // fallback to local TTS
        }

        if (ttsInstance == null) {
            ttsInstance = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsInstance.setLanguage(Locale.getDefault());
                    ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_aloud_id");
                }
            });
        } else {
            ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_aloud_id");
        }
    }
}
