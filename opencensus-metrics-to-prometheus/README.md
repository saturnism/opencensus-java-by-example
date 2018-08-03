# Introduction
This example shows how to use OpenCensus to collect metrics and export via a Prometheus HTTP handler.

## Try it out
1. Run the code: `mvn compile exec:java -Dexec.mainClass=com.example.MetricsToPrometheus`
1. Navigate to Prometheus metrics URL: `http://localhost:8888/` to see the exported metrics.
