package com.example;

import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsConfiguration;
import io.opencensus.stats.*;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tags;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricsToPrometheus {
	// 1. Define a new measure for latency
	private static final Measure.MeasureDouble LATENCY_MS_MEASURE = Measure.MeasureDouble
			.create("latency", "Latency", "ms");

	// 2. Define a new measure to count the number of invocations
	private static final Measure.MeasureLong COUNT_MEASURE = Measure.MeasureLong
			.create("count", "Number of times", "1");

	// 3. Each measure can be associated with additional metadata, known as a Tag.
	// In Prometheus, this will be additional dimensions.

	private static final TagKey METHOD_TAG = TagKey.create("method");

	public static void main(String[] args) throws IOException {
		// 4. Define aggregation strategies
		Aggregation latencyDistribution = Aggregation.Distribution.create(BucketBoundaries.create(
				Arrays.asList(
						// [>=0ms, >=25ms, >=50ms, >=75ms, >=100ms, >=200ms, >=400ms, >=600ms, >=800ms, >=1s, >=2s, >=4s, >=6s]
						0.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 2000.0, 4000.0, 6000.0)
		));
		Aggregation countAggregation = Aggregation.Count.create();

		// 5. Define a View, which essentially binds a measure to an aggregration strategy.
		View latencyView = View
				.create(View.Name.create("example/latency"), "The distribution of latencies",
						LATENCY_MS_MEASURE,
						latencyDistribution,
						Collections.singletonList(METHOD_TAG));
		View countView = View.create(View.Name.create("example/count"), "The number of times doWork was invoked",
				COUNT_MEASURE,
				countAggregation,
				Collections.singletonList(METHOD_TAG));

		// 6. Register the view to the view manager.
		ViewManager viewManager = Stats.getViewManager();
		viewManager.registerView(latencyView);
		viewManager.registerView(countView);

		// 7. Register Prometheus collector.
		// Not only will this register with OpenCensus, it'll also hook into the default Prometheus collector
		// that can be exported via a HTTP handler later.
		PrometheusStatsCollector.createAndRegister();

		// 8. Start a server to expose Prometheus metrics on port 8888
		HTTPServer server = new HTTPServer("localhost", 8888, true);

		while (true) {
			doWork();
		}

	}

	private static void doWork() {
		// 4. Get the start time in milliseconds, since we are only measuring ms.
		final long startTime = System.currentTimeMillis();

		// Simulate some work.
		try {
			Random random = new Random();
			long duration = 200 + random.nextInt(500);
			System.out.println("doing busy work for " + duration + "ms");
			Thread.sleep(duration);
		}
		catch (InterruptedException e) {
		}
		finally {
			// 5. Calculate the total time spent in this method.
			final long endTime = System.currentTimeMillis();
			final long totalTime = endTime - startTime;

			// 6. Get a reference to the Stats Recorder singleton.
			StatsRecorder statsRecorder = Stats.getStatsRecorder();

			// 7. Put the measures in a measure map, keyed by the measures we created earlier.
			MeasureMap measureMap = statsRecorder.newMeasureMap()
					.put(COUNT_MEASURE, 1)
					.put(LATENCY_MS_MEASURE, totalTime);

			// 8. Record the measures by calling .record().
			// But in this case, we also want to add additional tags.
			measureMap.record(Tags.getTagger().emptyBuilder()
					.put(METHOD_TAG, TagValue.create("doWork")).build());
		}

	}
}
