package tv.piratemedia.lightcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
    private int screenHeight = 0;

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

        Point point = new Point();
        WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(point);
        screenHeight = point.y;
        brightnessValPixels = screenHeight;
        pixelsPerBrightnessStep = screenHeight / (float) BRIGHTNESS_STEPS;
        resources = getResources();
        brightnessTextContainer = (LinearLayout) findViewById(R.id.brightnesscontainer);
        txtBrightness = (TextView) findViewById(R.id.brightnesstext);

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
    private float deltaX = 0;
    private float deltaY = 0;
    private boolean swipingVertical = false;

    private float brightnessValPixels = 0;
    private float brightnessValPixelsTemp = 0;
    private int brightnessVal = 0;
    private int oldBrightnessVal = -1;

    private final int BRIGHTNESS_STEPS = 20;
    private float pixelsPerBrightnessStep = 1;

    Resources resources;
    LinearLayout brightnessTextContainer;
    TextView txtBrightness;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            deltaX = event.getX() - startX;
            deltaY = event.getY() - startY;

            if((Math.abs(deltaY) > Math.abs(deltaX))) {
                // Save start position for future reference
                startX = event.getX();
                startY = event.getY();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            ZonePager.setEnabled(true);

            deltaX = event.getX() - startX;
            deltaY = event.getY() - startY;

            if((Math.abs(deltaY) > Math.abs(deltaX))) {
                // Apply new brightness setting
                deltaY = event.getY() - startY;
                brightnessValPixels -= deltaY;
                brightnessValPixels = Math.max(brightnessValPixels, 0);
                brightnessValPixels = Math.min(brightnessValPixels, screenHeight);
                brightnessVal = Math.round(brightnessValPixels / pixelsPerBrightnessStep);
                System.out.println(pixelsPerBrightnessStep);

                // Protecting the controller from being sent more commands than it needs to be
                if (brightnessVal != oldBrightnessVal) {
                    oldBrightnessVal = brightnessVal;

                    // Send new brightness val to phone to send to hub
                    if (mGoogleApiClient != null) {
                        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                final List<Node> nodes = result.getNodes();
                                if (nodes != null) {
                                    for (int i = 0; i < nodes.size(); i++) {
                                        final Node node = nodes.get(i);
                                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/" + ZonePager.getCurrentItem() + "/level/"+ brightnessVal, null);
                                    }
                                }
                            }
                        });
                    }
                }
            }

            brightnessTextContainer.setVisibility(View.INVISIBLE);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            deltaX = event.getX() - startX;
            deltaY = event.getY() - startY;

            if((Math.abs(deltaY) > Math.abs(deltaX))) {
                // Decide if brightness swipe
                if (Math.abs(deltaY) > 20) {
                    // If it is, disable the background fragment to prevent it from being inadvertently pressed
                    ZonePager.setEnabled(false);
                }

                // Calculate new brightness step, but only display the current value on the screen
                System.out.println(deltaY);

                brightnessValPixelsTemp = brightnessValPixels;

                brightnessValPixelsTemp -= deltaY;
                brightnessValPixelsTemp = Math.max(brightnessValPixelsTemp, 0);
                brightnessValPixelsTemp = Math.min(brightnessValPixelsTemp, screenHeight);

                int brightnessPercentage = Math.round((brightnessValPixelsTemp / screenHeight) * 100);
                txtBrightness.setText(resources.getString(R.string.brightness_percentage, brightnessPercentage));
                brightnessTextContainer.setVisibility(View.VISIBLE);
            }
        }

        return mGestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event);
    }

    private class LongPressListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mDismissOverlayView.show();
        }
    }
}

