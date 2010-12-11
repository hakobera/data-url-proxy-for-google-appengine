package com.appspot.mobsurv.controller.dataurl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.util.StringUtil;

import com.appspot.mobsurv.util.ImageDataUrl;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class IndexController extends Controller {

	private Logger logger = Logger.getLogger(IndexController.class.getName());
	
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
		
		logger.info(String.format("Proxy request for %s", url));
		
		response.setContentType("text/plain");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
		response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		String dataUrl = (String) memcache.get(url);
		if (dataUrl == null) {
			if (memcache.contains(url)) {
				dataUrl = (String) memcache.get(url);
				logger.info(String.format("Load from cache %s", url));
			} else {
				dataUrl = downloadImage(url);
				logger.info(String.format("Download %s", url));
				if (StringUtil.isEmpty(dataUrl)) {
					logger.fine(String.format("Download Failed %s", url));
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} else {
					// Memcache の最大サイズ 1MB を超えるものは許可しない
					int contentLength = dataUrl.length();
					if (contentLength > 1024 * 1024) {
						logger.fine(String.format("File size overquota %d %s", contentLength, url));
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					} else {
						logger.info(String.format("Download Successed %s", url));
						memcache.put(url, dataUrl);
					}
				}
			}
		} else {
			logger.info(String.format("Load from cache %s", url));
		}

		PrintWriter writer = response.getWriter();
		response.setContentLength(dataUrl.length());
		writer.write(dataUrl);
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

			int contentLength = con.getContentLength();
			if (contentLength > 1024 * 1024) {
				logger.fine(String.format("File size overquota %d %s", contentLength, url));
				return "";
			}
			
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
