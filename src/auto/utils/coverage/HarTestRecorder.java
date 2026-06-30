package auto.utils.coverage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.json.Jsoner;
import fluid.test.message.AbstractMessage;

import auto.utils.coverage.HarRecorder.ApiKind;

/**
 * Per-JUnit-test HAR capture.  Sibling to {@link HarRecorder} — different
 * scope, different consumer.
 *
 * <p><strong>How it differs from HarRecorder:</strong>
 * <ul>
 *   <li><strong>Scope</strong>: captures EVERY AppServer call type
 *       (loadActivityDef, loadData, getResults, saveData, presenter,
 *       submitActivityTask) — not just the two HarRecorder is locked to.</li>
 *   <li><strong>Lifecycle</strong>: bound to a single JUnit test via
 *       {@link #start(String)} and {@link #finish()}.  Calls that arrive
 *       while idle are silently ignored.  When the test ends, all captured
 *       entries land in one HAR file and memory is released.</li>
 *   <li><strong>Output</strong>: one file per test under
 *       {@code activityOutput/automation/tests/{testClass}__{testMethod}.har}.
 *       File is overwritten if a test runs again — last-run-wins semantics.</li>
 *   <li><strong>Memory</strong>: only the currently-running test's entries
 *       are in RAM.  Bounded by single-test traffic (typically 5-50 calls,
 *       a few MB max).  Frees after every test.</li>
 * </ul>
 *
 * <p><strong>Use case — bidirectional HAR-as-interchange:</strong>
 * <pre>
 *   GUI scenario  → Playwright recordHar  → HAR file → (replay as API test)
 *   JUnit test    → HarTestRecorder       → HAR file → fluid-ui-test-kit
 *                                                       FLUID_PROXY_OVERRIDES
 * </pre>
 *
 * <p><strong>Wiring</strong>: instantiated by {@link AppServerAdapterRecorder}
 * alongside the per-activity {@code HarRecorder}.  A JUnit 5 extension
 * ({@code HarTestRecordingExtension}) calls start/finish around each test
 * method via the singleton AppServerAdapter.  See that class for activation
 * (system property {@code fluid.recorder.writeTestHar=true}).
 */
public class HarTestRecorder {

	private static final String HAR_VERSION = "1.2";
	private static final String CREATOR_NAME = "FluidAutomation:HarTestRecorder";
	private static final String CREATOR_VERSION = "1.0";

	private static final ThreadLocal<SimpleDateFormat> ISO_8601 = ThreadLocal.withInitial(() -> {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf;
	});

	private final File outDir;
	private final AtomicLong filesWritten = new AtomicLong();
	private final AtomicLong testsSeen = new AtomicLong();

	/** Guarded by `this` — the active-test fields are mutable across
	 *  start/record/finish and need consistent visibility. */
	private String activeTestName = null;
	private List<JsonObject> activeEntries = new ArrayList<>();

	public HarTestRecorder(File outDir) {
		this.outDir = outDir;
	}

	/**
	 * Begin capturing AppServer calls for the named test.  If an earlier test
	 * was still active (a bug in the lifecycle wiring), flush its entries
	 * first so they aren't silently lost.
	 */
	public synchronized void start(String testName) {
		if (activeTestName != null) {
			System.err.println("[HarTestRecorder] start(" + testName + ") called while \""
				+ activeTestName + "\" still active — flushing previous test first.");
			flushAndReset();
		}
		activeTestName = testName;
		activeEntries = new ArrayList<>();
		testsSeen.incrementAndGet();
	}

	/**
	 * End the active test and flush its captured entries to a single HAR file.
	 * No-op if no test is active (e.g., extension disabled, or test failed
	 * before start() ran).
	 */
	public synchronized void finish() {
		if (activeTestName == null) return;
		flushAndReset();
	}

