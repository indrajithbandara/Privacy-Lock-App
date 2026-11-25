package com.example.legacyfixture;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Representative version-2 plaintext schema; never part of the production app. */
public final class LegacyFixtureActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        File file = getDatabasePath("privacy_lock_db");
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            try (SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file, null)) {
                db.beginTransaction();
                try {
                    db.execSQL("CREATE TABLE locked_apps (packageName TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, isLocked INTEGER NOT NULL, isFavorite INTEGER NOT NULL, isPinned INTEGER NOT NULL, iconName TEXT NOT NULL)");
                    db.execSQL("CREATE TABLE security_config (id TEXT NOT NULL PRIMARY KEY, hashedPin TEXT NOT NULL, hashedDecoyPin TEXT NOT NULL, hashedPanicPin TEXT NOT NULL, biometricsEnabled INTEGER NOT NULL, autoLockTimeoutSeconds INTEGER NOT NULL, randomizeKeypad INTEGER NOT NULL, vibrateOnKeyPress INTEGER NOT NULL, lockNewlyInstalledApps INTEGER NOT NULL, intruderDetectionEnabled INTEGER NOT NULL, intruderSelfieEnabled INTEGER NOT NULL, failedAttemptLimit INTEGER NOT NULL, screenshotProtection INTEGER NOT NULL, autoLockNewApps INTEGER NOT NULL, temporaryUnlockDurationMinutes INTEGER NOT NULL, decoyModeType TEXT NOT NULL, themeMode TEXT NOT NULL, stealthCode TEXT NOT NULL)");
                    db.execSQL("CREATE TABLE timeline_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL, appName TEXT)");
                    db.execSQL("CREATE TABLE intruder_selfies (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, failedPinAttempt TEXT NOT NULL, avatarId INTEGER NOT NULL)");
                    db.execSQL("INSERT INTO locked_apps VALUES (?, ?, ?, 1, 1, 1, ?)", new Object[]{"com.example.legacyrecord", "Legacy record", "Other", "android"});
                    db.execSQL("INSERT INTO security_config VALUES ('global', ?, '', '', 0, 30, 1, 0, 0, 0, 0, 3, 1, 0, 0, 'NONE', 'DARK', '')", new Object[]{legacyTestVerifier()});
                    db.execSQL("INSERT INTO timeline_events VALUES (17, 123456789, 'CONFIG', 'Representative legacy fixture history', NULL)");
                    db.execSQL("INSERT INTO intruder_selfies VALUES (11, 123456788, '******', 0)");
                    db.setVersion(2);
                    db.setTransactionSuccessful();
                } finally { db.endTransaction(); }
            }
        }
        TextView message = new TextView(this);
        message.setText("Legacy test database initialized");
        setContentView(message);
    }

    private String legacyTestVerifier() {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest("731824PrivacyLockSaltEnterprise".getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
            return result.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
