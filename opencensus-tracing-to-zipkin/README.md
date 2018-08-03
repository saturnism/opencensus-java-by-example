# Introduction
This example shows how to use OpenCensus to trace calls within a single application, and exporting the trace to Zipkin.

## Try it out
1. Download Zipkin: `curl -sSL https://zipkin.io/quickstart.sh | bash -s`
1. Start Zipkin: `java -jar zipkin.jar`
1. Run the code: `mvn compile exec:java -Dexec.mainClass=com.example.TracingToZipkin`
1. Navigate to Zipkin Web UI: `http://localhost:9411`
1. Click *Find Traces*, and you should see a trace.
1. Click into that, and you should see the details.
![Trace Spans in Zipkin](opencensus-zipkin.png)