	/**
	 * Capture one AppServer call.  Silently ignored when no test is active —
	 * the extension is the only code path that decides "we're in a test now".
	 */
	public synchronized void record(
		String activityService,
		String activityName,
		ApiKind api,
		String action,
		Object request,
		Object response,
		long elapsedNanos
	) {
		if (activeTestName == null) return;

		String requestBody = serialize(request);
		String responseBody = serialize(response);
		long elapsedMs = Math.max(0L, Math.round(elapsedNanos / 1_000_000.0));
		long now = System.currentTimeMillis();
		String startedDateTime = ISO_8601.get().format(new Date(now - elapsedMs));

		JsonObject entry = new JsonObject();
		entry.put("startedDateTime", startedDateTime);
		entry.put("time", elapsedMs);
		entry.put("request", buildRequest(api, activityService, action, requestBody));
		entry.put("response", buildResponse(responseBody));
		entry.put("cache", new JsonObject());
		entry.put("timings", buildTimings(elapsedMs));

		// Custom non-standard HAR field for traceability — fluid-ui-test-kit
		// ignores unknown fields; useful for humans grepping the HAR.
		entry.put("_fluidActivity", (activityName == null ? "" : activityName)
			+ "::" + (activityService == null ? "" : activityService));

		activeEntries.add(entry);
	}

	public long filesWrittenCount() {
		return filesWritten.get();
	}

	public long testsSeenCount() {
		return testsSeen.get();
	}

	// ---- internals ----

	/** Caller holds the monitor. */
	private void flushAndReset() {
		String testName = activeTestName;
		List<JsonObject> entries = activeEntries;
		activeTestName = null;
		activeEntries = new ArrayList<>();

		if (entries.isEmpty()) return; // test made no recorded AppServer calls — skip empty HAR

		if (!outDir.exists() && !outDir.mkdirs()) {
			System.err.println("[HarTestRecorder] could not create output dir: " + outDir);
			return;
		}

		File outFile = new File(outDir, sanitizeFilename(testName) + ".har");

		JsonObject har = new JsonObject();
		JsonObject log = new JsonObject();
		log.put("version", HAR_VERSION);
		JsonObject creator = new JsonObject();
		creator.put("name", CREATOR_NAME);
		creator.put("version", CREATOR_VERSION);
		log.put("creator", creator);
		JsonArray jsonEntries = new JsonArray();
		jsonEntries.addAll(entries);
		log.put("entries", jsonEntries);
		har.put("log", log);

		try (Writer w = new FileWriter(outFile)) {
			w.write(Jsoner.prettyPrint(Jsoner.serialize(har)));
			filesWritten.incrementAndGet();
		} catch (IOException ex) {
			System.err.println("[HarTestRecorder] failed to write " + outFile + ": " + ex.getMessage());
		}
	}

	private static String serialize(Object o) {
		if (o == null) return "";
		try {
			if (o instanceof AbstractMessage) {
				String body = ((AbstractMessage) o).serializeMessage();
				return body == null ? "" : body;
			}
			return String.valueOf(o);
		} catch (Throwable ex) {
			System.err.println("[HarTestRecorder] serialise failed for "
				+ o.getClass().getName() + ": " + ex.getMessage());
			return "{\"_harRecorderSerializeError\":\"" + ex.getClass().getSimpleName() + "\"}";
		}
	}

	private static JsonObject buildRequest(ApiKind api, String service, String action, String body) {
		JsonObject r = new JsonObject();
		r.put("method", "POST");
		r.put("url", HarRecorder.SYNTHETIC_ORIGIN + "/web/fluid/" + api.pathSegment + "/" + service + "/" + action);
		r.put("httpVersion", "HTTP/1.1");
		r.put("headers", new JsonArray());
		r.put("queryString", new JsonArray());
		r.put("cookies", new JsonArray());
		r.put("headersSize", -1);
		r.put("bodySize", body.length());
		if (!body.isEmpty()) {
			JsonObject postData = new JsonObject();
			postData.put("mimeType", "application/json");
			postData.put("text", body);
			r.put("postData", postData);
		}
		return r;
	}

	private static JsonObject buildResponse(String body) {
		JsonObject r = new JsonObject();
		r.put("status", 200);
		r.put("statusText", "OK");
		r.put("httpVersion", "HTTP/1.1");
		r.put("headers", new JsonArray());
		r.put("cookies", new JsonArray());
		JsonObject content = new JsonObject();
		content.put("size", body.length());
		content.put("mimeType", "application/json");
		content.put("text", body);
		r.put("content", content);
		r.put("redirectURL", "");
		r.put("headersSize", -1);
		r.put("bodySize", body.length());
		return r;
	}

	private static JsonObject buildTimings(long elapsedMs) {
		JsonObject t = new JsonObject();
		t.put("send", 0);
		t.put("wait", elapsedMs);
		t.put("receive", 0);
		return t;
	}

	private static String sanitizeFilename(String key) {
		return key.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
	}
}
