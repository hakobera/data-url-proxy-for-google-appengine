package com.appspot.mobsurv.util;

/**
 * 画像データを DATA URL 形式の変換するユーティリティクラスです。
 * DATA URL については以下の URL を参照してください。
 * 
 * <a href="http://tools.ietf.org/html/rfc2397">[RFC 2397]The "data" URL scheme</a>
 */
public abstract class ImageDataUrl {
	
	private static final String PREFIX_TEMPLATE = "data:%s;base64,";
	
	private static final char PAD = '=';
	
	private static final char[] ENCODE_TABLE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '+', '/' 
	};
	
	/**
	 * DATA URL 形式の prefix 文字列を作成して返します。
	 * データはBASE64 エンコード形式を前提としています。
	 * 
	 * @param mimeType 画像の MIME タイプ (image/png, image/jpeg, image/gif, ...etc)
	 * @return DATA URL 形式の prefix 文字列 (data:[mimeType];base64,)
	 */
	public static String prefix(String mimeType) {
		return String.format(PREFIX_TEMPLATE, mimeType);
	}

	
	/**
	 * 画像データを BASE64 形式の文字列に変換して返します。
	 * 
	 * @param data 画像データ
	 * @return 画像データを BASE64 形式に変換した文字列
	 */
	public static String encodeBase64(byte[] data) {
		if (data == null || data.length == 0) {
			return "";
		}
		
		// bit pattern に変換
		StringBuilder bit = new StringBuilder();
		for (int i = 0; i < data.length; ++i) {
			int b = data[i] & 0xff;
			for (int j = 0; j < 8; ++j) {
				int mask = (1 << (7-j));
				int f = b & mask;
				bit.append(f == 0 ? "0" : "1");
			}
		}
		
		// 0 padding
		while (bit.length() % 6 != 0) {
			bit.append("0");
		}
		
		StringBuilder encodedString = new StringBuilder(); 
		// 6bit 単位で処理
		for (int i = 0; i < bit.length(); i+= 6) {
			int index = Integer.parseInt(bit.subSequence(i, i + 6).toString(), 2);
			encodedString.append(ENCODE_TABLE[index]);
		}
		
		// 4 の倍数になるように調整
		while (encodedString.length() % 4 != 0) {
			encodedString.append(PAD);
		}
		
		return encodedString.toString();
	}
	
}
