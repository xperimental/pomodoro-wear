package net.sourcewalker.android.pomodorowear;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.util.Date;

public class PomodoroService extends Service {

    private static final int WORK_TIME = 25;
    private static final int PAUSE_TIME = 5;
    private static final int LONG_PAUSE_TIME = 15;

    private PomodoroState state;
    private Date endTime;
    private int workCount;
    private Binder binder;
    private TickHandler tickHandler;

    public PomodoroState getState() {
        return state;
    }

    public Date getEndTime() {
        return endTime;
    }

    public PomodoroState getNextState() {
        switch (state) {
            case STOPPED:
            default:
                return PomodoroState.STOPPED;
            case WORK:
                if (workCount < 5) {
                    return PomodoroState.PAUSE;
                } else {
                    return PomodoroState.LONG_PAUSE;
                }
            case PAUSE:
            case LONG_PAUSE:
                return PomodoroState.WORK;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        tickHandler = new TickHandler();
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
        tickHandler.sendEmptyMessage(0);
    }

    private Date inMinutes(int minutes) {
        long add = minutes * 60000;
        Date now = new Date();
        return new Date(now.getTime() + add);
    }

    public void tick() {
        Date now = new Date();
        if (!now.before(endTime)) {
            state = getNextState();
            int mins;
            switch (state) {
                case STOPPED:
                default:
                    mins = 0;
                    break;
                case WORK:
                    mins = WORK_TIME;
                    workCount++;
                    break;
                case PAUSE:
                    mins = PAUSE_TIME;
                    break;
                case LONG_PAUSE:
                    mins = LONG_PAUSE_TIME;
                    break;
            }
            if (mins > 0) {
                endTime = inMinutes(mins);
            } else {
                reset();
            }
        }
    }

    public final class Binder extends android.os.Binder {

        public PomodoroService getService() {
            return PomodoroService.this;
        }

    }

    private final class TickHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (state != PomodoroState.STOPPED) {
                tick();
                sendEmptyMessageDelayed(0, 10000);
            }
        }

    }
}
