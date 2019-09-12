/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Code mostly copied from Android Developer example BluetoothChat
 * with the following notable exceptions:
 * ~ Added "Message request types" section
 * ~ Added MESSAGE_UUID_SECURE_CHANGE and MESSAGE_UUID_INSECURE_CHANGE
 * ~ Added MESSAGE_ERROR and HALT
 */

package com.stanfield.eric.wirelessfencingsandbox;

/**
 * Defines several constants used between {@link CommunicationService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_ERROR = 6;
    public static final int MESSAGE_UUID_SECURE_CHANGE = 10;
    public static final int MESSAGE_UUID_INSECURE_CHANGE = 11;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String HALT = "halt";

    // Message request types
    public static final String REQUEST_CHECKPOINT = "c";
    public static final String REQUEST_LASTTRIGGER = "h";
    public static final String REQUEST_GROUND = "g";
    public static final String REQUEST_TIMESYNC = "t";
    public static final String REQUEST_SHUTDOWN = "s";

    // Communication Config
    public static final int POLL_DELAY_MILLIS = 750;
    public static final int POLL_HIT_FREQUENCY_MILLIS = 40;
    public static final int POLL_CHECKPOINT_FREQUENCY_MILLIS = 10000;
}