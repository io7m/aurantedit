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
import javafx.application.Platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create a new file.
 *
 * @param state      The model state
 * @param identifier The identifier
 * @param file       The file
 */

public record AEControllerCommandCreate(
  AEModelState state,
  AUIdentifier identifier,
  Path file)
  implements AEControllerCommandType
{
  @Override
  public void execute(
    final AEControllerCommandContextType context)
  {
    Platform.runLater(() -> {
      this.state.setFile(this.file);
      this.state.setMeta(List.of());
      this.state.setIdentifier(Optional.of(this.identifier));
      this.state.setClips(List.of());
      this.state.setClipBuffers(Map.of());
    });
  }

  @Override
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void undo(
    final AEControllerCommandContextType context)
  {

  }

  @Override
  public String describe()
  {
    return "Create new file";
  }
}
