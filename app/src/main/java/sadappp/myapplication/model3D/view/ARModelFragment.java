package sadappp.myapplication.model3D.view;
import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.rendering.BackgroundRenderer;
import sadappp.myapplication.model3D.rendering.ObjectRenderer;
import sadappp.myapplication.model3D.rendering.PlaneRenderer;
import sadappp.myapplication.model3D.rendering.PointCloudRenderer;
import sadappp.myapplication.model3D.util.CameraPermissionHelper;
import sadappp.myapplication.model3D.util.DisplayRotationHelper;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mende on 1/24/2018.
 */

public class ARModelFragment extends Fragment implements GLSurfaceView.Renderer{

    private static final String TAG = ARModelFragment.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;
    private int objectBuiltFlag = 0;

    private Session session;
    private GestureDetector gestureDetector;
    private Snackbar messageSnackbar;
    private DisplayRotationHelper displayRotationHelper;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];

    // Tap handling and UI.
    private final ArrayBlockingQueue<MotionEvent> queuedSingleTaps = new ArrayBlockingQueue<>(16);
    private final ArrayList<Anchor> anchors = new ArrayList<>();

    private String paramFilename;
    private String paramFileTexture;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle b = this.getArguments();

        if (b != null) {
            this.paramFilename = b.getString("uri");
            this.paramFileTexture = paramFilename.replace(".obj",".jpg");
        }

        View v= inflater.inflate(R.layout.fragment_model_ar, container, false);

        init(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void init(View v) {

        surfaceView = v.findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ getContext());

        // Set up tap listener.
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onSingleTap(e);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        surfaceView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        installRequested = false;

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {//TODO: ReEnable, also first time app is run, access is denied...
//                switch (ArCoreApk.getInstance().requestInstall(getActivity(), !installRequested)) {
//                    case INSTALL_REQUESTED:
//                        installRequested = true;
//                        return;
//                    case INSTALLED:
//                        break;
//                }
                session = new Session(/* context= */ getContext());
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (Exception e) {
                message = "This device does not support AR";
                exception = e;
            }

            if (message != null) {
                showSnackbarMessage(message, true);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }

            // Create default config and check if supported.
            Config config = new Config(session);
            if (!session.isSupported(config)) {
                showSnackbarMessage("This device does not support AR", true);
            }
            session.configure(config);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
                    CameraPermissionHelper.requestCameraPermission(getActivity());
                    return;
                }
                else //if (objectBuiltFlag == 1)//This makes or breaks
                {
                    if (session != null)
                    {
                        showLoadingMessage();
                        // Note that order matters - see the note in onPause(), the reverse applies here.
                        session.resume();
                        surfaceView.onResume();
                        displayRotationHelper.onResume();
                    }
//                    surfaceView.onResume();
//                    displayRotationHelper.onResume();
                }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            Toast.makeText(getActivity(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(getActivity());
            }
            getActivity().finish();
        }
    }

    private void onSingleTap(MotionEvent e) {
        // Queue tap if there is space. Tap is lost if queue is full.
        queuedSingleTaps.offer(e);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Create the texture and pass it to ARCore session to be filled during update().
        backgroundRenderer.createOnGlThread(/*context=*/ getContext());

        // Prepare the other rendering objects.
        try {
            virtualObject.createOnGlThread(getContext(), paramFilename,paramFileTexture);
            // virtualObject.createOnGlThread(getContext(), "andy.obj","andy.jpg");
            this.objectBuiltFlag = 1;
            virtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

//            virtualObjectShadow.createOnGlThread(/*context=*/ getContext(), "andy_shadow.obj", "andy_shadow.png");
//            virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
//            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
        try {
            planeRenderer.createOnGlThread(/*context=*/ getContext(), "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
        pointCloud.createOnGlThread(/*context=*/ getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            MotionEvent tap = queuedSingleTaps.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    // Creates an anchor if a plane or an oriented point was hit.
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof Point
                            && ((Point) trackable).getOrientationMode()
                            == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        //MARU - I changed 20 to 1 , lets see what happens. to limit the models
                        if (anchors.size() >= 1) {
                            anchors.get(0).detach();
                            anchors.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor is created on the Plane to place the 3D model
                        // in the correct position relative both to the world and to the plane.
                        anchors.add(hit.createAnchor());
                        break;
                    }
                }
            }

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            this.pointCloud.update(pointCloud);
            this.pointCloud.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();

            // Check if we detected at least one plane. If so, hide the loading message.
            if (messageSnackbar != null) {
                for (Plane plane : session.getAllTrackables(Plane.class)) {
                    if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {
                        hideLoadingMessage();
                        break;
                    }
                }
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            // Visualize anchors created by touch.
            float scaleFactor = 0.1f; //1.0f;
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
              //  virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, lightIntensity);
              //  virtualObjectShadow.draw(viewmtx, projmtx, lightIntensity);
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
        messageSnackbar =
                Snackbar.make(
                        getActivity().findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_INDEFINITE);
        messageSnackbar.getView().setBackgroundColor(0xbf323232);
        if (finishOnDismiss) {
            messageSnackbar.setAction(
                    "Dismiss",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            messageSnackbar.dismiss();
                        }
                    });
            messageSnackbar.addCallback(
                    new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            getActivity().finish();
                        }
                    });
        }
        messageSnackbar.show();
    }

    private void showLoadingMessage() {
        getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        showSnackbarMessage("Searching for surfaces...", false);
                    }
                });
    }

    private void hideLoadingMessage() {
        getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (messageSnackbar != null) {
                            messageSnackbar.dismiss();
                        }
                        messageSnackbar = null;
                    }
                });
    }

        public void passData(String paramFilename) {
//        this.objectBuiltFlag = 0;
//        this.paramFilename = paramFilename;
//        this.paramFileTexture = paramFilename.replace(".obj",".jpg");
//        try {
//            mVirtualObject.createOnGlThread(getContext(), paramFilename,paramFileTexture);
//            this.objectBuiltFlag = 1;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("We hit this B");
    }
}
