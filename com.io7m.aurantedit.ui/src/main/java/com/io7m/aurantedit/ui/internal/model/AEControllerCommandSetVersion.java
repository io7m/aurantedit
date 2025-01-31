/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.aurantedit.ui.internal.model;

import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.aurantium.api.AUVersion;
import javafx.application.Platform;

import java.util.Objects;
import java.util.Optional;

/**
 * Set the sample map version.
 */

public final class AEControllerCommandSetVersion
  implements AEControllerCommandType
{
  private final AUVersion newVersion;
  private final AEModelState state;
  private volatile AUIdentifier oldIdentifier;

  /**
   * Set the sample map version.
   *
   * @param inState      The model state
   * @param inNewVersion The version
   */

  public AEControllerCommandSetVersion(
    final AEModelState inState,
    final AUVersion inNewVersion)
  {
    this.state =
      Objects.requireNonNull(inState, "state");
    this.newVersion =
      Objects.requireNonNull(inNewVersion, "newVersion");
  }

  @Override
  public void execute(
    final AEControllerCommandContextType context)
  {
    context.putAttribute("Version", this.newVersion);

    Platform.runLater(() -> {
      this.oldIdentifier =
        this.state.identifier()
          .getValue();

      this.state.setIdentifier(
        Optional.of(
          new AUIdentifier(
            this.oldIdentifier.name(),
            this.newVersion
          )
        )
      );
    });
  }

  @Override
  public boolean isUndoable()
  {
    return true;
  }

  @Override
  public void undo(
    final AEControllerCommandContextType context)
  {
    Platform.runLater(() -> {
      this.state.setIdentifier(Optional.of(this.oldIdentifier));
    });
  }

  @Override
  public String describe()
  {
    return "Set version";
  }
}
