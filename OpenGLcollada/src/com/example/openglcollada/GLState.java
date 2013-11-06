package com.example.openglcollada;

import android.opengl.Matrix;

//////////////////////////////////////////////////////////////
//描画に必要なモノ
public class GLState {
	
	private float[] mWorldMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelViewProjectionMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];
    private float[] mInvertModelViewMatrix = new float[16];
    private float[] mNormalMatrix = new float[16];
     
 
    // ////////////////////////////////////////////////////////////
    // 初期設定
    void setDefault() {
        Matrix.setIdentityM(this.mWorldMatrix, 0);
        // カメラの位置、見ている地点を設定している
        //this.setEye(0, 0, 3, 0, 0, 0, 0, 1, 0);
        this.setEye(0, 0, 6, 0, 0, 0, 0, 1, 0);
        // カメラの画角(fov)、画面サイズを設定
        this.setProjection(45.0f, 480 / 640.0f, 0.1f, 100.0f);
    }
     
    // ////////////////////////////////////////////////////////////
	// モデルビューの逆行列
    public float[] getInvertModelViewMatrix() {
        Matrix.invertM(this.mInvertModelViewMatrix, 0, this.getModelViewMatrix(), 0);
        return this.mInvertModelViewMatrix;
    }
 
    // ////////////////////////////////////////////////////////////
    // Normal Matrixを返す(ライティング計算するときに必要よ)
    public float[] getNormalMatrix() {
        Matrix.transposeM(
                this.mNormalMatrix, 0,
                this.getInvertModelViewMatrix(), 0);
        return this.mNormalMatrix;
    }
     
    // ////////////////////////////////////////////////////////////
    // ワールドへ移動する行列をセット
    void setWorldMatrix(float[] m) {
        System.arraycopy(m, 0, this.mWorldMatrix, 0, 16);
        //Matrix.multiplyMM(this.mModelViewMatrix, 0, this.mViewMatrix, 0, this.mWorldMatrix, 0);
    }
     
    // ////////////////////////////////////////////////////////////
    // モデルビュー行列を返す
    public float[] getModelViewMatrix() {
        Matrix.multiplyMM(
                this.mModelViewMatrix, 0,
                this.mViewMatrix, 0,
                this.mWorldMatrix, 0);
        return this.mModelViewMatrix;
    }
     
     
    // ////////////////////////////////////////////////////////////
    // モデルビュープロジェクションマトリックス
    float[] getModelViewProjectionMatrix() {
         
        Matrix.multiplyMM(
                this.mModelViewProjectionMatrix, 0,
                this.mProjectionMatrix, 0,
                this.getModelViewMatrix(), 0);
                 
        return this.mModelViewProjectionMatrix;
    }
     
    ////////////////////////////////////////////////////////////
    // 透視投影変換
    public void setProjection(float fov, float aspect, float near, float far) {
         
    	// 各要素の値を作る
        float top = near * (float)Math.tan(Math.toRadians(fov));
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
         
        // 今の場所のものに反映させる
        Matrix.frustumM(this.mProjectionMatrix, 0, 
                left, right, bottom, top, near, far);
    }
     
    ////////////////////////////////////////////////////////////
    // 視点変換
    public void setEye(
            final float eyeX, final float eyeY, final float eyeZ,
            final float centerX, final float centerY, final float centerZ,
            final float upX, final float upY, final float upZ) {
 
        Matrix.setLookAtM(this.mViewMatrix, 0,
                eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                upX, upY, upZ);
    }

}
