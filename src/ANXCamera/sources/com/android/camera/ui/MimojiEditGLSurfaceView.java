package com.android.camera.ui;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaPlayer2;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import com.android.camera.Util;
import com.android.camera.fragment.mimoji.AvatarEngineManager;
import com.android.camera.log.Log;
import com.android.gallery3d.exif.ExifInterface;
import com.arcsoft.avatar.AvatarEngine;
import com.mi.config.b;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import miui.reflect.Field;

public class MimojiEditGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static float[] BACKGROUND_COLOR = {0.0f, 0.0f, 0.0f, 0.0f};
    private static final boolean DEBUG_FPS = false;
    public static final int DO_DRAW = 6;
    public static final int GET_SAVE_THUM_END = 4;
    /* access modifiers changed from: private */
    public static final String TAG = "MimojiEditGLSurfaceView";
    public static final int THUM_HEIGHT = 200;
    public static final int THUM_WIDTH = 200;
    public static final int UPDATE_THUM = 5;
    private AvatarEngine mAvatar;
    private int mDeviceRotation;
    private final MyEGLConfigChooser mEglConfigChooser;
    private int mFrameCount;
    private long mFrameCountingStart;
    private Handler mHandler;
    private boolean mIsStopRender;
    private boolean mNeedRenderBG;
    private boolean mSaveConfigThum;

    private class MyEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {
        private final int[] ATTR_ID;
        private final String[] ATTR_NAME;
        private final int[] mConfigSpec;

        private MyEGLConfigChooser() {
            int[] iArr = new int[13];
            iArr[0] = 12324;
            iArr[1] = b.Bi() ? 8 : 5;
            iArr[2] = 12323;
            iArr[3] = b.Bi() ? 8 : 6;
            iArr[4] = 12322;
            iArr[5] = b.Bi() ? 8 : 5;
            iArr[6] = 12325;
            iArr[7] = 8;
            iArr[8] = 12321;
            iArr[9] = 0;
            iArr[10] = 12352;
            iArr[11] = 4;
            iArr[12] = 12344;
            this.mConfigSpec = iArr;
            this.ATTR_ID = new int[]{12324, 12323, 12322, 12321, 12325, 12326, 12328, 12327};
            this.ATTR_NAME = new String[]{"R", "G", Field.BYTE_SIGNATURE_PRIMITIVE, ExifInterface.GpsStatus.IN_PROGRESS, Field.DOUBLE_SIGNATURE_PRIMITIVE, "S", "ID", "CAVEAT"};
        }

        private EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr) {
            int[] iArr = new int[1];
            int length = eGLConfigArr.length;
            int i = Integer.MAX_VALUE;
            EGLConfig eGLConfig = null;
            for (int i2 = 0; i2 < length; i2++) {
                if (!egl10.eglGetConfigAttrib(eGLDisplay, eGLConfigArr[i2], 12324, iArr) || iArr[0] != 8) {
                    if (!egl10.eglGetConfigAttrib(eGLDisplay, eGLConfigArr[i2], 12326, iArr)) {
                        throw new RuntimeException("eglGetConfigAttrib error: " + egl10.eglGetError());
                    } else if (iArr[0] != 0 && iArr[0] < i) {
                        i = iArr[0];
                        eGLConfig = eGLConfigArr[i2];
                    }
                }
            }
            if (eGLConfig == null) {
                eGLConfig = eGLConfigArr[0];
            }
            egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, 12326, iArr);
            logConfig(egl10, eGLDisplay, eGLConfig);
            return eGLConfig;
        }

        private void logConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig) {
            int[] iArr = new int[1];
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (true) {
                int[] iArr2 = this.ATTR_ID;
                if (i < iArr2.length) {
                    egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, iArr2[i], iArr);
                    sb.append(this.ATTR_NAME[i]);
                    sb.append(iArr[0]);
                    sb.append(" ");
                    i++;
                } else {
                    Log.i(MimojiEditGLSurfaceView.TAG, "Config chosen: " + sb.toString());
                    return;
                }
            }
        }

        public EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay) {
            int[] iArr = new int[1];
            if (!egl10.eglChooseConfig(eGLDisplay, this.mConfigSpec, (EGLConfig[]) null, 0, iArr)) {
                throw new RuntimeException("eglChooseConfig failed");
            } else if (iArr[0] > 0) {
                EGLConfig[] eGLConfigArr = new EGLConfig[iArr[0]];
                if (egl10.eglChooseConfig(eGLDisplay, this.mConfigSpec, eGLConfigArr, eGLConfigArr.length, iArr)) {
                    return chooseConfig(egl10, eGLDisplay, eGLConfigArr);
                }
                throw new RuntimeException();
            } else {
                throw new RuntimeException("No configs match configSpec");
            }
        }
    }

    public MimojiEditGLSurfaceView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MimojiEditGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mEglConfigChooser = new MyEGLConfigChooser();
        this.mFrameCount = 0;
        this.mFrameCountingStart = 0;
        this.mDeviceRotation = 90;
        this.mIsStopRender = false;
        this.mSaveConfigThum = false;
        this.mNeedRenderBG = true;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(this.mEglConfigChooser);
        setRenderer(this);
        setRenderMode(0);
        setPreserveEGLContextOnPause(true);
        getHolder().setFormat(4);
        if (b.Dj()) {
            getHolder().setFixedSize(Util.LIMIT_SURFACE_WIDTH, (Util.sWindowHeight * Util.LIMIT_SURFACE_WIDTH) / Util.sWindowWidth);
        }
        setupAvatar();
    }

    private void outputFps() {
        long nanoTime = System.nanoTime();
        long j = this.mFrameCountingStart;
        if (j == 0) {
            this.mFrameCountingStart = nanoTime;
        } else if (nanoTime - j > 1000000000) {
            Log.d(TAG, "fps: " + ((((double) this.mFrameCount) * 1.0E9d) / ((double) (nanoTime - this.mFrameCountingStart))));
            this.mFrameCountingStart = nanoTime;
            this.mFrameCount = 0;
        }
        this.mFrameCount++;
    }

    public void onDeviceRotationChange(int i) {
        this.mDeviceRotation = i;
    }

    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0823f, 0.0823f, 0.0823f, 1.0f);
        GLES20.glEnable(2929);
        GLES20.glClear(16640);
        if (!this.mIsStopRender) {
            AvatarEngineManager.getInstance().queryAvatar().avatarRender(this.mDeviceRotation, getWidth(), getHeight(), 0, false, (int[]) null);
            if (this.mSaveConfigThum) {
                this.mSaveConfigThum = false;
                byte[] bArr = new byte[160000];
                AvatarEngineManager.getInstance().queryAvatar().renderThumb(244, 292, 21, 20, bArr, 200, 200, MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING, BACKGROUND_COLOR, 1.0f);
                Message obtainMessage = this.mHandler.obtainMessage();
                obtainMessage.what = 4;
                obtainMessage.obj = bArr;
                Handler handler = this.mHandler;
                if (handler != null) {
                    handler.sendMessage(obtainMessage);
                }
            }
        }
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        GLES20.glViewport(0, 0, i, i2);
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void setNeedRenderBG(boolean z) {
        this.mNeedRenderBG = z;
    }

    public void setSaveConfigThum(boolean z) {
        this.mSaveConfigThum = z;
    }

    public void setStopRender(boolean z) {
        this.mIsStopRender = z;
    }

    public void setmHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void setupAvatar() {
        this.mAvatar = AvatarEngineManager.getInstance().queryAvatar();
        this.mAvatar.setRenderScene(false, 0.85f);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
    }
}
