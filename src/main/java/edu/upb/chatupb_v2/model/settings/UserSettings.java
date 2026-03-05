package edu.upb.chatupb_v2.model.settings;

import java.util.UUID;
import java.util.prefs.Preferences;

public final class UserSettings {

    private static final String PREF_NODE = "edu.upb.chatupb_v2";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";

    private UserSettings() {}

    public static String getUsername() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.get(KEY_USERNAME, null);
    }

    public static void setUsername(String username) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_USERNAME, username);
    }

    public static String getUserId() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.get(KEY_USER_ID, null);
    }

    public static String createUserId() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String id = getUserId();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            prefs.put(KEY_USER_ID, id);
        }
        return id;
    }

    public static void clearUsername() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(KEY_USERNAME);
    }
}