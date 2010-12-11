package com.appspot.mobsurv.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import static com.appspot.mobsurv.util.ImageDataUrl.*;

import org.junit.Test;

/**
 * 画像データを DATA URL 形式の変換するユーティリティクラスです。
 * DATA URL については以下の URL を参照してください。
 * 
 * <a href="http://tools.ietf.org/html/rfc2397">[RFC 2397]The "data" URL scheme</a>
 */
public class ImageDataUrlTest {
	
	@Test
	public void testEncodeBase64() {
		String data = "ABCDEFG";
		String result = encodeBase64(data.getBytes());
		assertThat(result, is("QUJDREVGRw=="));
	}

	@Test
	public void testPrefix() {
		assertThat(prefix("image/png"), is("data:image/png;base64,"));
		assertThat(prefix("image/gif"), is("data:image/gif;base64,"));
		assertThat(prefix("image/jpeg"), is("data:image/jpeg;base64,"));
	}

}
