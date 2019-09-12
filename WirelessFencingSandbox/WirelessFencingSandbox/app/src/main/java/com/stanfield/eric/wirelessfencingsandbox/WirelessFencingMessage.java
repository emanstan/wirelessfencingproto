package com.stanfield.eric.wirelessfencingsandbox;

import android.util.Log;

/**
 * This handles formatting and extraction of the information from the message
 */
public class WirelessFencingMessage {

    // Debugging
    private static final String TAG = "WirelessFencingMessage";

    private static final String MESSAGE_DIVIDER = ":";
    public String prefix = "";
    public String value = "";

    public WirelessFencingMessage(String message) {
        this.prefix = getMessagePrefix(message);
        this.value = message.replace(this.prefix + MESSAGE_DIVIDER, "");
    }

    private String getMessagePrefix(String message) {
        try {
            String messagePrefixWithDivider = getMessagePrefixWithDivider(message);
            String messagePrefix = removeMessagePrefixDivider(messagePrefixWithDivider);
            return messagePrefix;
            //return removeMessagePrefixDivider(getMessagePrefixWithDivider(message));
        } catch (Exception e) {
            Log.e(TAG, "getMessagePrefix(): " + message + " " + e.getMessage());
            return "";
        }
    }

    private String getMessagePrefixWithDivider(String message) {
        try {
            if (message.length() < 1) {
                return "";
            }

            int index = message.indexOf(MESSAGE_DIVIDER);
            if (index < 1) { // must have some information before MESSAGE_DIVIDER
                return "";
            }

            return message.substring(0, index);
        } catch (Exception e) {
            Log.e(TAG, "getMessagePrefixWithDivider(): " + message + " " + e.getMessage());
            return "";
        }
    }

    private String removeMessagePrefixDivider(String message) {
        try {
            if (message.length() <= MESSAGE_DIVIDER.length()) {
                return message;
            }

            String messagePrefixWithoutDivider = message.substring(0, message.length() - MESSAGE_DIVIDER.length());
            return messagePrefixWithoutDivider;
            //return message.substring(0, message.length() - MESSAGE_DIVIDER.length());
        } catch (Exception e) {
            Log.e(TAG, "removeMessagePrefixDivider(): " + message + " " + e.getMessage());
            return "";
        }
    }
}
