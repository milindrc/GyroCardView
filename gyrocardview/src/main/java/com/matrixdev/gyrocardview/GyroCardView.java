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
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

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
        glareColor = attributes.getColor(R.styleable.GyroCardView_glareColor, Color.WHITE);
        intensity = attributes.getInteger(R.styleable.GyroCardView_intensity, 4);

        attributes.recycle();
    }

    private void init() {
        setStaticTransformationsEnabled(true);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


//        setCardElevation(0);
//        setScaleX(.7f);
//        setScaleY(.7f);
//        countDown = new CountDownTimer(36000, 10) {
//            float i = 1;
//            @Override
//            public void onTick(long l) {
//                i = i>360?0:i;
//                setRotationY(i++);
//                setScaleX((float) (1 + (.3 * ((i%180)-90)/90)));
////                setCameraDistance(1000);
////                matrix.preRotate(i,getWidth()/2,getHeight()/2);
////                MyAnimation animation = new MyAnimation(matrix);
////                animation.setDuration(0);
////                animation.setFillAfter(true);
////                setAnimation(animation);
//            }
//
//            @Override
//            public void onFinish() {
//                i=0;
//                countDown.start();
//            }
//        };
//        countDown.start();

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
                        drawGlare(orientationValues[2]);
                    }
                    invalidate();
                }
                else if (Math.abs(orientationValues[2]) >= 2 && Math.abs(orientationValues[1]) <= 1.5) {
                    orientationValues[2] = orientationValues[2]>3 ? 3 : orientationValues[2];
                    orientationValues[2] = orientationValues[2]<-3 ? -3 : orientationValues[2];
                   float factor = orientationValues[2] > 0 ? 3 - orientationValues[2] : -(orientationValues[2]+3);
                    setRotationY(intensity * factor);
                    if(isGlareEnabled) {
                        drawGlare(factor);
                    }
                    invalidate();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorListener, sensor2, SensorManager.SENSOR_DELAY_FASTEST);

    }


//    @Override
//    protected boolean getChildStaticTransformation(View child, Transformation t) {
//
//        // apply transform to child view - child triggers this call by call to `invalidate()`
//        t.getMatrix().set(matrix);
//        return true;
//    }

    public void drawGlare(final float rotation) {
        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                float mid = .55f + (-0.2f * rotation);
                int color1 = ColorUtils.setAlphaComponent(glareColor, 100);
                int color2 = ColorUtils.setAlphaComponent(glareColor, 75);
                LinearGradient lg = new LinearGradient(0, 0 - Math.abs(width - height), width, height + Math.abs(width - height),
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
