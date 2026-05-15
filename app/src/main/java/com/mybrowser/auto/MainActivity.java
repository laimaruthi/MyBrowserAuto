package com.mybrowser.auto;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlBar;
    private Button sendToCarButton;

    private final Set<String> blockedHosts = new HashSet<>();

    private static final String CHANNEL_ID = "car_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlBar = findViewById(R.id.urlBar);
        webView = findViewById(R.id.webView);
        sendToCarButton = findViewById(R.id.sendToCar);

        loadBlocklist();
        createNotificationChannel();
    createPersistentNotification();

        webView.setWebViewClient(new BrowserClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("https://www.google.com");

        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            String url = urlBar.getText().toString().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
            return true;
        });

        sendToCarButton.setOnClickListener(view -> sendToCar());

        // handle incoming share or custom-scheme intents
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                String url = text.toString();
                webView.loadUrl(url);
                // also enqueue for TTS play
                enqueueReadAloud(url);
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (data != null && "mybrowserauto".equals(data.getScheme())) {
                String url = data.getQueryParameter("url");
                if (url != null) {
                    webView.loadUrl(url);
                    enqueueReadAloud(url);
                }
            }
        } else {
            // normal launch, check for extra
            String open = intent.getStringExtra("open_url");
            if (open != null) webView.loadUrl(open);
        }
    }

    private void enqueueReadAloud(String url) {
        Intent ttsIntent = new Intent(this, ReadAloudReceiver.class);
        ttsIntent.putExtra("text", "Read aloud: " + url);
        sendBroadcast(ttsIntent);
    }

    private void loadBlocklist() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getAssets().open("blocklist.txt")))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                blockedHosts.add(line);
            }
        } catch (IOException e) {
            // ignore - no blocklist available
        }
    }

    private class BrowserClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String host = uri.getHost();
            if (host != null) {
                for (String blocked : blockedHosts) {
                    if (host.equals(blocked) || host.endsWith("." + blocked)) {
                        return new WebResourceResponse("text/plain", "UTF-8",
                                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    private void sendToCar() {
        String url = webView.getUrl();
        if (url == null) url = "";

        Intent openIntent = new Intent(this, MainActivity.class).putExtra("open_url", url);
        PendingIntent openPI = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent ttsIntent = new Intent(this, ReadAloudReceiver.class);
        String title = webView.getTitle() != null ? webView.getTitle() : "";
        ttsIntent.putExtra("text", "Title: " + title + ". URL: " + url);
        PendingIntent ttsPI = PendingIntent.getBroadcast(this, 0, ttsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Send to car")
                .setContentText(title)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .addAction(new NotificationCompat.Action(0, "Open on phone", openPI))
                .addAction(new NotificationCompat.Action(0, "Read aloud", ttsPI))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this).notify(1001, nb.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Car channel";
            String description = "Notifications for send-to-car actions";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }

    private void createPersistentNotification() {
    Intent openIntent = new Intent(this, MainActivity.class);
    PendingIntent openPI = PendingIntent.getActivity(this, 0, openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("MyBrowserAuto")
        .setContentText("Tap to open on phone")
        .setSmallIcon(android.R.drawable.ic_menu_view)
        .setContentIntent(openPI)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW);

    NotificationManagerCompat.from(this).notify(2001, nb.build());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
