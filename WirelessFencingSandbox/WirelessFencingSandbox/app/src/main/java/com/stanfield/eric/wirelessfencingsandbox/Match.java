package com.stanfield.eric.wirelessfencingsandbox;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Eric Stanfield on 12/1/2015.
 */
public class Match {

    // Debugging
    private static final String TAG = "Match";

    // Test
    public static final int MATCH_TEST = 0;
    public static final int MATCH_TEST_POINTS_FOR_WIN = 99;
    public static final int MATCH_TEST_PERIODS = 0; // really there is 1, but call it 0 so that label doesn't show
    public static final int MATCH_TEST_PERIOD_TIME_LIMIT = 60000; // milliseconds

    // Pool
    public static final int MATCH_POOL = 1;
    public static final int MATCH_POOL_POINTS_FOR_WIN = 5;
    public static final int MATCH_POOL_PERIODS = 0; // really there is 1, but call it 0 so that label doesn't show
    public static final int MATCH_POOL_PERIOD_TIME_LIMIT = 180000; // milliseconds

    // Elimination
    public static final int MATCH_ELIMINATION = 2;
    public static final int MATCH_ELIMINATION_POINTS_FOR_WIN = 15;
    public static final int MATCH_ELIMINATION_PERIODS = 3;
    public static final int MATCH_ELIMINATION_PERIOD_TIME_LIMIT = 180000; // milliseconds

    // Lockout
    public static final int MATCH_HIT_LOCKOUT_MILLIS = 40;
    public static final int MATCH_HIT_LOCKOUT_TOLERANCE_MILLIS = 10;

    // Break
    public static final int MATCH_BREAK_TIME_LIMIT = 60000;

    // Cards
    public static final int MATCH_CONVERT_CARD_YELLOW_TO_RED = 2; // 2 yellow cards = 1 red card

    // Properties
    public int type = 1; // This should match the default value of the UI control for match type
    public boolean isReady = false; // determines if match is ready to begin or not
    public boolean isStarted = false; // determines if match is currently underway or not
    public boolean isHalted = false;  // determines whether the match is currently in a halt state
    public boolean triggerLockoutInitiated = false; // determines if the trigger lockout timer has been started
    public boolean triggerLockout = false; // determines if triggers are allowed to register
    public boolean evaluationLockout = false; // determines if a score and match evaluation is allowed
    public boolean isStartTimeLocal = false;
    public boolean isStopTimeLocal = false;
    public long startTime = 0; // time of match start
    public long stopTime = 0;  // time of match stop
    //public long pointStartTime = 0; // time of point start
    //public long pointStopTime = 0; // time of point stop
    //public double hitTimeDiff = 0.0; // time between hits (before lockout)
    public int periodLimit = 0;
    public long periodTimeLimit = 0;
    public int period = 0;
    public long periodTimeRemaining = 0;
    public long breakTimeRemaining = 0;
    public DateFormat periodTimerFormat;
    public CountDownTimer periodTimer;
    public CountDownTimer breakTimer;
    private String validationMessage = "";

    // Fencers
    public Fencer red;
    public Fencer green;

    // Score
    public boolean isTieAllowed = true;
    public boolean isOvertimeAllowed = true;
    public boolean isScoreFinal = false;

    // Logging
    //public boolean logVerbose = true;//false;
    public boolean testMode = false;
    public int testStep = 0;
    private String logConfigSummary = "";

    public Match()
    {
        this.isReady = false;
        this.isStarted = false;
        this.isHalted = false;
        this.periodTimerFormat = new SimpleDateFormat("mm:ss");
    }

    public void configure() {
        try {
            if (this.testMode) this.type = MATCH_TEST;

            double minutes = 0.0;

            switch (this.type) {
                case MATCH_POOL:
                    this.periodLimit = MATCH_POOL_PERIODS;
                    this.periodTimeLimit = MATCH_POOL_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                    minutes = MATCH_POOL_PERIOD_TIME_LIMIT / 1000.0 / 60.0;
                    logConfigSummary = "WFS: Match configured for POOL (" +
                        "First to " + String.format("%d", MATCH_POOL_POINTS_FOR_WIN) +
                        " or " +
                        String.format("%.2f", minutes) + " minutes" +
                        ")\n";
                    break;
                case MATCH_ELIMINATION:
                    this.periodLimit = MATCH_ELIMINATION_PERIODS;
                    this.periodTimeLimit = MATCH_ELIMINATION_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                    minutes = MATCH_ELIMINATION_PERIOD_TIME_LIMIT / 1000.0 / 60.0;
                    logConfigSummary = "WFS: Match configured for ELIMINATION (" +
                        "First to " + String.format("%d", MATCH_ELIMINATION_POINTS_FOR_WIN) +
                        " or " +
                        String.format("%.2f", minutes) + " minutes" +
                        ")\n";
                    break;
                case MATCH_TEST:
                    this.periodLimit = MATCH_TEST_PERIODS;
                    this.periodTimeLimit = MATCH_TEST_PERIOD_TIME_LIMIT + 5; // must add 5 because timer stalls at the end
                    minutes = MATCH_TEST_PERIOD_TIME_LIMIT / 1000.0 / 60.0;
                    logConfigSummary = "WFS: Match configured for TEST (" +
                        "First to " + String.format("%d", MATCH_TEST_POINTS_FOR_WIN) +
                        " or " +
                        String.format("%.2f", minutes) + " minutes" +
                        ")\n";
                    break;
                default:
                    //Invalid Match Type
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "configure(): " + e.getMessage());
        }
    }
    /*
    public void start() {
        try {

            if (this.isReady) {

                if (this.isStartTimeLocal) {
                    if (logVerbose) log.append("WFS: NTP Start Time request failed. Using System time.\n");
                }
                red.timeOffset = this.startTime - red.timeSync; // if negative, then TimeSync was before Start
                green.timeOffset = this.startTime - green.timeSync; // if negative, then TimeSync was before Start
                Log.d(TAG, "matchStartTime = " + String.valueOf(this.startTime) +
                        ", redTimeSync=" + String.format(LOG_DECIMAL_FORMAT, red.timeSync) +
                        ", greenTimeSync=" + String.format(LOG_DECIMAL_FORMAT, green.timeSync));

                // Configure match
                this.configure();
                this.period = 1;
                red.score = 0;
                green.score = 0;

                // Start timer
                //initiateMatchPeriodTimer(matchPeriodTimeLimit);

                // Start match
                this.isStarted = true;
                setupUI(UI_TYPE_MATCH_START, 0); //??? not sure what to do about this
                resumeMatch();
                log.append("WFS: Starting Match at " + String.valueOf(this.startTime) + "\n");

                // Set fencer status
                red.status = Fencer.FENCER_STATUS_FENCING;
                green.status = Fencer.FENCER_STATUS_FENCING;

            }

        } catch (Exception e) {
            Log.e(TAG, "startMatch(): " + e.getMessage());
        }
    }
    */
    public boolean areAllFencersReady() {
        if (red.status == Fencer.FENCER_STATUS_READY
                && green.status == Fencer.FENCER_STATUS_READY) {
            return true;
        }

        return false;
    }

    public String getConfigSummary() {
        return logConfigSummary;
    }
}
