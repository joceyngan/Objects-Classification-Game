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
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.Message;
import android.os.Trace;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import 	android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.schoolofai.objectclassificationgame.env.ImageUtils;
import com.schoolofai.objectclassificationgame.env.Logger;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;
import com.schoolofai.objectclassificationgame.tflite.Classifier;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Device;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Items;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Model;
import com.schoolofai.objectclassificationgame.tflite.Classifier.Recognition;
import com.schoolofai.objectclassificationgame.tutor.tutorWelcome;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;

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
    private Animation alpha, rotate;
    private MediaPlayer win;

    private BottomSheetBehavior sheetBehavior;
    protected TextView completedTv, itemNow, itemNowStatus;
    protected ImageView item1,
            item2,
            item3,
            item4,
            item5,
            item6,
            item7,
            item8,
            item9,
            item10;
    protected ImageView bottomSheetArrowImageView;
    private String[] allitems = {"Apple",
            "Banana",
            "Carrot",
            "Corn",
            "Grape",
            "GreenGrape",
            "Lemon",
            "Orange",
            "Pear",
            "Tomato"};

    private ArrayList<String> itemsList;
    private ArrayList<Items> items;

    private Model model = Model.FLOAT;
    private Device device = Device.CPU;
    private int numThreads = -1;
    private int completed = 0;
    private int itemLeft = 0;
    private TextToSpeech tts,tts1;
    private Long startTime;
    private TextView time;
    private Timer timer, checker;
    private HashMap<String, String> map;

    private Player player;
    private String finishTime;
    private String roomNumber;
    private String objectText="";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        win = MediaPlayer.create(CameraActivity.this,R.raw.victory_music);
        map = new HashMap<String, String>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        roomNumber = intent.getStringExtra("roomNumber");
        player = intent.getParcelableExtra("player");
        Log.e("Player", player.getPlayerName() + player.getPlayerUid());

        handler = new Handler();
        startTime = System.currentTimeMillis();

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }

        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
        alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        alpha.reset();
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotate.reset();

        itemsList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            //String randomString = allitems[random.nextInt(allitems.length)];   //Random Item
            String randomString = allitems[i];     //Stable 10 Item
            while (true) {
                if (!itemsList.contains(randomString)) {
                    itemsList.add(randomString);
                    break;
                } else {
                    randomString = allitems[random.nextInt(allitems.length)];
                }
            }
        }
        items = new ArrayList<>();
        for (int i = 0; i < itemsList.size(); i++) {
            Items tmpItem = new Items(itemsList.get(i));
            items.add(tmpItem);
        }


        initView();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                           // Log.d("done music","done music");
                            win.start();
                        }
                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                            win.prepareAsync();
                        }
                    });
            }
            }

        });

        tts1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
            }

        });
        tts1.setSpeechRate(0.8f);
        tts1.setLanguage(Locale.ENGLISH);

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
        numThreads = 1;
    }

    private int currentItem = 999;
    private int checkValue[] = new int[3];
    private int checkInt = 0;
    private boolean checkAllSame = false;

    private void setupChecker(){
        checker.schedule(new TimerTask() {
            @Override
            public void run() {
                checkValue[checkInt] = currentItem;
                if (currentItem != 999){
                    while (true){
                        for (int checkValue : checkValue){
                            if (checkValue != currentItem){
                                checkAllSame = false;
                                break;
                            }else{
                                checkAllSame = true;
                            }
                        }
                        break;
                    }
                }else{
                    checkAllSame = false;
                }

                Log.e("Checker", "C Item: " + currentItem + "\t\tFound? " + checkAllSame);
                Log.e("Checker" , "Item 1: " + checkValue[0]);
                Log.e("Checker" , "Item 2: " + checkValue[1]);
                Log.e("Checker" , "Item 3: " + checkValue[2]);
                if (checkAllSame && currentItem != 999){
                    Log.e("Updated", "Item is complete: " + checkValue[checkInt]);
                    checkerHandler.sendEmptyMessage(checkValue[checkInt]);
                }

                checkInt ++;
                if (checkInt > 2){
                    checkInt = 0;
                }
            }
        },1000, 1000);
    }

    private Handler checkerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    objectText = "apple";
                    saidObject();
                    item1.setImageResource(R.drawable.apple3);
                    break;
                case 2:
                    objectText = "banana";
                    saidObject();
                    item2.setImageResource(R.drawable.banana3);
                    break;
                case 3:
                    objectText = "carrot";
                    saidObject();
                    item3.setImageResource(R.drawable.carrot3);
                    break;
                case 4:
                    objectText = "corn";
                    saidObject();
                    item4.setImageResource(R.drawable.corn3);
                    break;
                case 5:
                    objectText = "grape";
                    saidObject();
                    item5.setImageResource(R.drawable.grape3);
                    break;
                case 6:
                    objectText = "green grape";
                    saidObject();
                    item6.setImageResource(R.drawable.greengrape3);
                    break;
                case 7:
                    objectText = "lemon";
                    saidObject();
                    item7.setImageResource(R.drawable.lemon3);
                    break;
                case 8:
                    objectText = "orange";
                    saidObject();
                    item8.setImageResource(R.drawable.orange3);
                    break;
                case 9:
                    objectText = "pear";
                    saidObject();
                    item9.setImageResource(R.drawable.pear3);
                    break;
                case 10:
                    objectText = "tomato";
                    saidObject();
                    item10.setImageResource(R.drawable.tomato3);
                    break;
            }
            UpdateCompleteStatus(msg.what - 1);
            return false;
        }
    });

    private void saidObject(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                tts1.speak(objectText, TextToSpeech.QUEUE_FLUSH,  null , null);
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t=new Thread(r);
        t.start();
    }

    private void setupTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.sendEmptyMessage(0);
            }
        }, 1, 1);
    }

    private Handler timerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Long spentTime = System.currentTimeMillis() - startTime;
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
            Date resultdate = new Date(spentTime);
            finishTime = sdf.format(resultdate);
            time.setText(sdf.format(resultdate));
            return false;
        }
    });

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
        completedTv = findViewById(R.id.completeStatusValue);
        itemNow = findViewById(R.id.nowItem);
        itemNowStatus = findViewById(R.id.nowItem1Value);

        item1.setImageResource(R.drawable.apple1);
        item2.setImageResource(R.drawable.banana1);
        item3.setImageResource(R.drawable.carrot1);
        item4.setImageResource(R.drawable.corn1);
        item5.setImageResource(R.drawable.grape1);
        item6.setImageResource(R.drawable.greengrape1);
        item7.setImageResource(R.drawable.lemon1);
        item8.setImageResource(R.drawable.orange1);
        item9.setImageResource(R.drawable.pear1);
        item10.setImageResource(R.drawable.tomato1);
        time = findViewById(R.id.timerTextView);

        timer = new Timer();
        checker = new Timer();
        setupTimer();
        setupChecker();
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
        Log.e("Pause", this.toString());
        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        Log.e("Stop", this.toString());
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        Log.e("Destory", this.toString());
        timer.cancel();
        checker.cancel();
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
            //Log.e("Pong", Integer.toString(grantResults.length));
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
            //Log.e("Testing Classification:", Integer.toString(results.size()));
            if (recognition != null) {

                if (itemsList.contains(recognition.getTitle())) {
                    itemNow.setText(recognition.getTitle());
                    itemNowStatus.setText(String.format("%.2f", 100 * recognition.getConfidence()) + "%");
                    int location = itemsList.indexOf(recognition.getTitle());
                    if (!items.get(location).isStatus() && recognition.getConfidence() > 0.98f) {
                        switch (location) {
                            case 0:
                                currentItem = 1;
                                break;
                            case 1:
                                currentItem = 2;
                                break;
                            case 2:
                                currentItem = 3;
                                break;
                            case 3:
                                currentItem = 4;
                                break;
                            case 4:
                                currentItem = 5;
                                break;
                            case 5:
                                currentItem = 6;
                                break;
                            case 6:
                                currentItem = 7;
                                break;
                            case 7:
                                currentItem = 8;
                                break;
                            case 8:
                                currentItem = 9;
                                break;
                            case 9:
                                currentItem = 10;
                                break;
                        }
                    }else{
                        currentItem = 999;
                    }
                } else {
                    itemNow.setText("Unknown");
                    itemNowStatus.setText(String.format("%.2f", 100 * recognition.getConfidence()) + "%");
                }
            }
        }
    }

    private void UpdateCompleteStatus(int location) {
        items.get(location).setStatus(true);
        completed += 1;
        player.setCompletedItem(completed);
        if (roomNumber != null){
            documentReference = db.collection("rooms").document(roomNumber);
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot documentSnapshot = transaction.get(documentReference);
                    Room room = documentSnapshot.toObject(Room.class);
                    Log.e("UID", player.getPlayerUid());
                    room.UpdateCompleted(player.getPlayerUid(), completed);
                    if (completed==10){
                        room.UpdateCompleteTime(player.getPlayerUid(), finishTime);
                        room.UpdateStatus(player.getPlayerUid(), 2);
                    }
                    transaction.set(documentReference, room);
                    return null;
                }
            });
        }

        completedTv.setText(completed + " / 10");
        itemLeft = 10 - completed;
        if (completed == 10) {
            //tts.setPitch(0.8f);
            new AlertDialog.Builder(this)
                    .setTitle("Congratulation!")
                    .setMessage("You have completed the game in " + finishTime)
                    .setCancelable(false)
                    .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CameraActivity.super.onBackPressed();
                        }
                    })
                    .show();

            tts.setSpeechRate(0.9f);
            tts.setLanguage(Locale.ENGLISH);
            //map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    tts.speak("Team is completed all tasks", TextToSpeech.QUEUE_FLUSH,  null , null);
                    try {
                        Thread.sleep(3500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    win.start();
                }
            };

            Thread t1=new Thread(r1);
            t1.start();
            timer.cancel();
        }
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

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Warning!")
                .setMessage("Are you sure want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (roomNumber != null){
                            documentReference = db.collection("rooms").document(roomNumber);
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot documentSnapshot = transaction.get(documentReference);
                                    Room room = documentSnapshot.toObject(Room.class);
                                    room.UpdateCompleted(player.getPlayerUid(), completed);
                                    room.UpdateStatus(player.getPlayerUid(), 4);
                                    transaction.set(documentReference, room);
                                    return null;
                                }
                            });
                        }
                        CameraActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
