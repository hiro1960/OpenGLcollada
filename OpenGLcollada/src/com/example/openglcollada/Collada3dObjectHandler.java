package com.example.openglcollada;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

//////////////////////////////////////////////////////////////
//COLLADAをロードするためのもの
//Blender 2.63aで、すべてのフェイスを三角形に分割してCOLLADAエクスポーターで
//エクスポートされたdaeファイルだけよみこめる。
public class Collada3dObjectHandler extends DefaultHandler {
	
	private float[] mVertexArray;	// まとめられた頂点情報
	private int[] mIndexArray;	// 頂点配列
	
	private float[]	mPositionArray;	// 座標
	private float[] mNormalArray;	// 法線
	private int[]	mIndices;	// 頂点のインデックス(座標、法線、etc)
	
	// 今、どのタグ内に入っているか
	private boolean	mInPosition;	// 頂点座標検索中
	private boolean mInNormal;	// 法線検索中
	private boolean mInPolylist;	// インデックスリスト
	private boolean mInP;
	
	// XMLの内容が分割されてくることがあるので、この中にためて後で処理する
	// startElement → characters → characters → endElement
	// エレメント内の内容が大きすぎる（文字数が多すぎる）場合はcharactersが複数回やってくる
	// それをこの中にまとめておいてendElementで全部処理する
	private StringBuilder mBuffer = new StringBuilder();
	
	public void startDocument() throws SAXException {
		super.startDocument();
	}
	
	// ////////////////////////////////////////////////////////////
	// タグの開始時によびだされる
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		
		mBuffer.setLength(0);
		
		// ////////////////////////////////////////////////////////////
		// 
		if (localName.equalsIgnoreCase("float_array") && attributes.getValue("id").contains("position")) {
			this.mInPosition = true;
		} else if (localName.equalsIgnoreCase("float_array") && attributes.getValue("id").contains("normal")) {
			this.mInNormal = true;
		} else if (localName.equalsIgnoreCase("polylist") && this.mPositionArray != null) {
			this.mInPolylist = true;
		} else if (localName.equalsIgnoreCase("p") && this.mInPolylist) {
			this.mInP = true;
		}
	}
	
	// ////////////////////////////////////////////////////////////
	// タグが終了したときに呼び出される
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
		
		// 頂点座標
		if (this.mInPosition) {
			
			String[] tmp = this.getCharacter().split("\\s");
			
			// メモリを確保
			float[] positionArray = new float[tmp.length];
			this.mPositionArray = positionArray;

			// 頂点データを文字列から数値へ変換
			for (int i = 0, len = this.mPositionArray.length; i < len; i++) {
				positionArray[i] = Float.parseFloat(tmp[i]);
			}
			
			this.mInPosition = false;
		
		// 法線情報
		} else if (this.mInNormal) {
			
			// 頂点を求める
			String[] tmp = this.getCharacter().split("\\s");
			
			// メモリを確保
			float[] normalArray = new float[tmp.length];
			this.mNormalArray = normalArray;
			
			// 頂点データを文字列から数値へ変換
			for (int i = 0, len = normalArray.length; i < len; i++) {
				normalArray[i] = Float.parseFloat(tmp[i]);
			}
			
			this.mInNormal = false;
		// インデックス
		} else if (this.mInP) {
			// 頂点数を求める
			String[] tmp = this.getCharacter().split("\\s");
			
			// メモリ確保
			int[] indices = new int[tmp.length];
			this.mIndices = indices;

			// すべて含めたインデックス情報
			for (int i = 0, len = indices.length; i < len; i++) {
				indices[i] = Integer.parseInt(tmp[i]);
			}
			
			this.mInPolylist = false;
			this.mInP = false;
		}

	}
	
	// ////////////////////////////////////////////////////////////
	// バッファの内容を取り出す
	private String getCharacter() {
		return this.mBuffer.toString().trim();
	}
	
	// ////////////////////////////////////////////////////////////・
	// タグごとに1回から複数回呼び出される
	// タグの内容が長すぎた場合(頂点数が1000超えてるとか)
	// "-0.1379497 -0.1587564　-0.5478345　-0.1247865　-0.3548731"という文字列が
	// 1回目"-0.1379497 -0.1587564　-0.5478345　-0.1"
	// 2回目"247865　-0.3548731"
	// のように分割された送られてくる。
	// なのでStringBuilderにためてendElementが呼ばれた段階で内容を解析する
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		
		// この中にデータをためていく
		this.mBuffer.append(ch, start, length);
		
	}
	
	// ////////////////////////////////////////////////////////////
	// 解析された頂点データをOpenGLで使える様にまとめる
	private void packVertex() {
		
		// ////////////////////////////////////////////////////////////
		// 法線を反映させた情報
		int capacity = this.mIndices.length / 2;
		float[] vertexArray = new float[capacity * (3 + 3)];
		int[] indexArray = new int[capacity];
		int cursor = 0;
		
		float[] positionArray = this.mPositionArray;
		float[] normalArray = this.mNormalArray;
		
		for (int i = 0; i < capacity; i++) {
			
			int basePos = this.mIndices[i * 2 + 0] * 3;
			vertexArray[cursor++] = positionArray[basePos + 0];
			vertexArray[cursor++] = positionArray[basePos + 1];
			vertexArray[cursor++] = positionArray[basePos + 2];
			
			basePos = this.mIndices[i * 2 + 1] * 3;
			vertexArray[cursor++] = normalArray[basePos + 0];
			vertexArray[cursor++] = normalArray[basePos + 1];
			vertexArray[cursor++] = normalArray[basePos + 2];
			
			indexArray[i] = i;
		}
		
		this.mVertexArray = vertexArray;
		this.mIndexArray = indexArray;
	}
	
	
	// ////////////////////////////////////////////////////////////
	// ファイルからオブジェクト情報を取り出す
	public ArrayList<Collada3dObject> parseFile(InputStream input) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(input));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 頂点情報をまとめる
		this.packVertex();
		
		
		ArrayList<Collada3dObject> res = new ArrayList<Collada3dObject>();
		res.add(new Collada3dObject(this.mVertexArray, this.mIndexArray));
		
		return res;
	}

}
