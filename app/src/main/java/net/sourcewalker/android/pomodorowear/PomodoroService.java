package net.sourcewalker.android.pomodorowear;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

public class PomodoroService extends Service {

    private static final int WORK_TIME = 1;
    private PomodoroState state;
    private Date endTime;
    private int workCount;
    private Binder binder;

    public PomodoroState getState() {
        return state;
    }

    public Date getEndTime() {
        return endTime;
    }

    public PomodoroState getNextState() {
        return PomodoroState.PAUSE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Pomodoro", "serviceCreate");

        binder = new Binder();
        reset();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (state == PomodoroState.STOPPED) {
            stopSelf();
        }
        return false;
    }

    public void reset() {
        state = PomodoroState.STOPPED;
        endTime = null;
        workCount = 0;
    }

    public void start() {
        state = PomodoroState.WORK;
        endTime = inMinutes(WORK_TIME);
        workCount = 1;
    }

    private Date inMinutes(int minutes) {
        long add = minutes * 60000;
        Date now = new Date();
        return new Date(now.getTime() + add);
    }

    public void stop() {
        endTime = null;
    }

    public final class Binder extends android.os.Binder {

        public PomodoroService getService() {
            return PomodoroService.this;
        }

    }

}
