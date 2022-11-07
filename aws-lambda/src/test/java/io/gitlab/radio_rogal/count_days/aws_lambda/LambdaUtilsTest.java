package io.gitlab.radio_rogal.count_days.aws_lambda;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class LambdaUtilsTest {

  @DisplayName("Returns HTTP 200, application/json")
  @Test
  public void getResponseEvent() {
    // given
    Object object = mock(Object.class);

    when(object.toString()).thenReturn("{ 'test': 'passed' }");

    // when
    APIGatewayProxyResponseEvent responseEvent = LambdaUtils.getResponseEvent(object);

    // then
    assertAll("Return HTTP 200 with correct content type and not empty body",
        () -> assertEquals(200, responseEvent.getStatusCode(), "HTTP status code"),
        () -> assertThat("Content type", responseEvent.getHeaders(),
            hasEntry("Content-Type", "application/json")),
        () -> assertEquals("{ 'test': 'passed' }", responseEvent.getBody(), "Response body"));
  }

  @DisplayName("Returns HTTP 200, just OK")
  @Test
  public void responseOK() {
    // when
    APIGatewayProxyResponseEvent responseEvent = LambdaUtils.responseOK();

    // then
    assertAll("Return HTTP 200 with correct content type and not empty body",
        () -> assertEquals(200, responseEvent.getStatusCode(), "HTTP status code"),
        () -> assertThat("Content type", responseEvent.getHeaders(),
            hasEntry("Content-Type", "text/plain")),
        () -> assertEquals("OK", responseEvent.getBody(), "Response body"));
  }

}
