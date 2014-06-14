package net.sourcewalker.android.pomodorowear;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

public class PomodoroActivity extends Activity {

    private Connection connection;
    private UpdateHandler updateHandler;

    private TextView phaseView;
    private TextView timeView;
    private TextView nextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        connection = new Connection();
        updateHandler = new UpdateHandler();

        phaseView = (TextView) findViewById(R.id.phase);
        timeView = (TextView) findViewById(R.id.time);
        nextView = (TextView) findViewById(R.id.next);
        updateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, PomodoroService.class));
        bindService(new Intent(this, PomodoroService.class), connection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(connection);
    }

    public void onStartPressed(View view) {
        if (connection.isConnected()) {
            connection.service.start();
        }
    }

    public void onResetPressed(View view) {
        if (connection.isConnected()) {
            connection.service.reset();
        }
    }

    private void updateViews() {
        if (connection.isConnected()) {
            timeView.setText(connection.getRemaining());
            nextView.setText(connection.getNextState().toString());
            PomodoroState state = connection.getState();
            phaseView.setText(state.toString());
            switch (state) {
                case STOPPED:
                    findViewById(R.id.start).setEnabled(true);
                    findViewById(R.id.reset).setEnabled(false);
                    break;
                case WORK:
                case PAUSE:
                    findViewById(R.id.start).setEnabled(false);
                    findViewById(R.id.reset).setEnabled(true);
                    break;
            }
        } else {
            phaseView.setText(R.string.not_connected);
            timeView.setText(R.string.not_connected);
            nextView.setText(R.string.not_connected);

            findViewById(R.id.start).setEnabled(false);
            findViewById(R.id.reset).setEnabled(false);
        }
    }

    private final class Connection implements ServiceConnection {

        private PomodoroService service;

        public boolean isConnected() {
            return service != null;
        }

        public PomodoroState getState() {
            if (isConnected()) {
                return service.getState();
            } else {
                return PomodoroState.STOPPED;
            }
        }

        public String getRemaining() {
            if (!isConnected() || getState() == PomodoroState.STOPPED) {
                return getString(R.string.not_started);
            } else {
                Date now = new Date();
                Date end = service.getEndTime();
                if (end == null) {
                    return getString(R.string.not_started);
                } else {
                    long diff = end.getTime() - now.getTime();
                    if (diff > 1000) {
                        if (diff > 60000) {
                            return getString(R.string.remaining_minutes, Math.ceil(diff / 60000.0));
                        } else {
                            return getString(R.string.remaining_seconds, diff / 1000.0);
                        }
                    } else {
                        return getString(R.string.soon);
                    }
                }
            }
        }

        public PomodoroState getNextState() {
            if (isConnected()) {
                return service.getNextState();
            } else {
                return PomodoroState.STOPPED;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            this.service = ((PomodoroService.Binder) binder).getService();
            updateHandler.sendEmptyMessage(0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            this.service = null;
        }

    }

    private final class UpdateHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (connection.isConnected()) {
                updateViews();
                this.sendEmptyMessageDelayed(0, 500);
            }
        }

    }

}
