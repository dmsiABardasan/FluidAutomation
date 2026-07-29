package fluid.test.adapter;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import okhttp3.Interceptor;
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

	private static final String SESSION_COOKIE_NAME = "JSESSIONID";
	/**
	 * Cookie the Fluid web tier publishes the Spring Security CSRF token in
	 * (see FluidCsrfCookieFilter in FluidUI) and the header Spring's CsrfFilter
	 * reads it back from. The token is bound to the JSESSIONID session that
	 * issued it, so tokens are remembered per session.
	 */
	private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
	private static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

	private final Map<String, Cookie> csrfTokensBySession =
			Collections.synchronizedMap(new LinkedHashMap<String, Cookie>(16, 0.75f, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Cookie> eldest) {
					return size() > 64;
				}
			});

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
					.addNetworkInterceptor(csrfTokenInterceptor())
					.connectTimeout(timeout, TimeUnit.SECONDS)
					.readTimeout(timeout, TimeUnit.SECONDS)
					.callTimeout(timeout, TimeUnit.SECONDS)
					.writeTimeout(timeout, TimeUnit.SECONDS)
					.sslSocketFactory(sslContext.getSocketFactory(), trustAnyCertificate).build();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	/**
	 * Mirrors the CSRF handling of the Fluid GWT client (RequestBuilder in
	 * FluidUI): every response's XSRF-TOKEN cookie is remembered against the
	 * session that issued it, and state-changing requests carrying that session
	 * are stamped with the matching X-CSRF-TOKEN header. No-op against servers
	 * that don't have CSRF protection enabled (no cookie ever arrives).
	 *
	 * <p>Registered as a network interceptor so it sees the final Cookie
	 * header, including cookies contributed by the cookie jar.</p>
	 */
	private Interceptor csrfTokenInterceptor() {
		return chain -> {
			Request request = chain.request();
			String sessionId = firstCookieValue(request.headers("Cookie"), SESSION_COOKIE_NAME);
			if (sessionId != null && isCsrfProtectedMethod(request.method())
					&& request.header(CSRF_HEADER_NAME) == null) {
				Cookie token = csrfTokensBySession.get(sessionId);
				if (token != null && token.matches(request.url())) {
					request = request.newBuilder().header(CSRF_HEADER_NAME, token.value()).build();
				}
			}
			okhttp3.Response response = chain.proceed(request);
			rememberCsrfToken(sessionId, response);
			return response;
		};
	}

	private void rememberCsrfToken(String requestSessionId, okhttp3.Response response) {
		Cookie token = null;
		String responseSessionId = null;
		for (String setCookie : response.headers("Set-Cookie")) {
			Cookie cookie = Cookie.parse(response.request().url(), setCookie);
			if (cookie == null) {
				continue;
			}
			if (CSRF_COOKIE_NAME.equalsIgnoreCase(cookie.name())) {
				token = cookie;
			} else if (SESSION_COOKIE_NAME.equals(cookie.name()) && responseSessionId == null) {
				responseSessionId = cookie.value();
			}
		}
		if (token == null) {
			return;
		}
		// A JSESSIONID set in the same response supersedes the session the
		// request went out with (logins rotate the session id).
		String sessionId = responseSessionId != null ? responseSessionId : requestSessionId;
		if (sessionId != null && !sessionId.isEmpty()) {
			csrfTokensBySession.put(sessionId, token);
		}
	}

	private static boolean isCsrfProtectedMethod(String method) {
		return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
				|| "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
	}

	/** First occurrence wins, matching how servlet containers resolve duplicate cookies. */
	private static String firstCookieValue(List<String> cookieHeaders, String cookieName) {
		for (String header : cookieHeaders) {
			for (String pair : header.split(";")) {
				int equals = pair.indexOf('=');
				if (equals > 0 && cookieName.equals(pair.substring(0, equals).trim())) {
					return pair.substring(equals + 1).trim();
				}
			}
		}
		return null;
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
		/**
		 * Newest saved value of the named cookie, or null if absent. Login flows
		 * read the newest because session fixation protection re-issues
		 * JSESSIONID during authentication, leaving the stale id earlier in the jar.
		 */
		public String latestCookieValue(String name) {
			String value = null;
			for (Cookie cookie : getCookies()) {
				if (name.equals(cookie.name())) {
					value = cookie.value();
				}
			}
			return value;
		}
		/**
		 * Collapses duplicates left behind by Set-Cookie replacements (e.g. the
		 * pre-login JSESSIONID after rotation), keeping the newest cookie per
		 * name+domain+path.
		 */
		public void retainNewestCookies() {
			Map<String, Cookie> newest = new LinkedHashMap<>();
			for (Cookie cookie : getCookies()) {
				newest.put(cookie.name() + ";" + cookie.domain() + ";" + cookie.path(), cookie);
			}
			if (newest.size() != getCookies().size()) {
				setCookies(new ArrayList<>(newest.values()));
			}
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
