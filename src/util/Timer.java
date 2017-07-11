package util;

/**
 * Created by Bernardo on 11/07/2017.
 */
public class Timer {

    private long chronometerStart;

    public void timerOn(){
        chronometerStart = System.currentTimeMillis();
    }

    public Double timerOff(){
        long chronometerStop = System.currentTimeMillis();
        return (chronometerStop - chronometerStart) / 1000.0;
    }

}