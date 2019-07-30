/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.schoolofai.objectclassificationgame;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.schoolofai.objectclassificationgame.env.ImageUtils;
import com.schoolofai.objectclassificationgame.env.Logger;
import com.schoolofai.objectclassificationgame.tflite.Classifier;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Device;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Items;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Model;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Recognition;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class CameraActivity extends AppCompatActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private static final Logger LOGGER = new Logger();

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private LinearLayout bottomSheetLayout;
    private LinearLayout gestureLayout;

    private BottomSheetBehavior sheetBehavior;

    protected TextView recognitionTextView,
            recognition1TextView,
            recognition2TextView,
            recognitionValueTextView,
            recognition1ValueTextView,
            recognition2ValueTextView;
    protected TextView completedTv;
    protected TextView item1, itemStatus1,
            item2, itemStatus2,
            item3, itemStatus3,
            item4, itemStatus4,
            item5, itemStatus5,
            item6, itemStatus6,
            item7, itemStatus7,
            item8, itemStatus8,
            item9, itemStatus9,
            item10, itemStatus10,
            itemNow, itemNowStatus;
    protected ImageView bottomSheetArrowImageView;
    private String[] allitems = {"mouse",
            "tench",
            "goldfish",
            "great white shark",
            "tiger shark",
            "hammerhead",
            "electric ray",
            "computer keyboard",
            "cock",
            "hen"};
    private ArrayList<String> itemsList;
    private ArrayList<Items> items;

    private Model model = Model.QUANTIZED;
    private Device device = Device.CPU;
    private int numThreads = -1;
    private int completed = 0;
    private TextToSpeech tts;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }

        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

        itemsList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            String randomString = allitems[random.nextInt(allitems.length)];
            Log.e("Random", randomString);
            while (true) {
                if (!itemsList.contains(randomString)) {
                    itemsList.add(randomString);
                    break;
                } else {
                    randomString = allitems[random.nextInt(allitems.length)];
                    Log.e("Randoming", randomString);
                }
            }
        }
        items = new ArrayList<>();
        for (int i  = 0; i < itemsList.size();  i++){
            Items tmpItem = new Items(itemsList.get(i));
            items.add(tmpItem);
        }


        initView();
         tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });


        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();

                        sheetBehavior.setPeekHeight(height);
                    }
                });
        sheetBehavior.setHideable(false);

        sheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });

        //recognitionTextView = findViewById(R.id.detected_item);
        //recognitionValueTextView = findViewById(R.id.detected_item_value);
        //recognition1TextView = findViewById(R.id.detected_item1);
        //recognition1ValueTextView = findViewById(R.id.detected_item1_value);
        //recognition2TextView = findViewById(R.id.detected_item2);
        //recognition2ValueTextView = findViewById(R.id.detected_item2_value);


        //model = Model.valueOf(modelSpinner.getSelectedItem().toString().toUpperCase());
        //device = Device.valueOf(deviceSpinner.getSelectedItem().toString());
        //numThreads = Integer.parseInt(threadsTextView.getText().toString().trim());
        numThreads = 1;
    }

    private void initView() {
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        item3 = findViewById(R.id.item3);
        item4 = findViewById(R.id.item4);
        item5 = findViewById(R.id.item5);
        item6 = findViewById(R.id.item6);
        item7 = findViewById(R.id.item7);
        item8 = findViewById(R.id.item8);
        item9 = findViewById(R.id.item9);
        item10 = findViewById(R.id.item10);
        itemNow = findViewById(R.id.nowItem);
        itemStatus1 = findViewById(R.id.item1Status);
        itemStatus2 = findViewById(R.id.item2Status);
        itemStatus3 = findViewById(R.id.item3Status);
        itemStatus4 = findViewById(R.id.item4Status);
        itemStatus5 = findViewById(R.id.item5Status);
        itemStatus6 = findViewById(R.id.item6Status);
        itemStatus7 = findViewById(R.id.item7Status);
        itemStatus8 = findViewById(R.id.item8Status);
        itemStatus9 = findViewById(R.id.item9Status);
        itemStatus10 = findViewById(R.id.item10Status);
        itemNowStatus = findViewById(R.id.nowItem1Value);
        completedTv = findViewById(R.id.completeStatusValue);

        item1.setText(itemsList.get(0));
        item2.setText(itemsList.get(1));
        item3.setText(itemsList.get(2));
        item4.setText(itemsList.get(3));
        item5.setText(itemsList.get(4));
        item6.setText(itemsList.get(5));
        item7.setText(itemsList.get(6));
        item8.setText(itemsList.get(7));
        item9.setText(itemsList.get(8));
        item10.setText(itemsList.get(9));
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireNextImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            processImage();
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            Log.e("Pong", Integer.toString(grantResults.length));
            switch (grantResults.length) {
                case 1:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        setFragment();
                    } else {
                        requestPermission();
                    }
                    break;
                case 2:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        setFragment();
                    } else {
                        requestPermission();
                    }
                    break;
                default:
                    requestPermission();

            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API =
                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                                || isHardwareLevelSupported(
                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                LOGGER.i("Camera API lv2?: %s", useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            LOGGER.e(e, "Not allowed to access camera");
        }

        return null;
    }

    protected void setFragment() {
        String cameraId = chooseCamera();

        Fragment fragment;
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize());

            camera2Fragment.setCamera(cameraId);
            fragment = camera2Fragment;
        } else {
            fragment =
                    new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        }

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @SuppressLint("DefaultLocale")
    @UiThread
    protected void showResultsInBottomSheet(List<Recognition> results) {
        if (results != null && results.size() >= 3) {
            Recognition recognition = results.get(0);
            Log.e("Testing Classification:", Integer.toString(results.size()));
            if (recognition != null) {

                if (itemsList.contains(recognition.getTitle())) {
                    itemNow.setText(recognition.getTitle());
                    itemNowStatus.setText(String.format("%.2f", 100 * recognition.getConfidence()) + "%");
                    Log.e("Testing Classification:", recognition.getTitle() + recognition.getConfidence());
                    int location = itemsList.indexOf(recognition.getTitle());
                    if (!items.get(location).isStatus() && recognition.getConfidence() > 0.7f){
                        UpdateCompleteStatus(location);
                        switch (location) {
                            case 0:
                                itemStatus1.setText(R.string.game_completed);
                                break;
                            case 1:
                                itemStatus2.setText(R.string.game_completed);
                                break;
                            case 2:
                                itemStatus3.setText(R.string.game_completed);
                                break;
                            case 3:
                                itemStatus4.setText(R.string.game_completed);
                                break;
                            case 4:
                                itemStatus5.setText(R.string.game_completed);
                                break;
                            case 5:
                                itemStatus6.setText(R.string.game_completed);
                                break;
                            case 6:
                                itemStatus7.setText(R.string.game_completed);
                                break;
                            case 7:
                                itemStatus8.setText(R.string.game_completed);
                                break;
                            case 8:
                                itemStatus9.setText(R.string.game_completed);
                                break;
                            case 9:
                                itemStatus10.setText(R.string.game_completed);
                                break;
                        }
                    }
                } else {
                    itemNow.setText("Unknown");
                    itemNowStatus.setText(String.format("%.2f", 100 * recognition.getConfidence()) + "%");
                }
            }
            //Recognition recognition = results.get(0);
            //if (recognition != null) {
            //    if (recognition.getTitle() != null)
            //        recognitionTextView.setText(recognition.getTitle());
            //    if (recognition.getConfidence() != null)
            //        recognitionValueTextView.setText(
            //                String.format("%.2f", (100 * recognition.getConfidence())) + "%");
            //}

            //Recognition recognition1 = results.get(1);
            //if (recognition1 != null) {
            //    if (recognition1.getTitle() != null)
            //        recognition1TextView.setText(recognition1.getTitle());
            //    if (recognition1.getConfidence() != null)
            //        recognition1ValueTextView.setText(
            //                String.format("%.2f", (100 * recognition1.getConfidence())) + "%");
            //}

            //Recognition recognition2 = results.get(2);
            //if (recognition2 != null) {
            //    if (recognition2.getTitle() != null)
            //        recognition2TextView.setText(recognition2.getTitle());
            //    if (recognition2.getConfidence() != null)
            //        recognition2ValueTextView.setText(
            //                String.format("%.2f", (100 * recognition2.getConfidence())) + "%");
            //}
        }
    }

    private void UpdateCompleteStatus(int location) {
        items.get(location).setStatus(true);
        completed += 1;
        completedTv.setText(completed + " / 10");
        if (completed == 2){
            //tts.setPitch(0.8f);
            tts.setSpeechRate(0.8f);
            tts.speak("Team is completed all tasks", TextToSpeech.QUEUE_FLUSH, null, null);
            stopTimer();
        }

    }

    private void stopTimer() {
        //do something with timer;
    }


    protected Model getModel() {
        return model;
    }

    protected Device getDevice() {
        return device;
    }

    protected int getNumThreads() {
        return numThreads;
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }
}
