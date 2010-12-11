package com.appspot.mobsurv.controller.dataurl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.util.StringUtil;

import com.appspot.mobsurv.util.ImageDataUrl;

public class IndexController extends Controller {

	/**
	 * リクエストパラメータで指定された画像データを読み込み、 DATA URL 形式の文字列に変換して返します。
	 */
	@Override
	public Navigation run() throws Exception {
		if (!isGet()) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return null;
		}
		
		String url = request.getParameter("url");
		if (StringUtil.isEmpty(url)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		response.setContentType("text/plain");
		String result = downloadImage(url);
		if (StringUtil.isEmpty(result)) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} else {
	        response.setHeader("Access-Control-Allow-Origin", "*");
	        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	        response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
		}
		
		PrintWriter writer = response.getWriter();
		response.setContentLength(result.length());
		writer.write(result);
		writer.flush();
		return null;
	}

	private String downloadImage(String urlString) {
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();

			if (con.getResponseCode() != 200) {
				return "";
			}
			
			StringBuilder encodedString = new StringBuilder();

			String contentType = con.getContentType();
			encodedString.append(ImageDataUrl.prefix(contentType));
			
			is = con.getInputStream();
			byte[] buffer = new byte[3 * 2000];
			while (true) {
				int size = is.read(buffer);
				if (size == -1) {
					break;
				}
				byte[] data = Arrays.copyOfRange(buffer, 0, size);
				String encodedData = ImageDataUrl.encodeBase64(data);
				encodedString.append(encodedData);
			}

			return encodedString.toString();
		} catch (Exception e) {
			return "";
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}
}
