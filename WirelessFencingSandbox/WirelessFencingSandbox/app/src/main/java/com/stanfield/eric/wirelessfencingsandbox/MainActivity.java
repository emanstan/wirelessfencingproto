package com.stanfield.eric.wirelessfencingsandbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/*
TODO: implement code to handle establishing priority
TODO: implement code to handle rules around overtime
TODO: implement code to handle rules around draw
TODO: create a Settings dialog with player setup, match setup
TODO: create an About/Help dialog
TODO: support tournament standings
TODO: support log export/email, (need settings and integration with profiles first)
 */

public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Intent request codes
    private static final int REQUEST_BT_ENABLE = 3;
    private static final int REQUEST_RED_CONNECT_SECURE = 4;
    private static final int REQUEST_RED_CONNECT_INSECURE = 5;
    private static final int REQUEST_GREEN_CONNECT_SECURE = 6;
    private static final int REQUEST_GREEN_CONNECT_INSECURE = 7;

    // UI Config
    private static final int UI_TYPE_CONNECT = 11;
    private static final int UI_TYPE_DISCONNECT = 12;
    private static final int UI_TYPE_MATCH_READY = 21;
    private static final int UI_TYPE_MATCH_START = 22;
    private static final int UI_TYPE_MATCH_STOP = 23;
    private static final int UI_TYPE_MATCH_HALT = 24;
    private static final int UI_TYPE_MATCH_RESUME = 25;
    private static final int UI_TYPE_HIT = 31;
    private static final int UI_TYPE_GROUND = 32;
    private static final int UI_TYPE_SOUND = 41;
    private static final int UI_TYPE_TIMER = 42;
    private static final int NUMBER_CONFIG_TYPE_POOL_SCORE = 1;
    private static final int NUMBER_CONFIG_TYPE_ELIMINATION_SCORE = 2;
    private static final int NUMBER_CONFIG_TYPE_RED_CARD = 3;
    private static final int NUMBER_CONFIG_TYPE_YELLOW_CARD = 4;
    private static final int RED_PLAYER = 1;
    private static final UUID RED_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int RED_COLOR = Color.argb(255, 255, 0, 0);
    private static final int GREEN_PLAYER = 2;
    private static final UUID GREEN_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC");
    private static final int GREEN_COLOR = Color.argb(255, 0, 255, 0);
    private static final int BACKGROUND_COLOR = Color.argb(255, 220, 221, 221);
    private static final int HIT_COLOR = Color.argb(255, 255, 255, 0);
    private static final int GROUND_COLOR = Color.argb(255, 255, 255, 0);
    private static final int CONNECT_FONT_SIZE = 24;
    private static final int SCORE_FONT_SIZE = 100;
    private static final String LOG_DECIMAL_FORMAT = "%.4f";
    private static final int MATCH_PHASE_START = 1;
    private static final int MATCH_PHASE_STOP = 2;

    // Debugging / Logging Config
    private String logContent = "";
    private boolean logVerbose = true;//false;
    private boolean testMode = false;
    private int testStep = 0;

    // UI References
    private Activity activity;
    private Menu mMainMenu;
    private Drawable mBorder;
    private Drawable mBorderRed;
    private Drawable mBorderGreen;
    private Drawable mBorderRounded;
    private Drawable mRounded;
    private TextView mLog;
    private Button mRedConnect;
    private Button mRedDisconnect;
    private Button mRedGround;
    private Button mRedPlayerYellowCard;
    private Button mRedPlayerRedCard;
    private Button mGreenConnect;
    private Button mGreenDisconnect;
    private Button mGreenGround;
    private Button mGreenPlayerYellowCard;
    private Button mGreenPlayerRedCard;
    private Button mStart;
    private Button mHalt;
    private TextView mPeriod;
    private TextView mTimer;
    private String validationMessage;

    private boolean isMatchConditionTypeChecked = false;

    // Communication
    private BluetoothAdapter mBluetoothAdapter = null;
    /*
    private CommunicationService mRedService = null;
    public BluetoothDevice mRedDevice = null;
    private String mRedAddress = null;
    private String mRedDeviceName = null;

    private CommunicationService mGreenService = null;
    public BluetoothDevice mGreenDevice = null;
    private String mGreenAddress = null;
    private String mGreenDeviceName = null;
    */
    private static final long NTP_FAILURE = -1;
    private String mNTPServer = "2.android.pool.ntp.org";

    // Fencing
    private Fencer red = null;
    private Fencer green = null;

    // Match (eventually to go into Match class)
    private Match match;
    private static final int TIME_TO_DISPLAY_HIT_MILLIS = 2000;
    private static final int TIME_TO_DISPLAY_GROUND_MILLIS = 500;
    private static final int TIME_TO_RESET_WAIT = 500;
    /*
    private static final int TIME_TO_LOCKOUT_MILLIS = 40;
    private static final int TIME_TO_LOCKOUT_TOLERANCE_MILLIS = 10;
    private static final int MATCH_BREAK_TIME_LIMIT = 60000;
    private static final int MATCH_TEST = 0;
    private static final int MATCH_TEST_POINTS_FOR_WIN = 99;
    private static final int MATCH_TEST_PERIODS = 0; // really there is 1, but call it 0 so that label doesn't show
    private static final int MATCH_TEST_PERIOD_TIME_LIMIT = 60000; // milliseconds
    private static final int MATCH_POOL = 1;
    private static final int MATCH_POOL_POINTS_FOR_WIN = 5;
    private static final int MATCH_POOL_PERIODS = 0; // really there is 1, but call it 0 so that label doesn't show
    private static final int MATCH_POOL_PERIOD_TIME_LIMIT = 180000; // milliseconds
    private static final int MATCH_ELIMINATION = 2;
    private static final int MATCH_ELIMINATION_POINTS_FOR_WIN = 15;
    private static final int MATCH_ELIMINATION_PERIODS = 3;
    private static final int MATCH_ELIMINATION_PERIOD_TIME_LIMIT = 180000; // milliseconds
    private static final int MATCH_CONVERT_CARD_YELLOW_TO_RED = 2; // 2 yellow cards = 1 red card
    private int matchType = 0;
    private boolean matchReady = false; // determines if match is ready to begin or not
    private boolean matchStart = false; // determines if match is currently underway or not
    private boolean isHalted = false;  // determines whether the match is currently in a halt state
    private boolean triggerLockoutInitiated = false; // determines if the trigger lockout timer has been started
    private boolean triggerLockout = false; // determines if triggers are allowed to register
    private boolean evaluationLockout = false; // determines if a score and match evaluation is allowed
    private boolean isMatchStartTimeLocal = false;
    private boolean isMatchStopTimeLocal = false;
    private long matchStartTime = 0; // time of match start
    private long matchStopTime = 0;  // time of match stop
    //private long pointStartTime = 0; // time of point start
    //private long pointStopTime = 0; // time of point stop
    //private double hitTimeDiff = 0.0; // time between hits (before lockout)
    private int matchPeriodLimit = 0;
    private long matchPeriodTimeLimit = 0;
    private int matchPeriod = 0;
    private long matchPeriodTimeRemaining = 0;
    private long matchBreakTimeRemaining = 0;
    private DateFormat matchPeriodTimerFormat;
    private CountDownTimer matchPeriodTimer;
    private CountDownTimer matchBreakTimer;

    private int redScore = 0;
    private int redScoreCardsAdjustment = 0;
    private double redTimeSync = 0.0;
    private double redTimeOffset = 0.0;
    private boolean isRedTriggered = false;
    private double redLastTrigger = 0.0;
    private double redTouchTSN = 0.0;
    //private long redDelay = 0;

    private int greenScore = 0;
    private int greenScoreCardsAdjustment = 0;
    private double greenTimeSync = 0.0;
    private double greenTimeOffset = 0.0;
    private boolean isGreenTriggered = false;
    private double greenLastTrigger = 0.0;
    private double greenTouchTSN = 0.0;
    //private long greenDelay = 0;
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //supposed to help force orientation
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (savedInstanceState != null) {
            // Restore value of members from saved state

        } else {
            // Probably initialize members with default values for a new instance
        }

        // Initialize fencers
        red = new Fencer();
        red.status = Fencer.FENCER_STATUS_NOT_READY;
        red.name = "Red";
        green = new Fencer();
        green.status = Fencer.FENCER_STATUS_NOT_READY;
        green.name = "Green";

        // Initialize match
        match = new Match();
        match.red = red;
        match.green = green;
        /*
        matchReady = false;
        matchStart = false;
        isHalted = false;
        matchPeriodTimerFormat = new SimpleDateFormat("mm:ss");
        validationMessage = "";
        */

        // Get UI References
        activity = MainActivity.this;
        mBorder = getResources().getDrawable(R.drawable.border); //getResources().getDrawable() is depricated
        mBorderRed = getResources().getDrawable(R.drawable.border_red); //getResources().getDrawable() is depricated
        mBorderGreen = getResources().getDrawable(R.drawable.border_green); //getResources().getDrawable() is depricated
        mBorderRounded = getResources().getDrawable(R.drawable.border_rounded); //getResources().getDrawable() is depricated
        mRounded = getResources().getDrawable(R.drawable.rounded_button); //getResources().getDrawable() is depricated
        mLog = (TextView) findViewById(R.id.logView);
        mRedConnect = (Button) findViewById(R.id.redButton);
        mRedDisconnect = (Button) findViewById(R.id.redDisconnect);
        mRedGround = (Button) findViewById(R.id.redGroundButton);
        mRedPlayerYellowCard = (Button) findViewById(R.id.redCardsYellowButton);
        mRedPlayerRedCard = (Button) findViewById(R.id.redCardsRedButton);
        mGreenConnect = (Button) findViewById(R.id.greenButton);
        mGreenDisconnect = (Button) findViewById(R.id.greenDisconnect);
        mGreenGround = (Button) findViewById(R.id.greenGroundButton);
        mGreenPlayerYellowCard = (Button) findViewById(R.id.greenCardsYellowButton);
        mGreenPlayerRedCard = (Button) findViewById(R.id.greenCardsRedButton);
        mStart = (Button) findViewById(R.id.startButton);
        mHalt = (Button) findViewById(R.id.haltButton);
        mPeriod = (TextView) findViewById(R.id.periodText);
        mTimer = (TextView) findViewById(R.id.timerText);

        // Get NTP settings
        //getNTPServer();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, R.string.request_bt_adapter_fail, Toast.LENGTH_LONG).show();
            finish(); // does this exit application?
        }

        // Setup UI Events
        try {
            mRedConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (red.service != null) {
                        if (red.service.getState() == CommunicationService.STATE_CONNECTED) {
                            switch (match.type) {
                                case Match.MATCH_POOL:
                                    numberDialog(mRedConnect, RED_PLAYER, NUMBER_CONFIG_TYPE_POOL_SCORE);
                                    break;
                                case Match.MATCH_ELIMINATION:
                                    numberDialog(mRedConnect, RED_PLAYER, NUMBER_CONFIG_TYPE_ELIMINATION_SCORE);
                                    break;
                            }
                            return;
                        }
                    }
                    connectServiceRequest(REQUEST_RED_CONNECT_SECURE); //originally just this statement
                    /*
                    if (mRedService != null) {
                        if (mRedService.getState() == CommunicationService.STATE_CONNECTED) {
                            switch (matchType) {
                                case MATCH_POOL:
                                    numberDialog(mRedConnect, RED_PLAYER, NUMBER_CONFIG_TYPE_POOL_SCORE);
                                    break;
                                case MATCH_ELIMINATION:
                                    numberDialog(mRedConnect, RED_PLAYER, NUMBER_CONFIG_TYPE_ELIMINATION_SCORE);
                                    break;
                            }
                            return;
                        }
                    }
                    connectServiceRequest(REQUEST_RED_CONNECT_SECURE); //originally just this statement
                    */
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mRedConnect.setOnClickListener: " + e.getMessage());
        }

        try {
            mGreenConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (green.service != null) {
                        if (green.service.getState() == CommunicationService.STATE_CONNECTED) {
                            switch (match.type) {
                                case Match.MATCH_POOL:
                                    numberDialog(mGreenConnect, GREEN_PLAYER, NUMBER_CONFIG_TYPE_POOL_SCORE);
                                    break;
                                case Match.MATCH_ELIMINATION:
                                    numberDialog(mGreenConnect, GREEN_PLAYER, NUMBER_CONFIG_TYPE_ELIMINATION_SCORE);
                                    break;
                            }
                            return;
                        }
                    }
                    connectServiceRequest(REQUEST_GREEN_CONNECT_SECURE); //originally just this statement
                    /*
                    if (mGreenService != null) {
                        if (mGreenService.getState() == CommunicationService.STATE_CONNECTED) {
                            switch (matchType) {
                                case MATCH_POOL:
                                    numberDialog(mGreenConnect, GREEN_PLAYER, NUMBER_CONFIG_TYPE_POOL_SCORE);
                                    break;
                                case MATCH_ELIMINATION:
                                    numberDialog(mGreenConnect, GREEN_PLAYER, NUMBER_CONFIG_TYPE_ELIMINATION_SCORE);
                                    break;
                            }
                            return;
                        }
                    }
                    connectServiceRequest(REQUEST_GREEN_CONNECT_SECURE); //originally just this statement
                    */
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mGreenConnect.setOnClickListener: " + e.getMessage());
        }

        try {
            mRedDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        disconnectService(red.service);
                        red.deviceAddress = null;
                        red.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, RED_PLAYER);
                        /*
                        disconnectService(mRedService);
                        mRedAddress = null;
                        red.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, RED_PLAYER);
                        */
                    } catch (Exception e) {
                        Log.e(TAG, "mRedDisconnect.onClick: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mRedDisconnect.setOnClickListener: " + e.getMessage());
        }

        try {
            mGreenDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        disconnectService(green.service);
                        green.deviceAddress = null;
                        green.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, GREEN_PLAYER);
                        /*
                        disconnectService(mGreenService);
                        mGreenAddress = null;
                        green.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, GREEN_PLAYER);
                        */
                    } catch (Exception e) {
                        Log.e(TAG, "mGreenDisconnect.onClick: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mGreenDisconnect.setOnClickListener: " + e.getMessage());
        }

        try {
            mStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //if (matchStart) {
                        if (match.isStarted) {
                            initiateStopMatch();
                        } else {
                            initiateStartMatch();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "mStart.onClick: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mStart.setOnClickListener: " + e.getMessage());
        }

        try {
            mHalt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //if (isHalted) {
                        if (match.isHalted) {
                            resumeMatch();
                        } else {
                            haltMatch();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "mHalt.onClick: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mHalt.setOnClickListener: " + e.getMessage());
        }

        try {
            mRedPlayerYellowCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberDialog(mRedPlayerYellowCard, RED_PLAYER, NUMBER_CONFIG_TYPE_YELLOW_CARD);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mRedPlayerYellowCard.setOnClickListener: " + e.getMessage());
        }

        try {
            mRedPlayerRedCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberDialog(mRedPlayerRedCard, RED_PLAYER, NUMBER_CONFIG_TYPE_RED_CARD);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mRedPlayerRedCard.setOnClickListener: " + e.getMessage());
        }

        try {
            mGreenPlayerYellowCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberDialog(mGreenPlayerYellowCard, GREEN_PLAYER, NUMBER_CONFIG_TYPE_YELLOW_CARD);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mGreenPlayerRedCard.setOnClickListener: " + e.getMessage());
        }

        try {
            mGreenPlayerRedCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberDialog(mGreenPlayerRedCard, GREEN_PLAYER, NUMBER_CONFIG_TYPE_RED_CARD);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "mGreenPlayerRedCard.setOnClickListener: " + e.getMessage());
        }
    }
    /*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("mLog", mLog.getText().toString());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLog.setText(savedInstanceState.getString("mLog"));
    }
    */
    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_BT_ENABLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disconnectService(red.service);
        disconnectService(green.service);
        /*
        disconnectService(mRedService);
        disconnectService(mGreenService);
        */
    }

    @Override
    public void onResume() {
        super.onResume();

        checkService(red.service);
        checkService(green.service);
        /*
        checkService(mRedService);
        checkService(mGreenService);
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMainMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuMatchSummary = menu.findItem(R.id.menuMatchSummary);
        if (menuMatchSummary != null) {
            menuMatchSummary.setTitle(getMatchSummary());
        }
        MenuItem menuMatchConditionType = menu.findItem(R.id.menuMatchConditionType);
        if (menuMatchConditionType != null) {
            menuMatchConditionType.setChecked(isMatchConditionTypeChecked);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menuMatchSummary:
                item.setTitle(getMatchSummary());
                return true;
            case R.id.menuMatchConditionType:
                isMatchConditionTypeChecked = !item.isChecked();
                actionMatchConditionType(isMatchConditionTypeChecked);
                return true;
            case R.id.menuShowLog:
                showLog();
                return true;
            case R.id.menuExportLog:
                //actionExportLog();
                return true;
            case R.id.menuSelfTest:
                selfTest();
                setMatchSummary();
                return true;
            case R.id.action_settings:
                //showSettings();
                return true;
            //case R.id.help:
            //    showHelp();
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getMatchSummary() {
        String redPlayerName = "Red";
        String greenPlayerName = "Green";
        String weapon = "Epee"; // open up to 3-weapon later
        String type = "";

        if (match.testMode) { type = "Test"; }

        try {
            switch (match.type) {
                case Match.MATCH_TEST:
                    type = "Test";
                    break;
                case Match.MATCH_POOL:
                    type = "Pool";
                    break;
                case Match.MATCH_ELIMINATION:
                    type = "Elim";
                    break;
                //default:
                //    type = "";
                //    break;
            }

            if (red.name != null && red.name.trim() != "") {
                redPlayerName = red.name;
            }

            if (green.name != null && green.name.trim() != "") {
                greenPlayerName = green.name;
            }
        } catch (Exception e) {
            Log.e(TAG, "getMatchSummary(): " + e.getMessage());
        }

        return redPlayerName + " vs " + greenPlayerName + " - " + weapon + " - " + type;
    }

    private void setMatchSummary() {
        MenuItem menuMatchSummary = mMainMenu.findItem(R.id.menuMatchSummary);
        if (menuMatchSummary != null) {
            menuMatchSummary.setTitle(getMatchSummary());
        }
    }

    private void actionMatchConditionType (boolean isChecked) {
        try {
            if (isChecked) {
                match.type = Match.MATCH_ELIMINATION;
            } else {
                match.type = Match.MATCH_POOL;
            }
            /*
            if (isChecked) {
                matchType = MATCH_ELIMINATION;
                mMatchTypeSwitch.setChecked(true);
                ((MenuItem) findViewById(R.id.menuMatchConditionType)).setChecked(true);
            } else {
                matchType = MATCH_POOL;
                mMatchTypeSwitch.setChecked(false);
                ((MenuItem) findViewById(R.id.menuMatchConditionType)).setChecked(false);
            }
            */
            MenuItem menuMatchConditionType = mMainMenu.findItem(R.id.menuMatchConditionType);
            if (menuMatchConditionType != null) {
                menuMatchConditionType.setChecked(isChecked);
            }
            MenuItem menuMatchSummary = mMainMenu.findItem(R.id.menuMatchSummary);
            if (menuMatchSummary != null) {
                menuMatchSummary.setTitle(getMatchSummary());
            }
        } catch (Exception e) {
            Log.e(TAG, "actionMatchConditionType(): " + e.getMessage());
        }
    }

    private void showLog() {
        try {
            new AlertDialog.Builder(activity)
                    .setTitle("Match Log")
                    .setMessage(mLog.getText())
                            /*
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            */
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "actionShowLog(): " + e.getMessage());
        }
    }

    private final Handler mRedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case CommunicationService.STATE_CONNECTED:

                            // Set fencer ready
                            red.status = Fencer.FENCER_STATUS_READY;

                            // Update UI state
                            setupUI(UI_TYPE_CONNECT, RED_PLAYER);

                            // Ready Yet?
                            if (match.areAllFencersReady()) {
                            //if (isMatchReady()) {
                                setupUI(UI_TYPE_MATCH_READY, 0);
                            }

                            break;
                        case CommunicationService.STATE_CONNECTING:
                            Toast.makeText(activity, R.string.red_connecting, Toast.LENGTH_SHORT).show();
                            break;
                        case CommunicationService.STATE_LISTEN:
                        case CommunicationService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.trim();
                    if (logVerbose) mLog.append("WFS: " + writeMessage + "\n");
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (logVerbose) mLog.append("Red: " + readMessage + "\n");
                    WirelessFencingMessage wfm = new WirelessFencingMessage(readMessage.trim());
                    switch (wfm.prefix) {
                        case Constants.REQUEST_TIMESYNC:
                            try {
                                red.timeSync = Double.valueOf(wfm.value).doubleValue();
                                //redTimeSync = Double.valueOf(wfm.value).doubleValue();
                                if (logVerbose) mLog.append("Red: TimeSync at " + wfm.value + "\n");
                            } catch (Exception e) {
                                Log.e(TAG, "Red MESSAGE_READ timeSync(" + Constants.REQUEST_TIMESYNC + "): " + wfm.value + " " + e.getMessage());
                            }
                            break;
                        case Constants.REQUEST_LASTTRIGGER:
                            try {
                                double trigger = Double.valueOf(wfm.value).doubleValue();
                                if (trigger != red.lastTrigger) {
                                    red.lastTrigger = trigger;
                                    //red.delay = red.service.getPollingResponseDelay();
                                    catchRedTrigger();
                                    if (logVerbose) mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                                }
                                /*
                                if (trigger != redLastTrigger) {
                                    redLastTrigger = trigger;
                                    //redDelay = mRedService.getPollingResponseDelay();
                                    catchRedTrigger();
                                    if (logVerbose) mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, redLastTrigger) + "\n");
                                }
                                */
                            } catch (Exception e) {
                                Log.e(TAG, "Red MESSAGE_READ lastTrigger(" + Constants.REQUEST_LASTTRIGGER + "): " + wfm.value + " " + e.getMessage());
                            }
                            break;
                        case Constants.REQUEST_GROUND:
                            try {
                                //This will come back to bite me, need a more elegant and robust solution
                                setupUI(UI_TYPE_GROUND, RED_PLAYER);
                            } catch (Exception e) {
                                Log.e(TAG, "Red MESSAGE_READ ground(" + Constants.REQUEST_GROUND + ") " + e.getMessage());
                            }
                            break;
                        default:
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    red.deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + red.deviceName, Toast.LENGTH_SHORT).show();
                    }
                    /*
                    mRedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mRedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    */
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_UUID_SECURE_CHANGE:
                    //Toast.makeText(activity, "Red UUID (Secure) Changed",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_UUID_INSECURE_CHANGE:
                    //Toast.makeText(activity, "Red UUID (Insecure) Changed",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_ERROR:
                    Toast.makeText(activity, R.string.request_error,
                            Toast.LENGTH_SHORT).show();
                    haltMatch();
                    break;
                default:
            }
        }
    };

    private final Handler mGreenHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case CommunicationService.STATE_CONNECTED:

                            // Set fencer ready
                            green.status = Fencer.FENCER_STATUS_READY;

                            // Update UI state
                            setupUI(UI_TYPE_CONNECT, GREEN_PLAYER);

                            // Ready Yet?
                            if (match.areAllFencersReady()) {
                                //if (isMatchReady()) {
                                setupUI(UI_TYPE_MATCH_READY, 0);
                            }

                            break;
                        case CommunicationService.STATE_CONNECTING:
                            Toast.makeText(activity, R.string.green_connecting, Toast.LENGTH_SHORT).show();
                            break;
                        case CommunicationService.STATE_LISTEN:
                        case CommunicationService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.trim();
                    if (logVerbose) mLog.append("WFS: " + writeMessage + "\n");
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    readMessage = readMessage.trim();
                    if (logVerbose) mLog.append("Green: " + readMessage + "\n");
                    WirelessFencingMessage wfm = new WirelessFencingMessage(readMessage.trim());
                    switch (wfm.prefix) {
                        case Constants.REQUEST_TIMESYNC:
                            try {
                                green.timeSync = Double.valueOf(wfm.value).doubleValue();
                                //greenTimeSync = Double.valueOf(wfm.value).doubleValue();
                                if (logVerbose) mLog.append("Green: TimeSync at " + wfm.value + "\n");
                            } catch (Exception e) {
                                Log.e(TAG, "Green MESSAGE_READ timeSync(" + Constants.REQUEST_TIMESYNC + "): " + wfm.value + " " + e.getMessage());
                            }
                            break;
                        case Constants.REQUEST_LASTTRIGGER:
                            try {
                                double trigger = Double.valueOf(wfm.value).doubleValue();
                                if (trigger != green.lastTrigger) {
                                    green.lastTrigger = trigger;
                                    //green.delay = green.service.getPollingResponseDelay();
                                    catchGreenTrigger();
                                    if (logVerbose) mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                                }
                                /*
                                if (trigger != greenLastTrigger) {
                                    greenLastTrigger = trigger;
                                    //greenDelay = mGreenService.getPollingResponseDelay();
                                    catchGreenTrigger();
                                    if (logVerbose) mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, greenLastTrigger) + "\n");
                                }
                                */
                            } catch (Exception e) {
                                Log.e(TAG, "Green MESSAGE_READ lastTrigger(" + Constants.REQUEST_LASTTRIGGER + "): " + wfm.value + " " + e.getMessage());
                            }
                            break;
                        case Constants.REQUEST_GROUND:
                            try {
                                //This will come back to bite me, need a more elegant and robust solution
                                setupUI(UI_TYPE_GROUND, GREEN_PLAYER);
                            } catch (Exception e) {
                                Log.e(TAG, "Green MESSAGE_READ ground(" + Constants.REQUEST_GROUND + ") " + e.getMessage());
                            }
                            break;
                        default:
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    green.deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + green.deviceName, Toast.LENGTH_SHORT).show();
                    }
                    /*
                    mGreenDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mGreenDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    */
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_UUID_SECURE_CHANGE:
                    //Toast.makeText(activity, "Green UUID (Secure) Changed",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_UUID_INSECURE_CHANGE:
                    //Toast.makeText(activity, "Green UUID (Insecure) Changed",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_ERROR:
                    Toast.makeText(activity, R.string.request_error,
                            Toast.LENGTH_SHORT).show();
                    haltMatch();
                    break;
                default:
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(activity, R.string.ready_to_connect, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "REQUEST_BT_ENABLED Failed");
                    Toast.makeText(activity, R.string.request_bt_enable_fail, Toast.LENGTH_SHORT).show();
                }
            case REQUEST_RED_CONNECT_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (red.deviceAddress == null) {
                            red.deviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        }
                        Log.d(TAG, red.deviceAddress);
                        red.device = mBluetoothAdapter.getRemoteDevice(red.deviceAddress);
                        if (red.service == null) {
                            red.service = new CommunicationService(MainActivity.this, mRedHandler);
                        }
                        red.service.setUuidSecure(RED_UUID);
                        if (red.service.getState() == CommunicationService.STATE_NONE) {
                            red.service.start();
                        }
                        red.service.connect(red.device, true);
                        /*
                        if (mRedAddress == null) {
                            mRedAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        }
                        mRedDevice = mBluetoothAdapter.getRemoteDevice(mRedAddress);
                        if (mRedService == null) {
                            mRedService = new CommunicationService(MainActivity.this, mRedHandler);
                        }
                        mRedService.setUuidSecure(RED_UUID);
                        if (mRedService.getState() == CommunicationService.STATE_NONE) {
                            mRedService.start();
                        }
                        mRedService.connect(mRedDevice, true);
                        */
                    } catch (Exception e) {
                        Log.e(TAG, "REQUEST_RED_CONNECT_SECURE: " + e.getMessage());
                    }
                }
                break;
            case REQUEST_RED_CONNECT_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // we are not supporting insecure right now
                }
                break;
            case REQUEST_GREEN_CONNECT_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (green.deviceAddress == null) {
                            green.deviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        }
                        green.device = mBluetoothAdapter.getRemoteDevice(green.deviceAddress);
                        if (green.service == null) {
                            green.service = new CommunicationService(MainActivity.this, mGreenHandler);
                        }
                        green.service.setUuidSecure(GREEN_UUID);
                        if (green.service.getState() == CommunicationService.STATE_NONE) {
                            green.service.start();
                        }
                        green.service.connect(green.device, true);
                        /*
                        if (mGreenAddress == null) {
                            mGreenAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        }
                        mGreenDevice = mBluetoothAdapter.getRemoteDevice(mGreenAddress);
                        if (mGreenService == null) {
                            mGreenService = new CommunicationService(MainActivity.this, mGreenHandler);
                        }
                        mGreenService.setUuidSecure(GREEN_UUID);
                        if (mGreenService.getState() == CommunicationService.STATE_NONE) {
                            mGreenService.start();
                        }
                        mGreenService.connect(mGreenDevice, true);
                        */
                    } catch (Exception e) {
                        Log.e(TAG, "REQUEST_GREEN_CONNECT_SECURE: " + e.getMessage());
                    }
                }
                break;
            case REQUEST_GREEN_CONNECT_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // we are not supporting insecure right now
                }
                break;
        }
    }

    private void ensureDiscoverable() {
        try {
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "ensureDiscoverable(): " + e.getMessage());
        }
    }

    private void connectServiceRequest(int requestCode) {
        try {
            ensureDiscoverable();
            Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "connectRequest(): " + e.getMessage());
        }
    }

    private void disconnectService(CommunicationService service) {
        if (service != null) {
            service.stop();
        }
    }

    private void checkService(CommunicationService service) {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (service != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (service.getState() == CommunicationService.STATE_NONE) {
                // Start the Bluetooth chat services
                service.start();
            }
        }
    }

    private void getNTPServer() {
        try {
            final Resources res = this.getResources();
            final int id = Resources.getSystem().getIdentifier("config_ntpServer", "string", "android");
            mNTPServer = res.getString(id);
            mLog.append("WFS: You are using NTP server " + mNTPServer + "\n");
        } catch (Exception e) {
            Log.e(TAG, "getNTPServer(): " + e.getMessage());
        }
    }

    private long getNTPTime() {
        long now = NTP_FAILURE;

        try {
            SntpClient client = new SntpClient();
            if (client.requestTime(mNTPServer, 500)) {
                now = client.getNtpTime() + SystemClock.elapsedRealtime() -
                        client.getNtpTimeReference();
                Log.i(TAG, "getNTPTime(): " + String.valueOf(now));
            }
        } catch (Exception e) {
            Log.e(TAG, "getNTPTime(): " + e.getMessage());
        }

        return now;
    }

    private void setupUI(int type, int participant) {
        switch (type) {
            case UI_TYPE_CONNECT:
                switch (participant) {
                    case RED_PLAYER:
                        mRedConnect.setBackground(mBorderRed);
                        mRedConnect.setText(String.format("%d", red.score));//redScore));
                        mRedConnect.setTextSize(SCORE_FONT_SIZE);
                        //mRedConnect.setBackgroundColor(RED_COLOR);
                        mRedDisconnect.setEnabled(true);
                        break;
                    case GREEN_PLAYER:
                        mGreenConnect.setBackground(mBorderGreen);
                        mGreenConnect.setText(String.format("%d", green.score));//greenScore));
                        mGreenConnect.setTextSize(SCORE_FONT_SIZE);
                        //mGreenConnect.setBackgroundColor(GREEN_COLOR);
                        mGreenDisconnect.setEnabled(true);
                        break;
                }
                break;
            case UI_TYPE_DISCONNECT:
                switch (participant) {
                    case RED_PLAYER:
                        mRedConnect.setBackground(mBorder);
                        mRedConnect.setText(R.string.connect);
                        mRedConnect.setTextSize(CONNECT_FONT_SIZE);
                        //mRedConnect.setBackgroundColor(BACKGROUND_COLOR);
                        mRedDisconnect.setEnabled(false);
                        break;
                    case GREEN_PLAYER:
                        mGreenConnect.setBackground(mBorder);
                        mGreenConnect.setText(R.string.connect);
                        mGreenConnect.setTextSize(CONNECT_FONT_SIZE);
                        //mGreenConnect.setBackgroundColor(BACKGROUND_COLOR);
                        mGreenDisconnect.setEnabled(false);
                        break;
                }
                break;
            case UI_TYPE_MATCH_READY:
                mStart.setEnabled(true);
                mHalt.setEnabled(false); // newly added
                break;
            case UI_TYPE_MATCH_START:
                mStart.setText(R.string.stop);
                mHalt.setText(R.string.halt);
                mHalt.setEnabled(true);
                break;
            case UI_TYPE_MATCH_STOP:
                mHalt.setEnabled(false);
                mHalt.setText(R.string.halt);
                mStart.setText(R.string.start);
                break;
            case UI_TYPE_MATCH_HALT:
                mHalt.setText(R.string.resume);
                break;
            case UI_TYPE_MATCH_RESUME:
                mHalt.setText(R.string.halt);
                break;
            case UI_TYPE_HIT:
                switch (participant) {
                    case RED_PLAYER:
                        if (red.status != Fencer.FENCER_STATUS_NOT_READY) {
                            //mRedConnect.setBackgroundColor(HIT_COLOR);
                            mRedConnect.setBackgroundColor(RED_COLOR);
                            mRedConnect.setText(String.valueOf(red.score));//redScore));
                            mRedConnect.setTextSize(SCORE_FONT_SIZE);
                            //initiateHitTimerColor(mRedConnect, RED_COLOR);
                            initiateHitTimer(mRedConnect, mBorderRed);
                        }
                        break;
                    case GREEN_PLAYER:
                        if (green.status != Fencer.FENCER_STATUS_NOT_READY) {
                            //mGreenConnect.setBackgroundColor(HIT_COLOR);
                            mGreenConnect.setBackgroundColor(GREEN_COLOR);
                            mGreenConnect.setText(String.valueOf(green.score));//greenScore));
                            mGreenConnect.setTextSize(SCORE_FONT_SIZE);
                            //initiateHitTimerColor(mGreenConnect, GREEN_COLOR);
                            initiateHitTimer(mGreenConnect, mBorderGreen);
                        }
                        break;
                }
                break;
            case UI_TYPE_GROUND:
                switch (participant) {
                    case RED_PLAYER:
                        if (red.status != Fencer.FENCER_STATUS_NOT_READY) {
                            mRedGround.setBackground(mRounded);
                            initiateGroundTimer(mRedGround, mBorderRounded);
                        }
                        break;
                    case GREEN_PLAYER:
                        if (green.status != Fencer.FENCER_STATUS_NOT_READY) {
                            mGreenGround.setBackground(mRounded);
                            initiateGroundTimer(mGreenGround, mBorderRounded);
                        }
                        break;
                }
                break;
            case UI_TYPE_SOUND:
                final MediaPlayer mTone = MediaPlayer.create(activity, R.raw.score);
                mTone.start();
                break;
            case UI_TYPE_TIMER:
                try {
                    if (match.period > 0) {
                        mPeriod.setText("Period " + String.valueOf(match.period));
                    }
                    mTimer.setText(match.periodTimerFormat.format(new Date(match.periodTimeRemaining)));
                    /*
                    if (matchPeriod > 0) {
                        mPeriod.setText("Period " + String.valueOf(matchPeriod));
                    }
                    mTimer.setText(matchPeriodTimerFormat.format(new Date(matchPeriodTimeRemaining)));
                    */
                } catch (Exception e) {
                    Log.e(TAG, "UI_TYPE_TIMER: " + e.getMessage());
                }
        }
    }
    /*
    private boolean isMatchReady() {
        if (red.status == Fencer.FENCER_STATUS_READY
                && green.status == Fencer.FENCER_STATUS_READY) {
            return true;
        }

        return false;
    }
    */
    private boolean validMatch() {
        if (match.testMode) return true;

        if (red.status != Fencer.FENCER_STATUS_READY)
        {
            validationMessage = validationMessage
                    .concat(getString(R.string.red_connect_invalid))
                    .concat(System.getProperty("line.separator"));
        }

        if (green.status != Fencer.FENCER_STATUS_READY) {
            validationMessage = validationMessage
                    .concat(getString(R.string.green_connect_invalid))
                    .concat(System.getProperty("line.separator"));
        }

        if (validationMessage.length() > 0) {
            Toast.makeText(activity,
                    "Please address the issues before continuing:"
                            .concat(System.getProperty("line.separator"))
                            .concat(validationMessage)
                    , Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    /*
    private void configMatch() {
        if (testMode) matchType = MATCH_TEST;

        double minutes = 0;

        switch (matchType) {
            case MATCH_POOL:
                matchPeriodLimit = MATCH_POOL_PERIODS;
                matchPeriodTimeLimit = MATCH_POOL_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                minutes = MATCH_POOL_PERIOD_TIME_LIMIT / 1000 / 60;
                mLog.append("WFS: Match configured for POOL (" +
                                "First to " + String.format("%d", MATCH_POOL_POINTS_FOR_WIN) +
                                " or " +
                                String.format("%.2f", minutes) + " minutes" +
                                ")\n"
                                );
                break;
            case MATCH_ELIMINATION:
                matchPeriodLimit = MATCH_ELIMINATION_PERIODS;
                matchPeriodTimeLimit = MATCH_ELIMINATION_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                minutes = MATCH_ELIMINATION_PERIOD_TIME_LIMIT / 1000 / 60;
                mLog.append("WFS: Match configured for ELIMINATION (" +
                                "First to " + String.format("%d", MATCH_ELIMINATION_POINTS_FOR_WIN) +
                                " or " +
                                String.format("%.2f", minutes) + " minutes" +
                                ")\n"
                                );
                break;
            case MATCH_TEST:
                matchPeriodLimit = MATCH_TEST_PERIODS;
                matchPeriodTimeLimit = MATCH_TEST_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                minutes = MATCH_TEST_PERIOD_TIME_LIMIT / 1000 / 60;
                mLog.append("WFS: Match configured for TEST (" +
                                "First to " + String.format("%d", MATCH_TEST_POINTS_FOR_WIN) +
                                " or " +
                                String.format("%.2f", minutes) + " minutes" +
                                ")\n"
                );
                break;
            default:
                //Invalid Match Type
                break;
        }
    }
    */
    private void initiateStartMatch() {
        new NTPTask().execute(MATCH_PHASE_START);
    }

    private void initiateStopMatch() {
        new NTPTask().execute(MATCH_PHASE_STOP);
    }

    private void startMatch() {
        try {

            match.isReady = validMatch();//matchReady = validMatch();

            if (match.isReady) {//if (matchReady) {

                if (match.isStartTimeLocal) {//if (isMatchStartTimeLocal) {
                    if (logVerbose) mLog.append("WFS: NTP Start Time request failed. Using System time.\n");
                }
                red.timeOffset = match.startTime - red.timeSync;//redTimeOffset = matchStartTime - redTimeSync; // if negative, then TimeSync was before Start
                green.timeOffset = match.startTime - green.timeSync;//greenTimeOffset = matchStartTime - greenTimeSync; // if negative, then TimeSync was before Start
                Log.d(TAG, "matchStartTime = " + String.valueOf(match.startTime) +//matchStartTime) +
                        ", redTimeSync=" + String.format(LOG_DECIMAL_FORMAT, red.timeSync) +//redTimeSync) +
                        ", greenTimeSync=" + String.format(LOG_DECIMAL_FORMAT, green.timeSync));//greenTimeSync));

                // Configure match
                match.configure();
                match.period = 1;
                red.score = 0;
                green.score = 0;
                mLog.append(match.getConfigSummary());
                /*
                configMatch();
                matchPeriod = 1;
                redScore = 0;
                greenScore = 0;
                */

                // Start timer
                //initiateMatchPeriodTimer(matchPeriodTimeLimit);

                // Start match
                match.isStarted = true;//matchStart = true;
                setupUI(UI_TYPE_MATCH_START, 0);
                resumeMatch();
                mLog.append("WFS: Starting Match at " + String.valueOf(match.startTime) + "\n");//matchStartTime) + "\n");

                // Set fencer status
                red.status = Fencer.FENCER_STATUS_FENCING;
                green.status = Fencer.FENCER_STATUS_FENCING;

            } else {
                Toast.makeText(activity, R.string.match_invalid, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "startMatch(): " + e.getMessage());
        }
    }

    private void stopMatch() {
        try {

            // Set fencer status
            red.status = Fencer.FENCER_STATUS_READY;
            green.status = Fencer.FENCER_STATUS_READY;

            // Stop match
            match.isReady = false;//matchReady = false;
            match.isStarted = false;//matchStart = false;
            setupUI(UI_TYPE_MATCH_STOP, 0);
            haltMatch();
            mLog.append("WFS: Stopping Match at " + String.valueOf(match.stopTime) + "\n");//matchStopTime) + "\n");

            // Reset match
            red.score = 0;
            green.score = 0;
            match.periodTimeRemaining = 0;
            /*
            redScore = 0;
            greenScore = 0;
            matchPeriodTimeRemaining = 0;
            */
            setupUI(UI_TYPE_TIMER, 0);

            if (match.isStopTimeLocal) {//if (isMatchStopTimeLocal) {
                if (logVerbose) mLog.append("WFS: NTP Stop Time request failed. Using System time.\n");
            }

        } catch (Exception e) {
            Log.e(TAG, "stopMatch(): " + e.getMessage());
        }
    }

    private void haltMatch() {
        if (match.periodTimer != null) {
            match.periodTimer.cancel();
            match.periodTimer = null;
        }
        if (match.isStarted) {
            match.isHalted = true;
            setupUI(UI_TYPE_MATCH_HALT, 0);
        } else {
            match.isHalted = false;
            setupUI(UI_TYPE_MATCH_STOP, 0);
        }
        /*
        if (matchPeriodTimer != null) {
            matchPeriodTimer.cancel();
            matchPeriodTimer = null;
        }
        if (matchStart) {
            isHalted = true;
            setupUI(UI_TYPE_MATCH_HALT, 0);
        } else {
            isHalted = false;
            setupUI(UI_TYPE_MATCH_STOP, 0);
        }
        */
    }

    private void resumeMatch() {
        if (match.isStarted) {
            match.isHalted = false;
            setupUI(UI_TYPE_MATCH_RESUME, 0);
            initiateMatchPeriodTimer(match.periodTimeRemaining);
        } else {
            match.isHalted = false;
            setupUI(UI_TYPE_MATCH_STOP, 0);
        }
        /*
        if (matchStart) {
            isHalted = false;
            setupUI(UI_TYPE_MATCH_RESUME, 0);
            initiateMatchPeriodTimer(matchPeriodTimeRemaining);
        } else {
            isHalted = false;
            setupUI(UI_TYPE_MATCH_STOP, 0);
        }
        */
    }

    private void numberDialog (final Button sourceButton, final int participant, final int configType) {
        final int originalN = getButtonCount(sourceButton);

        final Dialog d = new Dialog(activity);
        d.setTitle(getNumberDialogTitle(participant, configType));
        d.setContentView(R.layout.number_dialog);
        Button increaseButton = (Button) d.findViewById(R.id.increaseButton);
        Button decreaseButton = (Button) d.findViewById(R.id.decreaseButton);
        Button setButton = (Button) d.findViewById(R.id.setButton);
        Button cancelButton = (Button) d.findViewById(R.id.cancelButton);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(getNumberDialogMaxValue(configType));
        np.setMinValue(0);
        np.setWrapSelectorWheel(false);
        //np.setOnValueChangedListener(this);
        np.setValue(originalN);
        increaseButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                np.setValue(np.getValue() + 1);
            }
        });
        decreaseButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                np.setValue(np.getValue() - 1);
            }
        });
        setButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (configType == NUMBER_CONFIG_TYPE_YELLOW_CARD && np.getValue() >= Match.MATCH_CONVERT_CARD_YELLOW_TO_RED) {
                    String threshold = String.format("%d", Match.MATCH_CONVERT_CARD_YELLOW_TO_RED);
                    new AlertDialog.Builder(activity)
                            .setTitle("Convert Yellow Cards to Red Card")
                            .setMessage("You have set " + threshold + " or more Yellow Cards. " +
                                    "Every " + threshold + " Yellow Cards will be converted to a Red Card."
                            )
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    int currentYellowCount = np.getValue();
                                    int newYellowCount = 0;
                                    int currentRedCount = 0;
                                    int newRedCount = 0;
                                    int convertedRedCards = currentYellowCount / Match.MATCH_CONVERT_CARD_YELLOW_TO_RED;
                                    switch (participant) {
                                        case RED_PLAYER:
                                            currentRedCount = getButtonCount(mRedPlayerRedCard);
                                            newRedCount = currentRedCount + convertedRedCards;
                                            newYellowCount = currentYellowCount % Match.MATCH_CONVERT_CARD_YELLOW_TO_RED;
                                            mRedPlayerRedCard.setText(String.format("%d", newRedCount));
                                            mRedPlayerYellowCard.setText(String.format("%d", newYellowCount));
                                            mLog.append("WFS: Converted Red Player " + String.format("%d", currentYellowCount) + " Yellow Cards " +
                                                    "to " + String.format("%d", convertedRedCards) + " Red Cards " +
                                                    "with " + String.format("%d", newYellowCount) + " Yellow Cards remaining.\n"
                                            );
                                            break;
                                        case GREEN_PLAYER:
                                            currentRedCount = getButtonCount(mGreenPlayerRedCard);
                                            newRedCount = currentRedCount + convertedRedCards;
                                            newYellowCount = currentYellowCount % Match.MATCH_CONVERT_CARD_YELLOW_TO_RED;
                                            mGreenPlayerRedCard.setText(String.format("%d", newRedCount));
                                            mGreenPlayerYellowCard.setText(String.format("%d", newYellowCount));
                                            mLog.append("WFS: Converted Green Player " + String.format("%d", currentYellowCount) + " Yellow Cards " +
                                                            "to " + String.format("%d", convertedRedCards) + " Red Cards " +
                                                            "with " + String.format("%d", newYellowCount) + " Yellow Cards remaining.\n"
                                            );
                                            break;
                                    }
                                    np.setValue(newYellowCount);
                                }
                            })
                            .show();
                }
                int newN = np.getValue();
                sourceButton.setText(String.valueOf(newN));
                d.dismiss();
                mLog.append(getNumberDialogMessage(participant, configType, originalN, newN));
            }
        });
        cancelButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private String getNumberDialogParticipantString (final int participant) {
        switch (participant) {
            case RED_PLAYER:
                return "Red Player";
            case GREEN_PLAYER:
                return "Green Player";
            default:
                return "System";
        }
    }

    private String getNumberDialogAdjustmentTypeString (final int configType) {
        switch (configType) {
            case NUMBER_CONFIG_TYPE_POOL_SCORE:
            case NUMBER_CONFIG_TYPE_ELIMINATION_SCORE:
                return "Score";
            case NUMBER_CONFIG_TYPE_YELLOW_CARD:
                return "Yellow Cards";
            case NUMBER_CONFIG_TYPE_RED_CARD:
                return "Red Cards";
            default:
                return "";
        }
    }

    private int getNumberDialogMaxValue (final int configType) {
        switch (configType) {
            case NUMBER_CONFIG_TYPE_POOL_SCORE:
                return Match.MATCH_POOL_POINTS_FOR_WIN;
            case NUMBER_CONFIG_TYPE_ELIMINATION_SCORE:
                return Match.MATCH_ELIMINATION_POINTS_FOR_WIN;
            case NUMBER_CONFIG_TYPE_YELLOW_CARD:
            case NUMBER_CONFIG_TYPE_RED_CARD:
            default:
                return 30; // 2 * 15 points is a safe upper limit
        }
    }

    private String getNumberDialogTitle (final int participant, final int configType) {
        return getNumberDialogParticipantString(participant) +
                " > " + getNumberDialogAdjustmentTypeString(configType);
    }

    private String getNumberDialogMessage (final int participant, final int configType, final int origValue, final int newValue) {
        return "WFS: Manual adjustment made to " + getNumberDialogParticipantString(participant) +
                " " + getNumberDialogAdjustmentTypeString(configType) +
                " from " + String.format("%d", origValue) +
                " to " + String.format("%d", newValue) + ".\n";
    }

    private int getButtonCount(final Button sourceButton) {
        int buttonCount = 0;

        try {
            buttonCount = Integer.parseInt(sourceButton.getText().toString());
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "getButtonCount() -> " + getResources().getResourceEntryName(sourceButton.getId()) + " parseInt: " + nfe.getMessage());
        }

        return buttonCount;
    }

    private void initiateMatchPeriodTimer(long millisTimeRemaining) {
        try {
            if (millisTimeRemaining > 0) {
                if (match.periodTimer != null) {
                    match.periodTimer.cancel();
                    match.periodTimer = null;
                }
                match.periodTimer = new CountDownTimer(millisTimeRemaining, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        match.periodTimeRemaining = millisUntilFinished;
                        setupUI(UI_TYPE_TIMER, 0);
                    }

                    @Override
                    public void onFinish() {
                        if (!match.isHalted) {
                            match.period++;
                            match.periodTimeRemaining = 0;
                            setupUI(UI_TYPE_TIMER, 0);
                            haltMatch();
                            if (match.period > match.periodLimit) {
                                evaluateMatch();
                            } else {
                                initiateMatchBreakTimer(Match.MATCH_BREAK_TIME_LIMIT);
                            }
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "initiateMatchPeriodTimer(): " + e.getMessage());
        }
    }

    private void initiateMatchBreakTimer(long millisTimeRemaining) {
        try {
            if (millisTimeRemaining > 0) {
                if (match.breakTimer != null) {
                    match.breakTimer.cancel();
                    match.breakTimer = null;
                }
                match.breakTimer = new CountDownTimer(millisTimeRemaining, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        match.breakTimeRemaining = millisUntilFinished;
                        setupUI(UI_TYPE_TIMER, 0);
                    }

                    @Override
                    public void onFinish() {
                        initiateMatchPeriodTimer(match.periodTimeLimit);
                    }
                }.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "initiateMatchBreakTimer(): " + e.getMessage());
        }
    }

    private void initiateHitTimerColor(final Button button, final int color) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundColor(color);
            }
        }, TIME_TO_DISPLAY_HIT_MILLIS);
    }

    private void initiateHitTimer(final Button button, final Drawable bkg) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackground(bkg);
            }
        }, TIME_TO_DISPLAY_HIT_MILLIS);
    }

    private void initiateGroundTimerColor(final Button button, final int color) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundColor(color);
            }
        }, TIME_TO_DISPLAY_GROUND_MILLIS);
    }

    private void initiateGroundTimer(final Button button, final Drawable bkg) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackground(bkg);
            }
        }, TIME_TO_DISPLAY_GROUND_MILLIS);
    }

    private void initiateTriggerLockoutTimer() {
        match.triggerLockoutInitiated = true;
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        try {
                            Log.i(TAG, "initiateTriggerLockoutTimer()");

                            // Lockout further hit(s) until current ones are evaluated
                            match.triggerLockout = true;

                            // Evaluate hit(s)
                            if (!match.evaluationLockout &&
                                    red.status == Fencer.FENCER_STATUS_FENCING &&
                                    green.status == Fencer.FENCER_STATUS_FENCING) {
                                match.evaluationLockout = true;
                                evaluateScore();
                                evaluateMatch();
                            } else {
                                if (red.isTriggered || green.isTriggered) {
                                    hitSound();
                                }
                                if (red.isTriggered) {
                                    hitRed();
                                }
                                if (green.isTriggered) {
                                    hitGreen();
                                }
                            }

                            initiateResetWaitTimer();
                            // Reset for the next hit
                            //resetHit();
                        } catch (Exception e) {
                            Log.e(TAG, "initiateTriggerLockoutTimer(): " + e.getMessage());
                        }
                    }
                }
                , Match.MATCH_HIT_LOCKOUT_MILLIS + Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS);
    }

    private void initiateResetWaitTimer() {
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        try {
                            Log.i(TAG, "initiateResetWaitTimer()");

                            // Reset for the next hit
                            resetHit();
                        } catch (Exception e) {
                            Log.e(TAG, "initiateResetWaitTimer(): " + e.getMessage());
                        }
                    }
                }
                , TIME_TO_RESET_WAIT);
    }

    private void catchRedTrigger() {
        try {
            if (!match.triggerLockoutInitiated) {
                initiateTriggerLockoutTimer();
            }
            if (!red.isTriggered) {
                red.isTriggered = true;
                red.touchTSN = touchTSN(match.startTime, red.lastTrigger, red.timeSync, 0); //red.delay);
                Log.d(TAG, "calculating redTouchTSN using " +
                            "matchStartTime of " + String.valueOf(match.startTime) +
                            ", redLastTrigger of " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) +
                            ", redTimeOffset of " + String.format(LOG_DECIMAL_FORMAT, red.timeOffset) +
                            ", redTimeSync of " + String.format(LOG_DECIMAL_FORMAT, red.timeSync) +
                            //", redDelay of " + String.valueOf(red.delay) +
                            "; redTouchTSN = " + String.format(LOG_DECIMAL_FORMAT, red.touchTSN)
                );
                mLog.append("Red: Hit at +" + String.format(LOG_DECIMAL_FORMAT, red.touchTSN) + "\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "catchRedTrigger(): " + e.getMessage());
        }
    }

    private void catchGreenTrigger() {
        try {
            if (!match.triggerLockoutInitiated) {
                initiateTriggerLockoutTimer();
            }
            if (!green.isTriggered) {
                green.isTriggered = true;
                green.touchTSN = touchTSN(match.startTime, green.lastTrigger, green.timeSync, 0); //green.delay);
                Log.d(TAG, "calculating greenTouchTSN using " +
                            "matchStartTime of " + String.valueOf(match.startTime) +
                            ", greenLastTrigger of " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) +
                            ", greenTimeOffset of " + String.format(LOG_DECIMAL_FORMAT, green.timeOffset) +
                            ", greenTimeSync of " + String.format(LOG_DECIMAL_FORMAT, green.timeSync) +
                            //", greenDelay of " + String.valueOf(green.delay) +
                            "; greenTouchTSN = " + String.format(LOG_DECIMAL_FORMAT, green.touchTSN)
                );
                mLog.append("Green: Hit at +" + String.format(LOG_DECIMAL_FORMAT, green.touchTSN) + "\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "catchGreenTrigger(): " + e.getMessage());
        }
    }

    private void evaluateMatch() {
        try {
            if (match.isScoreFinal) return;

            int redScoreTotal = red.score + red.scoreCardsAdjustment;
            int greenScoreTotal = green.score + green.scoreCardsAdjustment;
            switch (match.type) {
                case Match.MATCH_POOL:
                    if (match.isOvertimeAllowed &&
                            redScoreTotal >= Match.MATCH_POOL_POINTS_FOR_WIN &&
                            greenScoreTotal >= Match.MATCH_POOL_POINTS_FOR_WIN) {
                        haltMatch();
                        mLog.append("WFS: " + R.string.match_draw + "\n");
                        Toast.makeText(MainActivity.this, R.string.match_draw, Toast.LENGTH_LONG).show();

                        // 1) Determine Priority
                        red.hasPriority = doCoinToss();
                        green.hasPriority = !red.hasPriority;
                        if (red.hasPriority) {
                            mRedGround.setText("P");
                            mLog.append("WFS: " + R.string.match_priority_red + "\n");
                            Toast.makeText(MainActivity.this, R.string.match_priority_red, Toast.LENGTH_LONG).show();
                        } else {
                            mRedGround.setText("");
                        }
                        if (red.hasPriority) {
                            mRedGround.setText("P");
                            mLog.append("WFS: " + R.string.match_priority_green + "\n");
                            Toast.makeText(MainActivity.this, R.string.match_priority_green, Toast.LENGTH_LONG).show();
                        } else {
                            mRedGround.setText("");
                        }

                        // 2) Set the clock for (1) minute
                        match.period++;
                        match.periodTimeRemaining = 60000; // 60 seconds
                        match.isOvertimeAllowed = false;
                    } else if (redScoreTotal >= Match.MATCH_POOL_POINTS_FOR_WIN) {
                        mLog.append("WFS: " + R.string.red_win + "\n");
                        Toast.makeText(MainActivity.this, R.string.red_win, Toast.LENGTH_LONG).show();
                        stopMatch();
                    } else if (greenScoreTotal >= Match.MATCH_POOL_POINTS_FOR_WIN) {
                        mLog.append("WFS: " + R.string.green_win + "\n");
                        Toast.makeText(MainActivity.this, R.string.green_win, Toast.LENGTH_LONG).show();
                        stopMatch();
                    }
                    break;
                case Match.MATCH_ELIMINATION:
                    if (match.isOvertimeAllowed &&
                            redScoreTotal >= Match.MATCH_ELIMINATION_POINTS_FOR_WIN &&
                            greenScoreTotal >= Match.MATCH_ELIMINATION_POINTS_FOR_WIN) {
                        // There is more logic to this, but for now just call it a draw
                        Toast.makeText(MainActivity.this, R.string.match_draw, Toast.LENGTH_LONG).show();
                        stopMatch();
                    } else if (redScoreTotal >= Match.MATCH_ELIMINATION_POINTS_FOR_WIN) {
                        Toast.makeText(MainActivity.this, R.string.red_win, Toast.LENGTH_LONG).show();
                        stopMatch();
                    } else if (greenScoreTotal >= Match.MATCH_ELIMINATION_POINTS_FOR_WIN) {
                        Toast.makeText(MainActivity.this, R.string.green_win, Toast.LENGTH_LONG).show();
                        stopMatch();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "evaluateMatch(): " + e.getMessage());
        }
    }

    private void evaluateScore() {
        try {
            if (isDoubleTouch(match.triggerLockout, red.isTriggered, green.isTriggered, red.touchTSN, green.touchTSN)) {
                mLog.append("WFS: Double-Touch! " +
                        "Hit Time Difference of " + String.format(LOG_DECIMAL_FORMAT, Math.abs(red.touchTSN - green.touchTSN)) +
                        "ms (Time Before Hit Lockout of " + String.format("%d", Match.MATCH_HIT_LOCKOUT_MILLIS) +
                        "ms +/-" + String.format("%d", Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS) + "ms)\n");
                hitSound();
                increaseRedScore();
                increaseGreenScore();
            } else if (isTouch(match.triggerLockout, red.isTriggered, green.isTriggered)) {
                hitSound();
                if (red.isTriggered) {
                    increaseRedScore();
                } else if (green.isTriggered) {
                    increaseGreenScore();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "evaluateScore(): " + e.getMessage());
        }
    }

    private void evaluateCards() {
        /* Rules for Cards issued against a player
          ~ Black card means ejection from event
          ~ Every Red card adjusts opponents score + 1
          ~ Every 2 Yellow cards results in Red card
         */
        int redPlayerYellow2Red = 0,
                redPlayerYellowCards = getButtonCount(mRedPlayerYellowCard),
                redPlayerRedCards = getButtonCount(mRedPlayerRedCard)
        ;
        green.scoreCardsAdjustment = 0;
        if (redPlayerRedCards > 0) {
            green.scoreCardsAdjustment += redPlayerRedCards;
            Log.d(TAG, "Green Score Adjustment of " + String.format("%d", redPlayerRedCards) +
                    " because of Red Player Red Card(s)");
        }
        redPlayerYellow2Red = redPlayerYellowCards / 2;
        if (redPlayerYellow2Red > 0) {
            green.scoreCardsAdjustment += redPlayerYellow2Red;
            Log.d(TAG, "Green Score Adjustment of " + String.format("%d", redPlayerYellow2Red) +
                    " because of Red Player Yellow Card(s)");
        }

        int greenPlayerYellow2Red = 0,
                greenPlayerYellowCards = getButtonCount(mGreenPlayerYellowCard),
                greenPlayerRedCards = getButtonCount(mGreenPlayerRedCard)
        ;
        red.scoreCardsAdjustment = 0;
        if (greenPlayerRedCards > 0) {
            red.scoreCardsAdjustment += greenPlayerRedCards;
            Log.d(TAG, "Red Score Adjustment of " + String.format("%d", greenPlayerRedCards) +
                    " because of Green Player Red Card(s)");
        }
        greenPlayerYellow2Red = greenPlayerYellowCards / 2;
        if (greenPlayerYellow2Red > 0) {
            red.scoreCardsAdjustment += greenPlayerYellow2Red;
            Log.d(TAG, "Red Score Adjustment of " + String.format("%d", greenPlayerYellow2Red) +
                    " because of Green Player Yellow Card(s)");
        }

        if (red.score == 5) return; // TODO: take this out when finished testing!!!
        evaluateMatch();
    }

    private void increaseRedScore() {
        red.score++;
        hitRed();
    }

    private void increaseGreenScore() {
        green.score++;
        hitGreen();
    }

    private void hitRed() {
        setupUI(UI_TYPE_HIT, RED_PLAYER);
    }

    private void hitGreen() {
        setupUI(UI_TYPE_HIT, GREEN_PLAYER);
    }

    private void hitSound() {
        setupUI(UI_TYPE_SOUND, 0);
    }

    private void resetHit() {
        match.evaluationLockout = false;
        match.triggerLockout = false;
        red.isTriggered = false;
        green.isTriggered = false;
        match.triggerLockoutInitiated = false;
    }

    private boolean isDoubleTouch(boolean lockout, boolean redTriggered, boolean greenTriggered, double redTSN, double greenTSN) {
        if (lockout) {
            if (redTriggered && greenTriggered) {
                if (Math.abs(redTSN - greenTSN) <= Match.MATCH_HIT_LOCKOUT_MILLIS + Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTouch(boolean lockout, boolean redTriggered, boolean greenTriggered) {
        if (lockout) {
            if (redTriggered || greenTriggered) {
                return true;
            }
        }

        return false;
    }

    /**
     * This figures the time since new match start (TSN) touch for the participant in milliseconds
     */
    private double touchTSN(long startTime, double triggerTime, double syncTime, long delayTime) {
        if ((triggerTime - delayTime - syncTime) > (startTime - syncTime)) {
            return (triggerTime - delayTime - syncTime) - (startTime - syncTime);
        }

        return 0.0;
    }

    /**
     * This figures the time since epoch (TSE) touch for the participant in milliseconds
     */
    /*
    private double touchTSE(long startTime, double triggerTime, double offsetTime) {
        if ((triggerTime + offsetTime) > startTime) {
            return (triggerTime + offsetTime) - startTime;
        }

        return 0.0;
    }
    */

    private boolean doCoinToss() {
        SecureRandom sr = new SecureRandom();
        return sr.nextBoolean();
    }

    private void selfTest() {
        try {
            match.testMode = true;
            match.testStep = 0;

            int currentStep = 1;
            int lastStep = 10;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (match.testStep < 10) {
                        selfTestStep(++match.testStep);
                        mLog.append("testStep = " + String.format("%d", match.testStep) + "\n");
                    }
                    if (match.testStep >= 10) {
                        match.testStep = 100;
                        match.testMode = false;
                    }
                }
            };
            Handler h = new Handler();

            for (currentStep = 1; currentStep < lastStep; currentStep++) {
                //mLog.append("Setting up Self-Test step " + String.format("%d", currentStep) + " ...\n");
                h.postDelayed(r, 3000 * currentStep);
            }

            //mLog.append("Setting up Self-Test step " + String.format("%d", lastStep) + " ...\n");
            h.postDelayed(r, 3000 * lastStep + 500);
        } catch (Exception e) {
            match.testStep = 100;
            match.testMode = false;
            Log.e(TAG, "selfTest(): " + e.getMessage());
        }
    }

    private void selfTestStep(int step) {
        int diff = 0;
        String state = "";

        try {
            /*
            // Setup reusable objects
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchGreenTrigger();
                }
            };
            Handler h = new Handler();
            */
            // Execute Step
            switch (step) {
                case 1:
                    state = "Initiating Self-Test ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    state = "Starting Match ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    red.timeSync = System.currentTimeMillis();
                    mLog.append("Red: TimeSync = " + String.format(LOG_DECIMAL_FORMAT, red.timeSync) + "\n");
                    green.timeSync = System.currentTimeMillis();
                    mLog.append("Green: TimeSync = " + String.format(LOG_DECIMAL_FORMAT, green.timeSync) + "\n");
                    initiateStartMatch();
                    break;
                case 3:
                    state = "Single Touch Red ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    red.lastTrigger = System.currentTimeMillis();
                    mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                    catchRedTrigger();
                    break;
                case 4:
                    state = "Single Touch Green ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchGreenTrigger();
                    break;
                case 5:
                    state = "Double Touch ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    red.lastTrigger = System.currentTimeMillis();
                    mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchRedTrigger();
                    catchGreenTrigger();
                    break;
                case 6:
                    diff = (Match.MATCH_HIT_LOCKOUT_MILLIS + Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS) / 2;
                    state = "Double Touch (at half of lockout time; " +
                            String.format("%d", diff) + "ms apart) ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    //h.postDelayed(r, diff);
                    red.lastTrigger = System.currentTimeMillis();
                    mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                    catchRedTrigger();
                    Thread.sleep(diff);
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchGreenTrigger();
                    break;
                case 7:
                    diff = Match.MATCH_HIT_LOCKOUT_MILLIS + Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS;
                    state = "Double Touch (at edge of lockout time; " +
                            String.format("%d", diff) + "ms apart) ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    //h.postDelayed(r, diff);
                    red.lastTrigger = System.currentTimeMillis();
                    mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                    catchRedTrigger();
                    Thread.sleep(diff);
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchGreenTrigger();
                    break;
                case 8:
                    diff = Match.MATCH_HIT_LOCKOUT_MILLIS + Match.MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS * 2;
                    state = "Two Touches (after lockout time; " +
                            String.format("%d", diff) + "ms apart) ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    //h.postDelayed(r, diff);
                    red.lastTrigger = System.currentTimeMillis();
                    mLog.append("Red: Trigger at " + String.format(LOG_DECIMAL_FORMAT, red.lastTrigger) + "\n");
                    catchRedTrigger();
                    Thread.sleep(diff);
                    green.lastTrigger = System.currentTimeMillis();
                    mLog.append("Green: Trigger at " + String.format(LOG_DECIMAL_FORMAT, green.lastTrigger) + "\n");
                    catchGreenTrigger();
                case 9:
                    state = "Stopping Match ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    initiateStopMatch();
                    break;
                case 10:
                    state = "Self-Test Complete ...\n";
                    mLog.append(state);
                    Toast.makeText(activity, state, Toast.LENGTH_SHORT).show();
                    if (red.service == null || red.service.getState() != CommunicationService.STATE_CONNECTED) {
                        red.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, RED_PLAYER);
                    }
                    if (green.service == null || green.service.getState() != CommunicationService.STATE_CONNECTED) {
                        green.status = Fencer.FENCER_STATUS_NOT_READY;
                        setupUI(UI_TYPE_DISCONNECT, GREEN_PLAYER);
                    }
                    break;
                default:
                    match.testStep = 100;
                    match.testMode = false;
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "selfTestStep(" + String.format("%d", step) + "): " + e.getMessage());
        }
    }

    private class NTPTask extends AsyncTask<Integer, Integer, Long> {

        private int matchPhase = 0;

        protected Long doInBackground(Integer... phase) {
            try {
                matchPhase = phase[0];
                if (red.service != null && green.service != null) {
                    Log.d(TAG, "Red Time Sync request sent at System Time " + String.format("%d", System.currentTimeMillis()));
                    red.service.write(Constants.REQUEST_TIMESYNC.getBytes());
                    Log.d(TAG, "Green Time Sync request sent at System Time " + String.format("%d", System.currentTimeMillis()));
                    green.service.write(Constants.REQUEST_TIMESYNC.getBytes());
                    return getNTPTime();
                } else {
                    return NTP_FAILURE;
                }
            } catch (Exception e) {
                Log.e(TAG, "NTPTask doInBackground(): " + e.getMessage());
                return NTP_FAILURE;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            switch (matchPhase) {
                case MATCH_PHASE_START:
                    match.startTime = result;
                    if (match.startTime == NTP_FAILURE) {
                        match.startTime = System.currentTimeMillis();
                        match.isStartTimeLocal = true;
                    } else {
                        match.isStartTimeLocal = false;
                    }
                    startMatch();
                    /*
                    matchStartTime = result;
                    if (matchStartTime == NTP_FAILURE) {
                        matchStartTime = System.currentTimeMillis();
                        isMatchStartTimeLocal = true;
                    } else {
                        isMatchStartTimeLocal = false;
                    }
                    startMatch();
                    */
                    break;
                case MATCH_PHASE_STOP:
                    match.stopTime = result;
                    if (match.stopTime == NTP_FAILURE) {
                        match.stopTime = System.currentTimeMillis();
                        match.isStopTimeLocal = true;
                    } else {
                        match.isStopTimeLocal = false;
                    }
                    stopMatch();
                    /*
                    matchStopTime = result;
                    if (matchStopTime == NTP_FAILURE) {
                        matchStopTime = System.currentTimeMillis();
                        isMatchStopTimeLocal = true;
                    } else {
                        isMatchStopTimeLocal = false;
                    }
                    stopMatch();
                    */
                    break;
                default:
                    Log.d(TAG, "NTPTask onPostExecute(): MATCH_PHASE (" + Integer.toString(matchPhase) + ") not found.");
                    break;
            }
        }
    }
}
