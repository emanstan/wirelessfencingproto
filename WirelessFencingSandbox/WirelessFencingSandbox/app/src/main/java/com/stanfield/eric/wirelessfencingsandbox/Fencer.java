package com.stanfield.eric.wirelessfencingsandbox;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Eric Stanfield on 12/1/2015.
 */
public class Fencer {

    // Debugging
    private static final String TAG = "Fencer";

    // Status
    public static final int FENCER_STATUS_NOT_READY = 0;
    public static final int FENCER_STATUS_READY = 1;
    public static final int FENCER_STATUS_FENCING = 2;

    // Properties
    public int status = FENCER_STATUS_NOT_READY;
    public String name = "";
    public CommunicationService service = null;
    public BluetoothDevice device = null;
    public String deviceName = null;
    public String deviceAddress = null;

    public double timeSync = 0.0;
    public double timeOffset = 0.0;

    public boolean isTriggered = false;
    public double lastTrigger = 0.0;
    public double touchTSN = 0.0;
    public long delay = 0;

    public boolean hasPriority = false;

    public int score = 0;
    public int scoreCardsAdjustment = 0;

    public Fencer()
    {
        status = FENCER_STATUS_NOT_READY;
    }
}
