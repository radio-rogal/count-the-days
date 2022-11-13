/*
 * Copyright 2022 Witalij Berdinskich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gitlab.radio_rogal.count_days.azure_function;

import static java.util.Objects.nonNull;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import io.gitlab.radio_rogal.count_days.bot.CountDaysUpdateFactory;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import java.util.Optional;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BotFunction {

  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String FORWARDED_FOR = "X-Forwarded-For";
  private static final int MAX_SUBSTRING_LENGTH = 1024;
  private static final String TEXT_PLAIN = "text/plain";

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final UpdateFactory updateFactory;

  public BotFunction() {
    this(new CountDaysUpdateFactory());
  }

  @VisibleForTesting
  BotFunction(UpdateFactory updateFactory) {
    this.updateFactory = updateFactory;
  }

  @FunctionName("CountDaysBot")
  public HttpResponseMessage run(
      @HttpTrigger(name = "request", methods = HttpMethod.POST, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> requestMessage,
      final ExecutionContext context) {
    MDC.put("@aws-request-id@", context.getInvocationId());

    var responseMessageBuilder = requestMessage.createResponseBuilder(HttpStatus.OK);
    var requestMessageBody = requestMessage.getBody();

    responseMessageBuilder.header(CONTENT_TYPE, TEXT_PLAIN);
    if (requestMessageBody.isEmpty() || requestMessageBody.get().isBlank()) {
      logger.warn("Empty request from {}", requestMessage.getHeaders().get(FORWARDED_FOR));
    } else {
      var update = updateFactory.parseUpdate(requestMessageBody.get());

      try {
        var responseEventBody = (null != update) ? update.call() : null;

        if (nonNull(responseEventBody)) {
          responseMessageBuilder.body(responseEventBody);
          responseMessageBuilder.header(CONTENT_TYPE, APPLICATION_JSON);
        }
      } catch (Exception exception) {
        logger.warn("Update call from {}: {}\n{}", requestMessage.getHeaders().get(FORWARDED_FOR),
            exception.getMessage(), requestMessageBody.get()
                .substring(0, Math.min(requestMessageBody.get().length(), MAX_SUBSTRING_LENGTH)));
      }
    }

    return responseMessageBuilder.build();
  }

}
