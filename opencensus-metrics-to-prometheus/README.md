# Introduction
This example shows how to use OpenCensus to collect metrics and export via a Prometheus HTTP handler.

## Try it out
1. Run the code: `mvn compile exec:java -Dexec.mainClass=com.example.MetricsToPrometheus`
1. Check the Prometheus export: `curl http://localhost:8888/`
