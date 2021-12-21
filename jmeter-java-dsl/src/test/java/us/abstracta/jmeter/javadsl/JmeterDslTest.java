package us.abstracta.jmeter.javadsl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

@WireMockTest
public abstract class JmeterDslTest {

  protected static final int TEST_ITERATIONS = 3;
  protected static final String SAMPLE_1_LABEL = "sample1";
  protected static final String SAMPLE_2_LABEL = "sample2";
  protected static final String OVERALL_STATS_LABEL = "overall";
  protected static final String JSON_BODY = "{\"var\":\"val\"}";

  protected WireMockRuntimeInfo wiremockServer;
  protected String wiremockUri;

  @BeforeEach
  public void setup(WireMockRuntimeInfo wiremock) {
    wiremockServer = wiremock;
    WireMock.stubFor(any(anyUrl()));
    wiremockUri = wiremock.getHttpBaseUrl();
  }

  protected Map<String, Long> buildExpectedTotalCounts() {
    Map<String, Long> expectedStats = new HashMap<>();
    expectedStats.put(OVERALL_STATS_LABEL, (long) 2 * TEST_ITERATIONS);
    expectedStats.put(SAMPLE_1_LABEL, (long) TEST_ITERATIONS);
    expectedStats.put(SAMPLE_2_LABEL, (long) TEST_ITERATIONS);
    return expectedStats;
  }

  protected String getFileContents(Path filePath) throws IOException {
    return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
  }

  protected String getResourceContents(String resourcePath) throws IOException {
    try {
      return new String(Files.readAllBytes(Paths.get(getClass().getResource(resourcePath).toURI())),
          StandardCharsets.UTF_8);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected Map<String, Long> extractCounts(TestPlanStats stats) {
    Map<String, Long> actualStats = new HashMap<>();
    actualStats.put(OVERALL_STATS_LABEL, stats.overall().samplesCount());
    for (String label : stats.labels()) {
      actualStats.put(label, stats.byLabel(label).samplesCount());
    }
    return actualStats;
  }

}
