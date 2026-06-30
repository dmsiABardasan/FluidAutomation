package auto.utils.coverage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.json.Jsoner;
import fluid.test.message.AbstractMessage;

/**
 * Captures Fluid AppServer call payloads as HAR 1.2 files for fluid-ui-test-kit
 * playback.  Scoped — by design — to <code>loadActivityDef</code> and
 * <code>loadData</code> only, which together are enough to render an activity's
 * initial screen with its initial data in the GUI test-kit; that's the visual
 * comparison surface we need for the 12.1p → 14.1p upgrade.
 *
 * <p><strong>Write-once, file-existence dedup:</strong> every call writes a
 * single-entry HAR file named <code>{service}__{activity}__{action}.har</code>.
 * If the file already exists (from this run or any prior run), the call is
 * skipped.  Properties this gives us for free:
 * <ul>
 *   <li>Zero in-RAM accumulation — only the in-flight entry exists, then it
 *       hits disk and is forgotten.  Survives suites with tens of thousands
 *       of API calls without GC pressure.</li>
 *   <li>Cross-run dedup — yesterday's HARs on disk → today's run skips matching
 *       calls.  Suite gets faster over time, not slower; first time an activity
 *       is touched anywhere in the suite, it's captured.</li>
 *   <li>JVM-crash safe — each file is self-contained, written immediately.</li>
 * </ul>
 *
 * <p><strong>Scope tradeoff:</strong> two <code>loadData</code> calls on the
 * same activity with different request bodies (e.g. filter variants) collapse
 * to the first one captured.  Acceptable for the visual-comparison goal — the
 * first variant is enough to render the screen.  If body-specific capture
 * becomes a requirement, extend the unique key with a request-body hash.
 *
 * <p><strong>Why synthetic localhost URLs:</strong> the recorder runs inside
 * the Java API suite which doesn't know which web frontend will replay the
 * data.  Hard-coding the deployment origin would lock each HAR to a single
 * environment.  Instead we emit <code>http://localhost/web/fluid/...</code>
 * and let the test-kit's origin+env rewriter map it to the playback target.
 * Localhost is RFC 6761 reserved; a stray request that escapes the rewrite
 * fails at 127.0.0.1 with connection-refused rather than leaking to public
 * DNS.
 */
public class HarRecorder {

	public enum ApiKind {
		STATIC("static"),
		PRESENTER("presenter");

		final String pathSegment;
		ApiKind(String pathSegment) { this.pathSegment = pathSegment; }
	}

	/** Synthetic origin written into HAR URLs; test-kit rewrites it at playback. */
	public static final String SYNTHETIC_ORIGIN = "http://localhost";

	private static final String HAR_VERSION = "1.2";
	private static final String CREATOR_NAME = "FluidAutomation:HarRecorder";
	private static final String CREATOR_VERSION = "1.0";

	/** ISO 8601 with millisecond precision and explicit zone offset. Required by HAR spec. */
	private static final ThreadLocal<SimpleDateFormat> ISO_8601 = ThreadLocal.withInitial(() -> {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf;
	});

	private final AtomicLong written = new AtomicLong();
	private final AtomicLong skipped = new AtomicLong();
	private final File outDir;

	public HarRecorder(File outDir) {
		this.outDir = outDir;
	}

