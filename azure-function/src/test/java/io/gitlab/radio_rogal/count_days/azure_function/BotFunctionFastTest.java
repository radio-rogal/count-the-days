package io.gitlab.radio_rogal.count_days.azure_function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import io.gitlab.radio_rogal.count_days.bot.Update;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class BotFunctionFastTest {

  @Captor
  private ArgumentCaptor<String> bodyCaptor;
  @Mock
  private ExecutionContext context;
  @InjectMocks
  private BotFunction function;
  @Mock
  private Logger logger;
  @Mock
  private HttpRequestMessage<Optional<String>> requestMessage;
  @Mock
  private HttpResponseMessage responseMessage;
  @Mock
  private HttpResponseMessage.Builder responseMessageBuilder;
  @Mock
  private Update update;
  @Mock
  private UpdateFactory updateFactory;

  private Map<String, String> headers;

  @BeforeEach
  void setUp() {
    headers = new HashMap<>();

    when(context.getInvocationId()).thenReturn("test-id");
    when(requestMessage.createResponseBuilder(isA(HttpStatus.class))).thenReturn(
        responseMessageBuilder);
    when(responseMessageBuilder.build()).thenReturn(responseMessage);
  }

  @DisplayName("A request event with an empty body")
  @ParameterizedTest(name = "[{index}] body <{0}>")
  @NullAndEmptySource
  @ValueSource(strings = " ")
  void emptyRequestEventBody(String body) {
    // given
    when(requestMessage.getBody()).thenReturn(Optional.ofNullable(body));
    when(requestMessage.getHeaders()).thenReturn(headers);

    headers.put("X-Forwarded-For", "1.2.3.4.5");

    // when and then
    assertEquals(responseMessage, function.run(requestMessage, context));

    verify(context).getInvocationId();
    verify(requestMessage).createResponseBuilder(HttpStatus.OK);
    verify(responseMessageBuilder).header("Content-Type", "text/plain");
    verify(logger).warn("Empty request from {}", "1.2.3.4.5");
  }

  @DisplayName("An update factory returns null: it cannot parse a request body")
  @Test
  void updateFactoryReturnsNull() throws Exception {
    // given
    when(requestMessage.getBody()).thenReturn(Optional.of("test body"));

    // when and then
    assertEquals(responseMessage, function.run(requestMessage, context));

    verify(context).getInvocationId();
    verify(requestMessage).createResponseBuilder(HttpStatus.OK);
    verify(responseMessageBuilder).header("Content-Type", "text/plain");
    verify(updateFactory).parseUpdate("test body");
    verify(update, never()).call();
    verifyNoInteractions(logger);
  }

  @DisplayName("An update returns null: it cannot process a request")
  @Test
  void updateReturnsNull() throws Exception {
    // given
    when(requestMessage.getBody()).thenReturn(Optional.of("test body"));
    when(updateFactory.parseUpdate(anyString())).thenReturn(update);

    // when and then
    assertEquals(responseMessage, function.run(requestMessage, context));

    verify(context).getInvocationId();
    verify(requestMessage).createResponseBuilder(HttpStatus.OK);
    verify(responseMessageBuilder).header("Content-Type", "text/plain");
    verify(updateFactory).parseUpdate("test body");
    verify(update).call();
    verifyNoInteractions(logger);
  }

  @DisplayName("An update throws an exception")
  @ParameterizedTest
  @CsvFileSource(resources = "/update-throws-an-exception.csv", numLinesToSkip = 1)
  void updateThrowsException(int length, String endsWith, String body) throws Exception {
    when(requestMessage.getBody()).thenReturn(Optional.of(body));
    when(requestMessage.getHeaders()).thenReturn(headers);
    when(updateFactory.parseUpdate(anyString())).thenReturn(update);
    when(update.call()).thenThrow(new Exception("test exception"));

    headers.put("X-Forwarded-For", "1.2.3.4.5");

    // when and then
    assertEquals(responseMessage, function.run(requestMessage, context));

    verify(context).getInvocationId();
    verify(requestMessage).createResponseBuilder(HttpStatus.OK);
    verify(responseMessageBuilder).header("Content-Type", "text/plain");
    verify(updateFactory).parseUpdate(body);
    verify(update).call();
    verify(logger).warn(eq("Update call from {}: {}\n{}"), eq("1.2.3.4.5"), eq("test exception"),
        bodyCaptor.capture());
  }

  @DisplayName("Happy path")
  @Test
  void happyPath() throws Exception {
    // given
    when(requestMessage.getBody()).thenReturn(Optional.of("test body"));
    when(updateFactory.parseUpdate(anyString())).thenReturn(update);
    when(update.call()).thenReturn("{\"test\":\"pass\"}");

    // when and then
    assertEquals(responseMessage, function.run(requestMessage, context));

    verify(context).getInvocationId();
    verify(requestMessage).createResponseBuilder(HttpStatus.OK);
    verify(responseMessageBuilder).header("Content-Type", "application/json");
    verify(responseMessageBuilder).body("{\"test\":\"pass\"}");
  }

}
