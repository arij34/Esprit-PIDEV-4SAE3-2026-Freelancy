package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CheatingEventType {
    TAB_SWITCH,
    FULLSCREEN_EXIT,
    COPY_PASTE,
    WINDOW_BLUR,
    MULTIPLE_LOGIN,
    WEBCAM_DISABLED,
    IP_CHANGED,
    DEVICE_CHANGED,
    PAGE_REFRESH,
    KEYBOARD_SHORTCUT,
    RIGHT_CLICK,
    MULTIPLE_MONITORS,
    SUSPICIOUS_ACTIVITY;

    @JsonCreator
    public static CheatingEventType fromValue(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "TAB_SWITCH" -> TAB_SWITCH;
            case "FULLSCREEN_EXIT" -> FULLSCREEN_EXIT;
            case "COPY_PASTE" -> COPY_PASTE;
            case "WINDOW_BLUR" -> WINDOW_BLUR;
            case "MULTIPLE_LOGIN" -> MULTIPLE_LOGIN;
            case "WEBCAM_DISABLED" -> WEBCAM_DISABLED;
            case "IP_CHANGED" -> IP_CHANGED;
            case "DEVICE_CHANGED" -> DEVICE_CHANGED;
            case "PAGE_REFRESH" -> PAGE_REFRESH;
            case "KEYBOARD_SHORTCUT" -> KEYBOARD_SHORTCUT;
            case "RIGHT_CLICK" -> RIGHT_CLICK;
            case "MULTIPLE_MONITORS" -> MULTIPLE_MONITORS;
            case "SUSPICIOUS_ACTIVITY" -> SUSPICIOUS_ACTIVITY;
            default -> throw new IllegalArgumentException("Unknown cheating event type: " + value);
        };
    }
}
