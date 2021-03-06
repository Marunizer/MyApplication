package sadappp.myapplication.model3D.view;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.rendering.ObjectRenderer;
import sadappp.myapplication.model3D.rendering.ObjectRendererFactory;
import sadappp.myapplication.model3D.rendering.Scene;
import sadappp.myapplication.model3D.rendering.XmlLayoutRenderer;
import sadappp.myapplication.model3D.util.CameraPermissionHelper;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mende on 1/24/2018.
 *
 * This fragment doesn't just act as the content of SurfaceView but has the role of a renderer
 */

public class ARModelFragment extends Fragment {

    private static final String TAG = ARModelFragment.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested = false;
    private int objectBuiltFlag = 0;

    private Scene scene;
    private Config defaultConfig;
    private Session session;
    //private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;

    // Tap handling and UI.
    private ArrayBlockingQueue<MotionEvent> queuedTaps = new ArrayBlockingQueue<>(16);
    private String nextObject = "ryan.obj";//"andy.obj";

    private String paramFilename;

    private final View.OnTouchListener tapListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Queue tap if there is space. Tap is lost if queue is full.
                queuedTaps.offer(event);
            }
            return true;
        }
    };

    private final Scene.DrawingCallback drawCallback = new Scene.DrawingCallback() {
        @Override
        public void onDraw(Frame frame) {
            handleTap(frame);
        }

        @Override
        public void trackingPlane() {
            hideLoadingMessage();
        }
    };

    private ObjectRendererFactory objectFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle b = this.getArguments();

        if (b != null) {
            this.paramFilename = b.getString("uri");
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

        //Here I would set up buttons? or set up interphase??? since I already have that

        surfaceView = v.findViewById(R.id.surfaceview);

        String extPath = getContext().getExternalFilesDir(null).getAbsolutePath();
        objectFactory = new ObjectRendererFactory(getContext().getFilesDir().getAbsolutePath()+"/model/", extPath);
        scene = new Scene(getContext(), surfaceView,drawCallback);// session, drawCallback);

        // Set up tap listener.
        surfaceView.setOnTouchListener(tapListener);
        copyAssetsToSdCard();
        //}
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if AR Core is installed
        switch (ArCoreApk.getInstance().requestInstall(getActivity(), !installRequested)) {
            case INSTALL_REQUESTED:
                installRequested = true;
                return;
            case INSTALLED:
                break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (CameraPermissionHelper.hasCameraPermission(getActivity())) {
            showLoadingMessage();
            // Note that order matters - see the note in onPause(), the reverse applies here.
            session = new Session(getContext());
            // Create default config, check is supported, create session from that config.
            defaultConfig = new Config(session);
            if (!session.isSupported(defaultConfig)) {
                Toast.makeText(getContext(), "This device does not support AR", Toast.LENGTH_LONG).show();
                getActivity().finish();
                return;
            }

            session.resume();
            scene.bind(session);
        } else {
            CameraPermissionHelper.requestCameraPermission(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Note that the order matters - GLSurfaceView is paused first so that it does not try
        // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call session.update() and create a SessionPausedException.
        scene.unbind();
        if (session != null) {
            session.pause();
        }
    }


    private void handleTap(Frame frame) {
        // Handle taps. Handling only one tap per frame, as taps are usually low frequency
        // compared to frame rate.
        MotionEvent tap = queuedTaps.poll();
        Camera camera = frame.getCamera();
        if (tap != null
                && tap.getAction() == MotionEvent.ACTION_UP
                && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon.
                Trackable trackable = hit.getTrackable();
                if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {

//                    final ObjectRenderer shadow = objectFactory.create("andy_shadow.obj");
//                    if (shadow != null) {
//                        shadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
//                        scene.addRenderer(
//                                shadow,
//                                trackable,
//                                hit.createAnchor()
//                        );
//                    }

                    final ObjectRenderer object;
                    if (nextObject.length() != 0) {
                        System.out.println("I really hope we atleast build the object");
                        object = objectFactory.create(nextObject);
                    } else {
                        object = new XmlLayoutRenderer(getContext(), R.layout.ar_sample_layout);
                    }

                    if (object != null) {
                        scene.addRenderer(
                                object,
                                trackable,
                                hit.createAnchor()
                        );
                    }

                    // Hits are sorted by depth. Consider only closest hit on a plane.
                    break;
                }
            }
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


    private void showLoadingMessage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingMessageSnackbar = Snackbar.make(
                        getActivity().findViewById(android.R.id.content),
                        "Searching for surfaces...", Snackbar.LENGTH_INDEFINITE);
                loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
                loadingMessageSnackbar.show();
            }
        });
    }

    private void hideLoadingMessage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingMessageSnackbar != null) {
                    loadingMessageSnackbar.dismiss();
                    loadingMessageSnackbar = null;
                }
            }
        });
    }

    //Should not need
    private void copyAssetsToSdCard() {
        final AssetManager assets = getActivity().getAssets();
        final String[] assetArray;
        try {
            assetArray = assets.list("");
        } catch (IOException e) {
            Log.e(TAG, "Could not list assets.", e);
            return;
        }

        final File outputDir = getActivity().getExternalFilesDir(null);
        if (outputDir == null) {
            Log.e(TAG, "Could not find default external directory");
            return;
        }

        for (final String file : assetArray) {
            final String localCopyName = outputDir.getAbsolutePath() + "/" + file;

            // ignore files without an extension (mostly folders)
            if (!localCopyName.contains("")) {
                continue;
            }

            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(localCopyName);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Could not open copy file: '" + localCopyName + "'.");
                outputStream = null;
            }

            if (outputStream != null) {
                try {
                    IOUtils.copy(assets.open(file), outputStream);
                } catch (IOException e) {
                    Log.i(TAG, "Could not open asset file: '" + file + "'.");
                }
            }
        }
    }

    //WE DO GET HERE, Now lets do something ;)
    public void passData(String paramFilename) {

        nextObject = paramFilename;
      //  System.out.println("The anchor that has been hit " + anchors.get(anchors.size()-1));

//        this.objectBuiltFlag = 0;
//        try {
//            mVirtualObject.createOnGlThread(getContext(), paramFilename,paramFileTexture);
//            virtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
        //somehow get the current anchor be replaced by this or at least be made to replace
//            this.objectBuiltFlag = 1;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("We hit this B   " + nextObject);
    }
}
