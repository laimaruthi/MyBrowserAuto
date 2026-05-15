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
