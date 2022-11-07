package io.gitlab.radio_rogal.count_days.aws_lambda;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.gitlab.radio_rogal.count_days.bot.Update;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
public class BotHandlerSlowTest {

  @Mock
  private Context context;
  @InjectMocks
  private BotHandler handler;
  @Mock
  private APIGatewayProxyRequestEvent requestEvent;
  @Mock
  private Update update;
  @Mock
  private UpdateFactory updateFactory;

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

    assertAll("Response", () -> assertEquals("{\"test\":\"pass\"}", responseEvent.getBody()),
        () -> assertEquals("application/json", responseEvent.getHeaders().get("Content-Type")),
        () -> assertEquals(200, responseEvent.getStatusCode()));
  }

}
