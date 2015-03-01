package dbuhler.morpher;

import android.os.CountDownTimer;

import dbuhler.morpher.activities.PlayActivity;

/**
 * This class describes a player for playing back or stepping through a series of frames.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class Player
{
    public static final int DEFAULT_FRAME_RATE = 20;
    public static final int MIN_FRAME_RATE     = 1;
    public static final int MAX_FRAME_RATE     = 100;

    private PlayActivity   activity;
    private CountDownTimer timer;
    private Direction      playDirection;
    private int numSteps;
    private int index;

    /**
     * Describes the direction the player is playing the frames.
     */
    public enum Direction
    {
        NONE,
        FORWARDS,
        BACKWARDS
    }

    /**
     * Creates a new player.
     *
     * @param activity The calling activity.
     */
    public Player(PlayActivity activity)
    {
        this.activity = activity;
        playDirection = Direction.NONE;
        numSteps = 0;
        index    = 0;
    }

    /**
     * @return The player's playing direction.
     */
    public Direction getPlayDirection()
    {
        return playDirection;
    }

    /**
     * Sets the number of steps to the given value.
     *
     * @param numSteps The new number of steps.
     */
    public void setNumSteps(int numSteps)
    {
        this.numSteps = numSteps;
    }

    /**
     * Displays the next frame in the activity.
     */
    public void showNextFrame()
    {
        if (index < numSteps)
        {
            activity.showFrame(++index);
        }
    }

    /**
     * Displays the previous frame in the activity.
     */
    public void showPrevFrame()
    {
        if (index > 0)
        {
            activity.showFrame(--index);
        }
    }

    /**
     * Plays the frames with the given frame rate in forward direction.
     *
     * @param frameRate The frame rate for playback.
     */
    public void playForwards(int frameRate)
    {
        if (index == numSteps)
        {
            // Start over if at the end.
            index = 0;
            activity.showFrame(0);
        }

        playDirection = Direction.FORWARDS;
        startTimer(frameRate);
    }

    /**
     * Plays the frames with the given frame rate in backward direction.
     *
     * @param frameRate The frame rate for playback.
     */
    public void playBackwards(int frameRate)
    {
        if (index == 0)
        {
            // Start over if at the beginning.
            index = numSteps;
            activity.showFrame(numSteps);
        }

        playDirection = Direction.BACKWARDS;
        startTimer(frameRate);
    }

    /**
     * Pauses playback.
     */
    public void pause()
    {
        playDirection = Direction.NONE;

        if (timer != null)
        {
            timer.cancel();
        }
    }

    /**
     * Starts a timer for the given frame rate.
     *
     * @param frameRate The frame rate for playback.
     */
    private void startTimer(int frameRate)
    {
        int period = 1000 / frameRate;
        int n = 0;

        // Determine how many frames are left in the current direction.
        switch (playDirection)
        {
            case FORWARDS:  n = numSteps - index--; break;
            case BACKWARDS: n = index++;            break;
        }

        // Includes a small buffer time to make sure the last tick is not skipped.
        timer = new CountDownTimer((n + 1) * period + 100, period)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                switch (playDirection)
                {
                    case FORWARDS:  showNextFrame(); break;
                    case BACKWARDS: showPrevFrame(); break;
                }
            }

            @Override
            public void onFinish()
            {
                playDirection = Direction.NONE;
                activity.resetPlayButtons();
            }
        };

        timer.start();
    }
}