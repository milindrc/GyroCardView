package com.matrixdev.gyrocardsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private OrientationEventListener listener;
    private int rotation;
    private SensorManager sensorManager;
    private Sensor sensor;
    private TriggerEventListener triggerEventListener;
    private Sensor sensor2;
    private SensorEventListener2 sensorListener;
    private View card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        triggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                // Do work
//                Log.d("-----", Arrays.toString(event.values));
            }
        };

//        sensorManager.requestTriggerSensor(triggerEventListener, sensor);


//        listener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                rotation = ((orientation + 45) / 90) % 4;
//                Log.d("----",""+rotation);
//            }
//        };
//        if (listener.canDetectOrientation()) listener.enable();
//        else listener = null; // Orientation detection not supported
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (listener != null) listener.disable();
    }

}
