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

import javafx.application.Platform;

import java.util.Objects;

/**
 * Add a metadata value.
 */

public final class AEControllerCommandMetadataAdd
  implements AEControllerCommandType
{
  private final AEMetadata meta;
  private final AEModelState state;

  /**
   * Add a metadata value.
   *
   * @param inState The model state
   * @param inMeta  The value
   */

  public AEControllerCommandMetadataAdd(
    final AEModelState inState,
    final AEMetadata inMeta)
  {
    this.state =
      Objects.requireNonNull(inState, "state");
    this.meta =
      Objects.requireNonNull(inMeta, "newName");
  }

  @Override
  public void execute(
    final AEControllerCommandContextType context)
  {
    Platform.runLater(() -> {
      this.state.metadataAdd(this.meta);
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
      this.state.metadataRemove(this.meta);
    });
  }

  @Override
  public String describe()
  {
    return "Add metadata";
  }
}
