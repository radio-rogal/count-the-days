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
package io.gitlab.radio_rogal.count_days.bot;

public enum TelegramField {

  CacheTime("cache_time"), ChatID("chat_id"), Description("description"), DisableWebPagePreview(
      "disable_web_page_preview"), Id("id"), InlineQueryId("inline_query_id"), InputMessageContent(
      "input_message_content"), IsPersonal("is_personal"), Keyboard("keyboard"), Latitude(
      "latitude"), Longitude("longitude"), MessageId("message_id"), MessageText(
      "message_text"), Method("method"), OneTimeKeyboard("one_time_keyboard"), ParseMode(
      "parse_mode"), ReplyMarkup("reply_markup"), ReplyToMessageID(
      "request_location"), ResizeKeyboard("resize_keyboard"), Results("results"), Text(
      "text"), Title("title"), ThumbHeight("thumb_height"), ThumbLocation("thumb_url"), ThumbWidth(
      "thumb_width"), Type("type");

  private final String fieldName;

  TelegramField(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public String toString() {
    return fieldName;
  }

}
