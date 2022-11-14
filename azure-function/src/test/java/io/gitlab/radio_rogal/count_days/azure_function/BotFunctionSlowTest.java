package io.gitlab.radio_rogal.count_days.azure_function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import io.gitlab.radio_rogal.count_days.bot.Update;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
public class BotFunctionSlowTest {

  @Mock
  private ExecutionContext context;
  @InjectMocks
  private BotFunction function;
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

  @DisplayName("Happy path")
  @Test
  void happyPath() throws Exception {
    // given
    when(context.getInvocationId()).thenReturn("test-id");
    when(requestMessage.getBody()).thenReturn(Optional.of("test body"));
    when(requestMessage.createResponseBuilder(isA(HttpStatus.class))).thenReturn(
        responseMessageBuilder);
    when(responseMessageBuilder.build()).thenReturn(responseMessage);
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
