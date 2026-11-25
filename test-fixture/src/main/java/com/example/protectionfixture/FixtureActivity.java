package com.example.protectionfixture;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** A separate, non-sensitive APK used only for emulator integration tests. */
public final class FixtureActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        TextView content = new TextView(this);
        content.setText("Fixture content visible");
        content.setTextSize(24);
        content.setGravity(Gravity.CENTER);
        setContentView(content);
    }
}
