package tv.piratemedia.lightcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends FragmentActivity {

    private ZonesPagerAdapter FragAdapter;
    private ViewPager ZonePager;
    private DismissOverlayView mDismissOverlayView;
    private GestureDetector mGestureDetector;
    public GoogleApiClient mGoogleApiClient;
    public Boolean isRound = false;
    private Boolean disableTouch = false;
    private BroadcastReceiver bc;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShapeWear.initShapeWear(this);

        ShapeWear.setOnShapeChangeListener(new ShapeWear.OnShapeChangeListener() {
            @Override
            public void shapeDetected(ShapeWear.ScreenShape screenShape) {
                //Do your stuff here for example:
                switch (screenShape){
                    case MOTO_ROUND:
                    case ROUND:
                        isRound = true;
                        break;
                    case RECTANGLE:
                        isRound = false;
                        break;
                }
                if(isRound) {
                    findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.color_border));
                } else {
                    findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.color_border_square));
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss);
        mDismissOverlayView.setIntroText(R.string.intro_text);
        mDismissOverlayView.showIntroIfNecessary();

        mGestureDetector = new GestureDetector(this, new LongPressListener());

        FragAdapter =
                new ZonesPagerAdapter(getSupportFragmentManager());
        ZonePager = (ViewPager) findViewById(R.id.pager);
        ZonePager.setAdapter(FragAdapter);
        ZonePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (FragAdapter.isColor(i)) {
                    if (isRound) {
                        findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.color_border));
                    } else {
                        findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.color_border_square));
                    }
                } else {
                    if (isRound) {
                        findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.white_border));
                    } else {
                        findViewById(R.id.rim).setBackground(getResources().getDrawable(R.drawable.white_border_square));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        bc = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Received", intent.getAction());
                if(intent.getAction().equals("tv.piratemedia.lightcontroler.wear.updated_zones")) {
                    for(int i = 0; i < FragAdapter.getCount(); i++) {
                        Log.d("Received", "update name in fragment");
                        try {
                            ColorZoneFragment f = (ColorZoneFragment) FragAdapter.getItem(i);
                            f.updateName();
                        } catch(ClassCastException e) {
                            WhiteZoneFragment f = (WhiteZoneFragment) FragAdapter.getItem(i);
                            f.updateName();
                        }
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(
                "tv.piratemedia.lightcontroler.wear.updated_zones");
        registerReceiver(bc, intentFilter);

        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.size(); i++) {
                        final Node node = nodes.get(i);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/zones", null);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        unregisterReceiver(bc);
        super.onPause();
    }

    private float startX = 0;
    private float startY = 0;
    private int currentStep = 0;
    private int newStep = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = event.getX();
            startY = event.getY();
            currentStep = 0;
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            float valX = event.getX();
            float valY = event.getY();
            float changeX = event.getX() - startX;
            float changeY = event.getY() - startY;
            float difX = changeX > 0 ? changeX : -changeX;
            float difY = changeY > 0 ? changeY : -changeY;
            if((difY > difX)) {
                if(ZonePager.getCurrentItem() <= 4) {
                    newStep = 20 - (int)(valY / 12.5f);
                    if(newStep > 20) {
                        newStep = 20;
                    } else if(newStep < 0) {
                        newStep = 0;
                    }
                    if(newStep != currentStep) {
                        currentStep = newStep;
                        if (mGoogleApiClient != null) {
                            final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                @Override
                                public void onResult(NodeApi.GetConnectedNodesResult result) {
                                    final List<Node> nodes = result.getNodes();
                                    if (nodes != null) {
                                        for (int i = 0; i < nodes.size(); i++) {
                                            final Node node = nodes.get(i);
                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/" + ZonePager.getCurrentItem() + "/level/"+ currentStep, null);
                                        }
                                    }
                                }
                            });
                        }
                        LinearLayout container = (LinearLayout) findViewById(R.id.brightnesscontainer);
                        TextView text = (TextView) findViewById(R.id.brightnesstext);
                        text.setText((currentStep * 5) + "%");
                        container.setVisibility(View.VISIBLE);
                    }
                } else {
                    newStep = -(int) (changeY / 12.5f);
                    if(newStep > 10) {
                        newStep = 10;
                    } else if(newStep < -10) {
                        newStep = -10;
                    }
                    if(newStep > currentStep) {
                        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                final List<Node> nodes = result.getNodes();
                                if (nodes != null) {
                                    for (int j = 0; j < nodes.size(); j++) {
                                        final Node node = nodes.get(j);
                                        int steps = newStep - currentStep;
                                        for(int i = 0; i < steps; i++) {
                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/" + ZonePager.getCurrentItem() + "/level/1", null);
                                            currentStep++;
                                        }
                                    }
                                }
                            }
                        });
                    } else if(newStep < currentStep) {
                        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                final List<Node> nodes = result.getNodes();
                                if (nodes != null) {
                                    for (int j = 0; j < nodes.size(); j++) {
                                        final Node node = nodes.get(j);
                                        int steps = currentStep - newStep;
                                        for(int i = 0; i < steps; i++) {
                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/" + ZonePager.getCurrentItem() + "/level/-1", null);
                                            currentStep--;
                                        }
                                    }
                                }
                            }
                        });
                    }
                    LinearLayout container = (LinearLayout) findViewById(R.id.brightnesscontainer);
                    TextView text = (TextView) findViewById(R.id.brightnesstext);
                    String txt = newStep > 0 ? ("+" + newStep) : (newStep + "");
                    text.setText(txt);
                    container.setVisibility(View.VISIBLE);
                }
            }
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            LinearLayout container = (LinearLayout) findViewById(R.id.brightnesscontainer);
            container.setVisibility(View.GONE);
        }

        return mGestureDetector.onTouchEvent(event)
                || super.dispatchTouchEvent(event);
    }

    private class LongPressListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mDismissOverlayView.show();
        }
    }
}

