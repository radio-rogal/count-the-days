package io.gitlab.radio_rogal.count_days.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UpdateFactory {

  @Nullable Update parseUpdate(@NotNull String updateText);

}