	/**
	 * Capture one Fluid AppServer call as a single-entry HAR file, or skip if
	 * an HAR for the same {@code (service, activity, action)} already exists
	 * on disk.
	 *
	 * @param activityService full activity classname (e.g. "Agility.Activity.Samples.SampleUIExamplesViewPR")
	 * @param activityName    short activity tag (model.getActivity()); may be null/empty for early loadActivityDef calls
	 * @param api             STATIC or PRESENTER — sets the URL's third path segment
	 * @param action          loadActivityDef / loadData / presenter ipFunction
	 * @param request         the Fluid message object (serialised via AbstractMessage.serializeMessage if available)
	 * @param response        the Fluid response object (same)
	 * @param elapsedNanos    System.nanoTime() difference around the underlying call
	 */
	public void record(
		String activityService,
		String activityName,
		ApiKind api,
		String action,
		Object request,
		Object response,
		long elapsedNanos
	) {
		String filename = sanitizeFilename(bucketKey(activityService, activityName, action)) + ".har";
		File outFile = new File(outDir, filename);
		if (outFile.exists()) {
			skipped.incrementAndGet();
			return;
		}
		if (!outDir.exists() && !outDir.mkdirs()) {
			System.err.println("[HarRecorder] could not create output dir: " + outDir);
			return;
		}

		// createNewFile is atomic — if another concurrent caller wins, we skip.
		// In practice the API suite is single-threaded per JVM, but this is
		// cheap insurance and prevents two captures racing on the same path.
		try {
			if (!outFile.createNewFile()) {
				skipped.incrementAndGet();
				return;
			}
		} catch (IOException ex) {
			System.err.println("[HarRecorder] could not create file " + outFile + ": " + ex.getMessage());
			return;
		}

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

		JsonObject har = new JsonObject();
		JsonObject log = new JsonObject();
		log.put("version", HAR_VERSION);
		JsonObject creator = new JsonObject();
		creator.put("name", CREATOR_NAME);
		creator.put("version", CREATOR_VERSION);
		log.put("creator", creator);
		JsonArray entries = new JsonArray();
		entries.add(entry);
		log.put("entries", entries);
		har.put("log", log);

		try (Writer w = new FileWriter(outFile)) {
			w.write(Jsoner.prettyPrint(Jsoner.serialize(har)));
			written.incrementAndGet();
		} catch (IOException ex) {
			System.err.println("[HarRecorder] failed to write " + outFile + ": " + ex.getMessage());
			// Best-effort cleanup so a half-written or 0-byte file doesn't
			// poison future skip-checks.
			if (!outFile.delete()) {
				System.err.println("[HarRecorder] also failed to clean up partial file " + outFile);
			}
		}
	}

	public long writtenCount() {
		return written.get();
	}

	public long skippedCount() {
		return skipped.get();
	}

	// ---- internals ----

	private static String serialize(Object o) {
		if (o == null) return "";
		try {
			if (o instanceof AbstractMessage) {
				String body = ((AbstractMessage) o).serializeMessage();
				return body == null ? "" : body;
			}
			return String.valueOf(o);
		} catch (Throwable ex) {
			System.err.println("[HarRecorder] serialise failed for "
				+ o.getClass().getName() + ": " + ex.getMessage());
			return "{\"_harRecorderSerializeError\":\"" + ex.getClass().getSimpleName() + "\"}";
		}
	}

	private static JsonObject buildRequest(ApiKind api, String service, String action, String body) {
		JsonObject r = new JsonObject();
		r.put("method", "POST");
		r.put("url", SYNTHETIC_ORIGIN + "/web/fluid/" + api.pathSegment + "/" + service + "/" + action);
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

	/**
	 * Minimal HAR timings.  No granular send/wait/receive split available at
	 * the AppServer adapter layer — call is an opaque dispatch from this
	 * vantage point — so the entire elapsed duration is attributed to "wait".
	 * HAR spec requires send + wait + receive be present and sum-or-exceed
	 * entry.time.
	 */
	private static JsonObject buildTimings(long elapsedMs) {
		JsonObject t = new JsonObject();
		t.put("send", 0);
		t.put("wait", elapsedMs);
		t.put("receive", 0);
		return t;
	}

	private static String bucketKey(String service, String activity, String action) {
		String s = service == null ? "_unknown_service_" : service;
		String act = action == null || action.isEmpty() ? "_unknown_action_" : action;
		if (activity == null || activity.isEmpty()) {
			return s + "__" + act;
		}
		return s + "__" + activity + "__" + act;
	}

	private static String sanitizeFilename(String key) {
		return key.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
	}
}
