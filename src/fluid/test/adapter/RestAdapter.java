package fluid.test.adapter;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

public class RestAdapter {
	public enum MediaType{
		JSON ("application/json; charset=utf-8"),
		APPLICATION ("application/x-www-form-urlencoded");
		
		private String value;
		private MediaType(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
		
		private static RequestBody transform(MediaType mediaType, String body) {
			okhttp3.MediaType mt = okhttp3.MediaType.get(mediaType.getValue());
			RequestBody requestBody = RequestBody.create(mt, body);
			return requestBody;
		}
	}

	private static final ThreadLocal<RestAdapter> THREAD_REST_LIVE = new ThreadLocal<RestAdapter>() {
		@Override
		protected RestAdapter initialValue() {
			return new RestAdapter();
		}
	};
	private static final ThreadLocal<RestAdapter> THREAD_REST_CUSTOM = new ThreadLocal<RestAdapter>() {
		@Override
		protected RestAdapter initialValue() {
			return THREAD_REST_LIVE.get();
		}
	};
	
	public static RestAdapter getInstance() {
		return THREAD_REST_CUSTOM.get();
	}
	public static void setInstance(RestAdapter adapter) {
		THREAD_REST_CUSTOM.set(adapter);
	}
	public static RestAdapter getLiveInstance() {
		return THREAD_REST_LIVE.get();
	}
	
	public static boolean isLive() {
		return THREAD_REST_CUSTOM.get() == THREAD_REST_LIVE.get();
	}
	
	private final OkHttpClient client;
	private final CustomCookieJar cookieJar = new CustomCookieJar();
	
	private RestAdapter() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			DummyTrustManager trustAnyCertificate = new DummyTrustManager();
			TrustManager[] trustManagers = new TrustManager[] { trustAnyCertificate };
			sslContext.init(null, trustManagers, null);
			final long timeout = 600;
			this.client = new OkHttpClient.Builder()
					.followRedirects(false)
					.followSslRedirects(false)
					.cookieJar(getCookieJar())
					.connectTimeout(timeout, TimeUnit.SECONDS)
					.readTimeout(timeout, TimeUnit.SECONDS)
					.callTimeout(timeout, TimeUnit.SECONDS)
					.writeTimeout(timeout, TimeUnit.SECONDS)
					.sslSocketFactory(sslContext.getSocketFactory(), trustAnyCertificate).build();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	public CustomCookieJar getCookieJar() {
		return this.cookieJar;
	}
	
	public String post(MediaType mediaType, String url, String body) throws IOException {
		return post(mediaType, url, body, Collections.emptyMap());
	}
	public String post(MediaType mediaType, String url, String body, Map<String, String> headers) throws IOException {
		return post(url, body, headers, mediaType).getResponseBody();
	}
	public Response post(String url, String body, Map<String, String> headers, MediaType mediaType)  throws IOException {
		RequestBody requestBody = MediaType.transform(mediaType, body);
		Builder requestBuilder = new Request.Builder().url(url).post(requestBody);
		addHeaders(requestBuilder, headers);
		return send(requestBuilder.build());
	}
	
	public String get(String url) throws IOException {
		return get(url, Collections.emptyMap());
	}
	public String get(String url, Map<String, String> headers) throws IOException {
		return get(headers, url).getResponseBody();
	}
	public Response get(Map<String, String> headers, String url) throws IOException {
		Builder requestBuilder = new Request.Builder().url(url);
		addHeaders(requestBuilder, headers);
		return send(requestBuilder.build());
	}
	
