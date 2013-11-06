package com.example.openglcollada;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.util.Log;

//////////////////////////////////////////////////////////////
//Collada 3d Object
public class Collada3dObject {
	
	// 頂点内容のインデックス をこちらから指定する
    public static final int ATTRIBUTE_POSITION_LOCATION = 0;
    public static final int ATTRIBUTE_NORMAL_LOCATION = 1;
    public static final int ATTRIBUTE_COLOR_LOCATION = 1;
    public static final String ATTRIBUTE_POSITION = "a_pos";
    public static final String ATTRIBUTE_NORMAL = "a_normal";
    public static final String ATTRIBUTE_COLOR = "a_color";
     
	public static final String UNIFORM_MVP_MATRIX = "u_mvpMatrix";	// モデルビュープロジェクション
	public static final String UNIFORM_NORMAL_MATRIX = "u_normalMatrix";	// ノーマルマトリックス
    public static final String UNIFORM_COLOR = "u_color";
    public static final String VARYING_COLOR = "v_color";
     
    // 頂点カラー 付き頂点シェーダーのコンパイル
    private static final String VERTEX_CODE =
            "uniform mat4 u_mvpMatrix;" +
            "uniform mat4 u_normalMatrix;" +
            "uniform vec4 u_color;" + 
            "attribute vec4 a_pos;" +
            "attribute vec4 a_normal;" +
            "varying vec4 v_color;" +
            "void main(){"+
            "   vec3 lightDir = vec3(0.0, 0.0, 1.0);" +
            "   vec3 normal = vec3(normalize(a_normal));" +
            "   normal = normalize(mat3(u_normalMatrix) * normal);" +
            "   gl_Position = u_mvpMatrix * a_pos;" +
            "   float power = dot(normal, lightDir);" +
            "   v_color = vec4(u_color.x * power, u_color.y * power, u_color.z * power, 1.0);" +
            //" v_color = vec4(normal.x, normal.y, normal.z, 1.0);" +
            "}";
 
    // 頂点カラー 付きフラグメントシェーダーのコンパイル
    private static final String FRAGMENT_CODE =
            "precision mediump float;"+
            "varying vec4 v_color;" +
            "void main(){"+
            "    gl_FragColor = v_color;" + 
            "}";
     
    private FloatBuffer mVertexBuffer;	// 頂点バッファ
	private IntBuffer mIndexBuffer;	// インデックスバッファ

	// 描画するためのもの
	private int mVertexShaderID;	// 頂点シェーダーID
	private int mFragmentShaderID;	// フラグメントシェーダーID
	private int mProgramID; // プログラムオブジェクトID

	// シェーダーに値を送るためのハンドル
	private int mLocMVPMatrix;
	private int mLocNormalMatrix;
	private int mLocColor;

	// VBO の管理番号
	private int mVertexBufferID; // 頂点バッファ
	private int mIndexBufferID;	// インデックスバッファ
	
    private float[] mFaceColor = { 0.9f, 0.3f, 0.8f, 1 };
 
 
    public Collada3dObject(float[] vertices, int[] indices) {
 
		// 頂点バッファを設定する
		this.mVertexBuffer = this.makeFloatBuffer(vertices);
		
		// インデックスバッファを作る
		this.mIndexBuffer = this.makeIntBuffer(indices);

		// 頂点配列を有効にする
		GLES20.glEnableVertexAttribArray(ATTRIBUTE_POSITION_LOCATION);
		GLES20.glEnableVertexAttribArray(ATTRIBUTE_NORMAL_LOCATION);
		
		// シェーダーを初期化する
		this.initShader();

		// VBO を初期化する
		this.initVBO();
 
    }
     
	// ////////////////////////////////////////////////////////////
	// 表示する
    public void draw(final GLState glState) {
		// VBO での描画
		// 頂点バッファのセット
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.mVertexBufferID);
		
		// 頂点内容のインデックス で、各値の場所を指定する
		int vertexSize = (3 + 3) * 4;
		GLES20.glVertexAttribPointer(ATTRIBUTE_POSITION_LOCATION, 3, GLES20.GL_FLOAT, false, vertexSize, 0);
		GLES20.glVertexAttribPointer(ATTRIBUTE_NORMAL_LOCATION, 3, GLES20.GL_FLOAT, false, vertexSize, 3 * 4);

		// シェーダーにモデルビュープロジェクション行列を送信
		GLES20.glUniformMatrix4fv(this.mLocMVPMatrix, 1, false, glState.getModelViewProjectionMatrix(), 0);
		
		// シェーダーにモデルビュー行列を送信
		GLES20.glUniformMatrix4fv(this.mLocMVPMatrix, 1, false, glState.getModelViewProjectionMatrix(), 0);
		
		// シェーダーにノーマル行列を送信
		GLES20.glUniformMatrix4fv(this.mLocNormalMatrix, 1, false, glState.getNormalMatrix(), 0);
		
