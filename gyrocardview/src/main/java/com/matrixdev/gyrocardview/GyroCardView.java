package com.matrixdev.gyrocardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import java.util.Arrays;

public class GyroCardView extends CardView {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Sensor sensor2;
    private SensorEventListener2 sensorListener;
    private ViewPropertyAnimator animator;
    Matrix matrix = new Matrix();
    private CountDownTimer countDown;
    private boolean isGlareEnabled;
    private int glareColor;
    private int intensity;
    private Sensor sensor3;
    private float XOffset = 0.7f;
    private boolean isVerticalRotationEnabled;
    private SensorEventListener2 sensorListener3;

    public GyroCardView(@NonNull Context context) {
        super(context);
        init();
    }

    public GyroCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParameters(attrs);
        init();
    }


    public GyroCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void initParameters(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.GyroCardView);

        isGlareEnabled = attributes.getBoolean(R.styleable.GyroCardView_isGlareEnabled, true);
        isVerticalRotationEnabled = attributes.getBoolean(R.styleable.GyroCardView_isVertialRotationEnabled, false);
        glareColor = attributes.getColor(R.styleable.GyroCardView_glareColor, Color.WHITE);
        intensity = attributes.getInteger(R.styleable.GyroCardView_intensity, 4);

        attributes.recycle();
    }

    private void init() {
        setStaticTransformationsEnabled(true);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensor3 = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        sensorListener = new SensorEventListener2() {
            private float[] mMagnetometerData = new float[3];
            private float[] mAccelerometerData = new float[3];
            float orientationValues[] = new float[3];

            @Override
            public void onFlushCompleted(Sensor sensor) {

            }

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
//                Log.d("-----*",Arrays.toString(sensorEvent.values));
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        mAccelerometerData = sensorEvent.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mMagnetometerData = sensorEvent.values.clone();
                        break;
                    default:
                        return;
                }
                float[] rotationMatrix = new float[9];
                boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                        null, mAccelerometerData, mMagnetometerData);
                SensorManager.getOrientation(rotationMatrix, orientationValues);
//                Log.d("------#",Arrays.toString(rotationMatrix));
//                Log.d("------#",Arrays.toString(orientationValues));
//                card.setRotationX(5*orientationValues[1]);
//                Log.d("-----", "" + (Math.round(orientationValues[1] * 100f) / 100f) + "|" + (Math.round(orientationValues[2] * 100f) / 100f));
                if (Math.abs(orientationValues[2]) <= 1 && Math.abs(orientationValues[1]) <= 1.5) {
                    setRotationY(intensity * orientationValues[2]);
                    if(isGlareEnabled) {
                        drawGlare(orientationValues[2], 0f);
                    }
                    invalidate();
                }
                else if (Math.abs(orientationValues[2]) >= 2 && Math.abs(orientationValues[1]) <= 1.5) {
                    orientationValues[2] = orientationValues[2]>3 ? 3 : orientationValues[2];
                    orientationValues[2] = orientationValues[2]<-3 ? -3 : orientationValues[2];
                   float factor = orientationValues[2] > 0 ? 3 - orientationValues[2] : -(orientationValues[2]+3);
                    setRotationY(intensity * factor);
                    if(isGlareEnabled) {
                        drawGlare(factor, 0f);
                    }
                    invalidate();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
//        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(sensorListener, sensor2, SensorManager.SENSOR_DELAY_FASTEST);


        sensorListener3 = new SensorEventListener2() {
            @Override
            public void onFlushCompleted(Sensor sensor) {

            }

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float percY = sensorEvent.values[0] /10;
                float percX = sensorEvent.values[1] /10;
                float targetPercX = (1-((1-percX)*Math.signum(sensorEvent.values[2]))-0.7f);
                setRotationY(intensity * percY);
                if(isVerticalRotationEnabled) {
                    setRotationX(intensity * targetPercX);
                }
                if(isVerticalRotationEnabled && isGlareEnabled){
                    drawGlare(percY,percX);
                }else if(isGlareEnabled) {
                    drawGlare(percY,0);
                }
                invalidate();
                Log.d("-----", Arrays.toString(sensorEvent.values)  );
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(sensorListener3,sensor3,SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try{
            sensorManager.unregisterListener(sensorListener3);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //    @Override
//    protected boolean getChildStaticTransformation(View child, Transformation t) {
//
//        // apply transform to child view - child triggers this call by call to `invalidate()`
//        t.getMatrix().set(matrix);
//        return true;
//    }

    public void drawGlare(final float rotation, final float percX) {
        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                float mid = .55f + (-0.2f * rotation);
                int color1 = ColorUtils.setAlphaComponent(glareColor, 100);
                int color2 = ColorUtils.setAlphaComponent(glareColor, 75);
                float xchange = 400 * percX;
                LinearGradient lg = new LinearGradient(0, 0 - Math.abs(width - height) - xchange, width , height + Math.abs(width - height) ,
                        new int[]{Color.TRANSPARENT, Color.TRANSPARENT, color1,color2, Color.TRANSPARENT},
                        new float[]{0, mid - .2f, mid, mid + .1f, mid + .2f}, Shader.TileMode.REPEAT);
                return lg;
            }
        };

        PaintDrawable p = new PaintDrawable();
        p.setShape(new RectShape());
        p.setShaderFactory(sf);

        setForeground(p);
    }

    private class MyAnimation extends Animation {
        private Matrix matrix;

        public MyAnimation(Matrix matrix) {
            this.matrix = matrix;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            t.getMatrix().set(matrix);
        }
    }
}
