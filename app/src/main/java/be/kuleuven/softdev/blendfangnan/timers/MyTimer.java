package be.kuleuven.softdev.blendfangnan.timers;

import java.util.Timer;
import java.util.TimerTask;

public class MyTimer {
    private int seconds;
    private int secondsLeft;
    private String label;
    private boolean active;
    private boolean initiated;

    Timer timer;
    TimerTask task;

    public MyTimer() {
    }

    public MyTimer(int seconds) {
        this.seconds = seconds;
        this.secondsLeft = seconds;
        label = null;
    }

    public MyTimer(int seconds, String label) {
        this.seconds = seconds;
        this.secondsLeft = seconds;
        this.label = label;
    }

    public boolean isInitiated() {
        return initiated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String showSecondsLeftProperly() {
        String time = null;
        int minutes;
        int seconds;
        if (secondsLeft > 59) {
            minutes = secondsLeft / 60;
            seconds = secondsLeft - (60 * minutes);
            if (minutes < 10)
                time = "0" + minutes + ":";
            else
                time = minutes + ":";
            if (seconds < 10)
                return time + "0" + seconds;
            else
                return time + seconds;
        }
        else {
            time = "00:";
            if (secondsLeft < 10)
                return time + "0" + secondsLeft;
            else
                return time + secondsLeft;
        }
    }

    public String showSecondsProperly() {
        String time = null;
        int minutes;
        int seconds;
        if (this.seconds > 59) {
            minutes = this.seconds / 60;
            seconds = this.seconds - (60 * minutes);
            if (minutes < 10)
                time = "0" + minutes + ":";
            else
                time = minutes + ":";
            if (seconds < 10)
                return time + "0" + seconds;
            else
                return time + seconds;
        }
        else {
            time = "00:";
            if (this.seconds < 10)
                return time + "0" + this.seconds;
            else
                return time + this.seconds;
        }
    }

    public void start() {
        if (!isActive()) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    secondsLeft--;
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000);
            setActive(true);
            initiated = true;
        }
    }

    public void pause() {
        if (isActive()) {
            timer.cancel();
            setActive(false);
        }
    }

    public void stop() {
        if (isActive())
            timer.cancel();
        secondsLeft = seconds;
        setActive(false);
        initiated = false;
    }

    public void decrement() {
        secondsLeft--;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFinished() {
        if (secondsLeft == 0)
            return true;
        else
            return false;
    }

    public void setInitiated(boolean initiated) {
        this.initiated = initiated;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
