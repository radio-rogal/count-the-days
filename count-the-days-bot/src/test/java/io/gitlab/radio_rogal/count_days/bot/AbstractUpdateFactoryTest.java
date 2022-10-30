package io.gitlab.radio_rogal.count_days.bot;

import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class AbstractUpdateFactoryTest {

  @Spy
  private AbstractUpdateFactory factory = new TestUpdateFactory();
  private Logger logger;

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @BeforeEach
  void setUp() {
    logger = LoggerFactory.getLogger(TestUpdateFactory.class);
  }

  @DisplayName("Wrong update")
  @ParameterizedTest(name = "[{index}] body <{0}>")
  @EmptySource
  @ValueSource(strings = {" ", "{\"json\":\"bad\""})
  void wrongUpdate(String body) {
    // when
    factory.parseUpdate(body);

    // then
    verify(logger).warn(startsWith("Wrong update:"), anyString(), eq(body));
  }

  @DisplayName("To handle an unknown update")
  @Test
  void handleUnknownRequest() {
    // given
    when(logger.isTraceEnabled()).thenReturn(false);

    // when
    factory.parseUpdate("{\"test\":\"passed\"}");

    // then
    verify(logger).isTraceEnabled();
    verify(logger).info(eq("Unprocessed update: {}"), eq(singleton("test")));
  }

  @DisplayName("To handle and trace an unknown update")
  @Test
  void handleAndTraceUnknownRequest() {
    // given
    when(logger.isTraceEnabled()).thenReturn(true);

    // when
    factory.parseUpdate("{\"test\":\"passed\"}");

    // then
    verify(logger).isTraceEnabled();
    verify(logger).trace("{\"test\":\"passed\"}");
    verify(logger).info(eq("Unprocessed update: {}"), eq(singleton("test")));
  }

  @DisplayName("To handle a message")
  @Test
  void handleMessage() {
    // when
    factory.parseUpdate("{\"message\":{\"test\":\"passed\"}}");

    // then
    verify(factory).parseMessage(isA(JSONObject.class));
  }

  static class TestUpdateFactory extends AbstractUpdateFactory {

    @Override
    Update parseMessage(JSONObject message) {
      return null;
    }

  }

}
