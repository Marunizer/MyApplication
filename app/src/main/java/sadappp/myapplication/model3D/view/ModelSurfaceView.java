package sadappp.myapplication.model3D.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.w3c.dom.Attr;

import sadappp.myapplication.model3D.controller.TouchController;

/**
 * This is the actual opengl view. From here we can detect touch gestures for example
 * 
 * @author andresoviedo
 *
 */
public class ModelSurfaceView extends GLSurfaceView {

	private static ModelFragment parent;
	private ModelRenderer mRenderer;
	private TouchController touchHandler;

	public ModelSurfaceView(Context context, AttributeSet attrs) {//ModelActivity parent) {Context context, AttributeSet attrs) {
		super(context,attrs);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// This is the actual renderer of the 3D space
		mRenderer = new ModelRenderer(this);
		setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		// TODO: enable this again
		// setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		touchHandler = new TouchController(this, mRenderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return touchHandler.onTouchEvent(event);
	}

	public ModelFragment getModelActivity() {
		return parent;
	}

	public void setModelActivity(ModelFragment model) {
		this.parent = model;
	}
}