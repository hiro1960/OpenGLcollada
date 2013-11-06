package com.example.openglcollada;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

// activity_main.xmlにて、一面GLSurfaceViewExtというエリアが定義してある
public class GLSurfaceViewExt extends GLSurfaceView {

	private GLRenderer mRenderer;
	
	public GLSurfaceViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
 
        this.initGLSurfaceView(context);
    }
 
    public GLSurfaceViewExt(Context context) {
        super(context);
 
        this.initGLSurfaceView(context);
    }
 
    public void initGLSurfaceView(Context contex) {
        this.setEGLContextClientVersion(2);
        this.setEGLConfigChooser(true);
        // レンダラ―の設定
        this.mRenderer = new GLRenderer(contex.getResources());
        this.setRenderer(this.mRenderer);
    }
	
	
}
