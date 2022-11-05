package io.gitlab.radio_rogal.count_days.aws_lambda;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.gitlab.radio_rogal.count_days.bot.Update;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class BotHandlerTest {

  @Mock
  private Context context;
  @Mock
  private Logger logger;
  @Mock
  private APIGatewayProxyRequestEvent requestEvent;
  @Mock
  private Update update;
  @Mock
  private UpdateFactory updateFactory;

  @InjectMocks
  private BotHandler handler;

  @DisplayName("A request event with an empty body")
  @ParameterizedTest(name = "[{index}] body <{0}>")
  @NullAndEmptySource
  @ValueSource(strings = " ")
  void emptyRequestEventBody(String body) {
    // given
    when(context.getAwsRequestId()).thenReturn("test-id");
    when(requestEvent.getHeaders()).thenReturn(singletonMap("X-Forwarded-For", "1.2.3.4.5"));
    when(requestEvent.getBody()).thenReturn(body);

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(context).getAwsRequestId();
    verify(logger).warn("Empty request from {}", "1.2.3.4.5");

    assertAll("Response", () -> assertEquals("OK", responseEvent.getBody()),
        () -> assertEquals("text/plain", responseEvent.getHeaders().get("Content-Type")),
        () -> assertEquals(200, responseEvent.getStatusCode()));
  }

  @DisplayName("An update factory returns null: it cannot parse a request body")
  @Test
  void updateFactoryReturnsNull() throws Exception {
    // given
    when(context.getAwsRequestId()).thenReturn("test-id");
    when(requestEvent.getBody()).thenReturn("test body");

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(context).getAwsRequestId();
    verify(updateFactory).parseUpdate("test body");
    verify(update, never()).call();
    verifyNoInteractions(logger);

    assertAll("Response", () -> assertEquals("OK", responseEvent.getBody()),
        () -> assertEquals("text/plain", responseEvent.getHeaders().get("Content-Type")),
        () -> assertEquals(200, responseEvent.getStatusCode()));
  }

  @DisplayName("An update returns null: it cannot process a request")
  @Test
  void updateReturnsNull() throws Exception {
    // given
    when(context.getAwsRequestId()).thenReturn("test-id");
    when(requestEvent.getBody()).thenReturn("test body");
    when(updateFactory.parseUpdate(anyString())).thenReturn(update);

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(context).getAwsRequestId();
    verify(updateFactory).parseUpdate("test body");
    verify(update).call();
    verifyNoInteractions(logger);

    assertAll("Response", () -> assertEquals("OK", responseEvent.getBody()),
        () -> assertEquals("text/plain", responseEvent.getHeaders().get("Content-Type")),
        () -> assertEquals(200, responseEvent.getStatusCode()));
  }

  @DisplayName("Happy path")
  @Test
  void happyPath() throws Exception {
    // given
    when(context.getAwsRequestId()).thenReturn("test-id");
    when(requestEvent.getBody()).thenReturn("test body");
    when(updateFactory.parseUpdate(anyString())).thenReturn(update);
    when(update.call()).thenReturn("{\"test\":\"pass\"}");

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(context).getAwsRequestId();
    verifyNoInteractions(logger);

    assertAll("Response", () -> assertEquals("{\"test\":\"pass\"}", responseEvent.getBody()),
        () -> assertEquals("application/json", responseEvent.getHeaders().get("Content-Type")),
        () -> assertEquals(200, responseEvent.getStatusCode()));
  }

}
