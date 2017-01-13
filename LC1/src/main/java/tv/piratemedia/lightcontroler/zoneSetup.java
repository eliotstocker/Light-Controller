package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by eliotstocker on 22/10/2016.
 */

public class zoneSetup extends ActionBarActivity {
    public boolean adding = true;
    private static controlCommands Controller;
    private Handler mHandler;

    private String Type;
    private int Zone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonesetup);

        if(!getIntent().hasExtra("type") || !getIntent().hasExtra("zone")) {
            finish();
            return;
        }

        Type = getIntent().getStringExtra("type");
        Zone = getIntent().getIntExtra("zone", -1);
        Log.d("Zone Setup", "setting up '"+Type+"' lights in Zone: "+Zone);

        final controller.ControllerPager cp = new controller.ControllerPager(getSupportFragmentManager(), null, this);

        TextView info = (TextView) findViewById(R.id.info);

        String title = "Unknown";
        if(getIntent().hasExtra("index")) {
            title = cp.getPageTitle(getIntent().getIntExtra("index", 0)).toString();
        } else {
            title = cp.getPageTitle(cp.getIndex(Type, Zone)).toString();
        }
        info.setText("Setup " + Type + " Zone: " + title);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 999) {
                    step3Complete();
                }
            }
        };

        Controller = new controlCommands(this, mHandler);
    }

    public void AddBulb(final View v) {
        adding = true;
        findViewById(R.id.step1).setVisibility(View.GONE);
        findViewById(R.id.step2).setVisibility(View.VISIBLE);
    }

    public void RemoveBulb(final View v) {
        adding = false;
        findViewById(R.id.step1).setVisibility(View.GONE);
        findViewById(R.id.step2).setVisibility(View.VISIBLE);
    }

    public void step2Next(final View v) {
        findViewById(R.id.step2).setVisibility(View.GONE);
        findViewById(R.id.step3).setVisibility(View.VISIBLE);
    }

    public void step3Next(final View v) {
        findViewById(R.id.step3).setVisibility(View.GONE);
        findViewById(R.id.running).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(Type.equals("white") && !adding) {
                        Thread.sleep(500);
                        for(int i = 0; i < 5; i++) {
                            Log.d("Zone Setup", "Sending Lights on for '"+Type+"' zone:"+Zone);
                            Controller.LightsOn(Type, Zone);
                            Thread.sleep(200);
                        }
                    } else {
                        Controller.LightsOn(Type, Zone);
                        if (!adding) {
                            Controller.setToWhite(Type, Zone);
                        }
                        Thread.sleep(500);
                        Controller.LightsOn(Type, Zone);
                        if (!adding) {
                            Controller.setToWhite(Type, Zone);
                        }
                        Thread.sleep(1000);
                        Controller.LightsOn(Type, Zone);
                        if (!adding) {
                            Controller.setToWhite(Type, Zone);
                        }
                        Thread.sleep(1000);
                        Controller.LightsOn(Type, Zone);
                        if (!adding) {
                            Controller.setToWhite(Type, Zone);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(999);
            }
        }).start();
    }

    public void step3Complete() {
        findViewById(R.id.running).setVisibility(View.GONE);
        findViewById(R.id.step4).setVisibility(View.VISIBLE);
        if(adding) {
            ((TextView) findViewById(R.id.finishedtext)).setText(R.string.zone_setup_add_bulb_step3);
        } else {
            ((TextView) findViewById(R.id.finishedtext)).setText(R.string.zone_setup_remove_bulb_step3);
        }
    }

    public void end(final View v) {
        finish();
    }

    public void restart(final View v) {
        findViewById(R.id.step4).setVisibility(View.GONE);
        findViewById(R.id.step1).setVisibility(View.VISIBLE);
    }
}