	public void addHeaders(Builder builder, Map<String, String> headers) {
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				builder.addHeader(header.getKey(), header.getValue());
			}
		}
	}
	
	private Response send(Request request) throws IOException {
		try (okhttp3.Response okResponse = client.newCall(request).execute()) {
			return new Response(okResponse);
		}
	}

	/**
	 * This allows the Automation to ignore certificate errors when sending the
	 * request.
	 */
	private static class DummyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws CertificateException {
		}
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws CertificateException {
		}
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}
	}
	
	public static final class CustomCookieJar implements CookieJar{
		private List<BiFunction<HttpUrl, List<Cookie>, List<Cookie>>> fromCookieCriteria = new ArrayList<>();
		private List<BiFunction<HttpUrl, List<Cookie>, List<Cookie>>> toCookieCriteria = new ArrayList<>();
		private List<Cookie> cookies = new ArrayList<>();
		
		//Some default filter implementations
		public static final BiFunction<HttpUrl, List<Cookie>, List<Cookie>> FILTER_ALLOW_ALL = (url, cookies) -> {return cookies;};
		public static BiFunction<HttpUrl, List<Cookie>, List<Cookie>> filterHttpUrlStartsWith(String startsWith){
			return (httpUrl, preCookies) ->  httpUrl.toString().startsWith(startsWith) ? preCookies : Collections.emptyList();
		}
		
		private CustomCookieJar() {}
		
		public List<Cookie> getCookies(){
			return this.cookies;
		}
		public void setCookies(List<Cookie> cookies){
			this.cookies = cookies;
		}
		public List<BiFunction<HttpUrl, List<Cookie>, List<Cookie>>> saveFromResponseFilters(){
			return fromCookieCriteria;
		}
		public List<BiFunction<HttpUrl, List<Cookie>, List<Cookie>>> loadForRequestFilters(){
			return toCookieCriteria;
		}
		
		/**
		 * If no filter criteria has been provided, no cookies (even if some have been
		 * saved) will be loaded for the request. However, you can use the default
		 * "{@link #FILTER_ALLOW_ALL}" filter to allow for everything.
		 */
		@Override
		public List<Cookie> loadForRequest(HttpUrl httpUrl) {
			List<Cookie> cookiesCopy;
			if (!loadForRequestFilters().isEmpty()) {
				cookiesCopy = new ArrayList<>(getCookies());
				for (BiFunction<HttpUrl, List<Cookie>, List<Cookie>> to : loadForRequestFilters()) {
					cookiesCopy = to.apply(httpUrl, cookiesCopy);
				}
			} else {
				cookiesCopy = Collections.emptyList();
			}
			return cookiesCopy;
		}
		/**
		 * If no filter criteria has been provided, no cookies will be saved from the
		 * response.
		 */
		@Override
		public void saveFromResponse(HttpUrl httpUrl, List<Cookie> incomingCookies) {
			if (!saveFromResponseFilters().isEmpty()) {
				for (BiFunction<HttpUrl, List<Cookie>, List<Cookie>> from : saveFromResponseFilters()) {
					incomingCookies = from.apply(httpUrl, incomingCookies);
				}
			} else {
				incomingCookies = Collections.emptyList();
			}
			getCookies().addAll(incomingCookies);
		}
	}
	
	public static final class MockInstance extends RestAdapter{
		private List<Response> mockResponses = new ArrayList<Response>();
		private int mockResponsesIndex = 0;
		
		public MockInstance() {
			super();
		}
		
		public MockInstance(List<Response> mockResponses) {
			super();
			this.mockResponses = mockResponses;
		}
		
		public List<Response> getMockResponses() {
			return mockResponses;
		}
		public void setMockResponses(List<Response> mockResponseList) {
			mockResponses = mockResponseList;
		}
		
		public int getCurrentMockResponseIndex() {
			return mockResponsesIndex;
		}
		
		public void setCurrentMockResponse(int index) {
			this.mockResponsesIndex = index;
		}
		
		public void reset() {
			setCurrentMockResponse(0);
		}
		
		public Response getNextMockResponse() {
			Response mockResponse = getMockResponses().get(getCurrentMockResponseIndex());
			setCurrentMockResponse(1 + getCurrentMockResponseIndex());
			return mockResponse;
		}
		
		public String send(Request request) {
			String requestUrl = request.url().toString();
			Response mockResponse = getNextMockResponse();
			if (mockResponse.getRequestUrl().equals(requestUrl)) {
				return mockResponse.responseBody;
			}else {
				throw new IllegalArgumentException("Unexpected request passed:\nExpected url: "
						+ mockResponse.getRequestUrl() + "\nActual: " + requestUrl);
			}
		}
	}
	
	public static class Response{
		
		private String requestUrl;
		private String responseBody;
		private int code;
		private Map<String, List<String>> headers;
		
		private long sentRequestAtMillis;
		private long receivedResponseAtMillis;
		
		public Response() {}
		
		public Response(okhttp3.Response response) throws IOException {
			this.requestUrl = response.request().url().toString();
			this.responseBody = response.body().string();
			this.code = response.code();
			this.headers = response.headers().toMultimap();
			this.sentRequestAtMillis = response.sentRequestAtMillis();
			this.receivedResponseAtMillis = response.receivedResponseAtMillis();
		}

		public String getRequestUrl() {
			return requestUrl;
		}
		public String getResponseBody() {
			return responseBody;
		}
		public int getCode() {
			return code;
		}
		public Map<String, List<String>> getHeaders() {
			return headers;
		}
		public long getSentRequestAtMillis() {
			return this.sentRequestAtMillis;
		}
		public long getReceivedResponseAtMillis() {
			return this.receivedResponseAtMillis;
		}
		public Response setRequestUrl(String requestUrl) {
			this.requestUrl = requestUrl;
			return this;
		}
		public Response setResponseBody(String responseBody) {
			this.responseBody = responseBody;
			return this;
		}
		public Response setCode(int code) {
			this.code = code;
			return this;
		}
		public Response setHeaders(Map<String, List<String>> headers) {
			this.headers = headers;
			return this;
		}
		public void setSentRequestAtMillis(long sentRequestAtMillis) {
			this.sentRequestAtMillis = sentRequestAtMillis;
		}
		public void setReceivedResponseAtMillis(long receivedResponseAtMillis) {
			this.receivedResponseAtMillis = receivedResponseAtMillis;
		}
		
	}
}
