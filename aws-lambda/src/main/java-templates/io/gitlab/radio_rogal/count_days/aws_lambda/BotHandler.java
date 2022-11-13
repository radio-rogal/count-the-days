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
package io.gitlab.radio_rogal.count_days.aws_lambda;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.gitlab.radio_rogal.count_days.bot.CountDaysUpdateFactory;
import io.gitlab.radio_rogal.count_days.bot.UpdateFactory;
import java.util.Optional;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BotHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String FORWARDED_FOR = "X-Forwarded-For";
  private static final int MAX_SUBSTRING_LENGTH = 1024;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final UpdateFactory updateFactory;

  public BotHandler() {
    this(new CountDaysUpdateFactory());
  }

  @VisibleForTesting
  BotHandler(UpdateFactory updateFactory) {
    this.updateFactory = updateFactory;
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent,
      Context context) {
    MDC.put("@aws-request-id@", context.getAwsRequestId());

    Optional<APIGatewayProxyResponseEvent> responseEvent = empty();
    var requestEventBody = requestEvent.getBody();

    if (isNull(requestEventBody) || requestEventBody.isBlank()) {
      logger.warn("Empty request from {}", requestEvent.getHeaders().get(FORWARDED_FOR));
    } else {
      var update = updateFactory.parseUpdate(requestEventBody);

      try {
        var responseEventBody = (null != update) ? update.call() : null;

        if (nonNull(responseEventBody)) {
          responseEvent = Optional.of(LambdaUtils.getResponseEvent(update.call()));
        }
      } catch (Exception exception) {
        logger.warn("Update call from {}: {}\n{}", requestEvent.getHeaders().get(FORWARDED_FOR),
            exception.getMessage(), requestEventBody.substring(0,
                Math.min(requestEventBody.length(), MAX_SUBSTRING_LENGTH)));
      }
    }

    return responseEvent.orElseGet(LambdaUtils::responseOK);
  }

}