		// シェーダーに色を送信
		GLES20.glUniform4fv(this.mLocColor, 1, this.mFaceColor, 0);
		

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.mIndexBufferID);

		// 面を描く
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, this.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_INT, 0);
		
    }
     
	// ////////////////////////////////////////////////////////////
	// 色をセットする
    public void setColor(float r, float g, float b, float a) {
        this.mFaceColor[0] = r;
        this.mFaceColor[1] = g;
        this.mFaceColor[2] = b;
        this.mFaceColor[3] = a;
    }
     
     
	// /////////////////////////////////////////////////////////////////////////
	// VBOを登録する
    private void initVBO() {
        this.mVertexBufferID = this.makeVBO(this.mVertexBuffer, 4, GLES20.GL_ARRAY_BUFFER);
        this.mIndexBufferID = this.makeVBO(this.mIndexBuffer, 4, GLES20.GL_ELEMENT_ARRAY_BUFFER);
    }
 
	// /////////////////////////////////////////////////////////////////////////
	// 各バッファから VBO へ変換する
    private int makeVBO(Buffer buffer, int size, int target) {
 
        int[] hardwareIDContainer= { -1 };
 
        // ハードウェア側の準備
        GLES20.glGenBuffers(1, hardwareIDContainer, 0);
        GLES20.glBindBuffer(target, 
                hardwareIDContainer[0]);
        GLES20.glBufferData(target,
                buffer.capacity() * size, buffer, GLES20.GL_STATIC_DRAW);
 
        return hardwareIDContainer[0];
    }
 
	// /////////////////////////////////////////////////////////////////////////
	// シェーダーを初期化する
    private void initShader() {
         
		// シェーダーのコンパイル
		this.mVertexShaderID = this.compileShader(
				GLES20.GL_VERTEX_SHADER, VERTEX_CODE);

		this.mFragmentShaderID = this.compileShader(
				GLES20.GL_FRAGMENT_SHADER, FRAGMENT_CODE);

		// プログラムオブジェクトを作る
		this.mProgramID = GLES20.glCreateProgram();
		GLES20.glAttachShader(this.mProgramID, this.mVertexShaderID);
		GLES20.glAttachShader(this.mProgramID, this.mFragmentShaderID);

		// 頂点内容のインデックス をシェーダー変数と関連付ける
		GLES20.glBindAttribLocation(this.mProgramID, ATTRIBUTE_POSITION_LOCATION, ATTRIBUTE_POSITION);
		GLES20.glBindAttribLocation(this.mProgramID, ATTRIBUTE_NORMAL_LOCATION, ATTRIBUTE_NORMAL);

		GLES20.glLinkProgram(this.mProgramID);

		// シェーダーに値を送るためのハンドルを取り出す
		this.mLocMVPMatrix = GLES20.glGetUniformLocation(this.mProgramID, UNIFORM_MVP_MATRIX);
		
		// シェーダーに値を送るためのハンドルを取り出す
		this.mLocNormalMatrix = GLES20.glGetUniformLocation(this.mProgramID, UNIFORM_NORMAL_MATRIX);
		
		// シェーダーに値を送るためのハンドルを取り出す
		this.mLocColor = GLES20.glGetUniformLocation(this.mProgramID, UNIFORM_COLOR);

		// プログラムオブジェクトを使い始める
		GLES20.glUseProgram(this.mProgramID);
    }
     
     
	///////////////////////////////////////////////////////////////////////////
	// FloatBufferを作って値をセットする
    private FloatBuffer makeFloatBuffer(float[] values) {
		// バッファを作る
		FloatBuffer fb = ByteBuffer.allocateDirect(values.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		// 作ったバッファに値をセットしておく
		fb.put(values)
		.position(0);
		return fb;
    }
     
    private IntBuffer makeIntBuffer(int[] values) {
		// バッファを作る
		IntBuffer ib = ByteBuffer.allocateDirect(values.length * 4)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer();
		// 作ったバッファに値をセットしておく
		ib.put(values).position(0);
		return ib;
    }
 
	///////////////////////////////////////////////////////////////////////////
	// シェーダーのソースコードをコンパイルする
    private int compileShader(int type, String code) {
		final int shaderId = GLES20.glCreateShader(type);
		if (shaderId == 0) {
			// シェーダーの領域確保に失敗した
			Log.d("compileShader", "領域確保に失敗");
			return -1;
		}
		// シェーダーをコンパイル
		GLES20.glShaderSource(shaderId, code);
		GLES20.glCompileShader(shaderId);

		// コンパイルが成功したか調べる
		int[] res = new int[1];
		GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, res, 0);
		if (res[0] == 0) {
			// 失敗してる
			Log.d("compileShader", GLES20.glGetShaderInfoLog(shaderId));
			return -1;
		}
		return shaderId;
    }
}
