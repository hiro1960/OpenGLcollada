package com.example.openglcollada;

import java.io.InputStream;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class GLRenderer implements GLSurfaceView.Renderer {

	private GLState mGlState = new GLState();
    private Collada3dObjectHandler mHandler;
    private ArrayList<Collada3dObject> m3dObjectArray;
    private Resources mResources;   // リソース
    
    // ////////////////////////////////////////////////////////////
    // コンストラクタ
    public GLRenderer(final Resources resource) {
        this.mResources = resource;
        this.mGlState.setDefault();
    }
 
 
    // ////////////////////////////////////////////////////////////
    // 最初に呼ばれる
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
         
        // COLLADAファイルのロード
        this.mHandler = new Collada3dObjectHandler();
         
        try {
            InputStream is = this.mResources.openRawResource(R.raw.sample);  // res/rawというフォルダにcolladaファイルを置いておく
            this.m3dObjectArray = this.mHandler.parseFile(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
     
    // ////////////////////////////////////////////////////////////
    // サーフェイスのサイズ変更時とかに呼ばれる 
    public void onSurfaceChanged(GL10 gl, int width, int height) {
 
        // ビューポートの再設定
        GLES20.glViewport(0, 0, width, height);
        this.mGlState.setProjection(45.0f, (float)width / (float)height, 0.1f, 100.0f);
    }
 
    float mRotate = 0.0f;
    // ////////////////////////////////////////////////////////////
    // 毎フレーム呼ばれるやつ
    public void onDrawFrame(GL10 gl) {
 
        // 画面をクリア
        GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
 
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        Matrix.rotateM(m, 0, this.mRotate, 0, 1, 0);
        this.mRotate += 0.5f;
        if (360.0f < this.mRotate)
            this.mRotate -= 360.0f;
        this.mGlState.setWorldMatrix(m);
 
        // オブジェクトをロードする
        this.m3dObjectArray.get(0).draw(this.mGlState);
    }
	
	
}
