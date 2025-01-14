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

import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Delete an audio clip.
 */

public final class AEControllerCommandClipDelete
  implements AEControllerCommandType
{
  private final AEModelState state;
  private final AUClipID clipId;
  private AUClipDeclaration clipOld;
  private SampleBufferType clipBufferOld;
  private ByteBuffer clipBufferRawOld;

  /**
   * @param inState  The model state
   * @param inClipID The clip ID
   */

  public AEControllerCommandClipDelete(
    final AEModelState inState,
    final AUClipID inClipID)
  {
    this.state =
      Objects.requireNonNull(inState, "state");
    this.clipId =
      Objects.requireNonNull(inClipID, "inClipID");
  }

  @Override
  public void execute(
    final AEControllerCommandContextType context)
    throws Exception
  {
    context.putAttribute("Clip", this.clipId);

    context.eventInProgress("Deleting audio clip...", 0.0);

    Platform.runLater(() -> {
      this.clipOld =
        this.state.clipGet(this.clipId);
      this.clipBufferOld =
        this.state.clipBuffer(this.clipId);
      this.clipBufferRawOld =
        this.state.clipRawBuffer(this.clipId);

      this.state.clipDelete(this.clipId);
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
    context.putAttribute("Clip", this.clipId);

    Platform.runLater(() -> {
      this.state.clipDelete(this.clipId);
      this.state.clipAdd(this.clipOld);
      this.state.clipBufferPut(this.clipId, this.clipBufferOld);
      this.state.clipRawBufferPut(this.clipId, this.clipBufferRawOld);
    });
  }

  @Override
  public String describe()
  {
    return "Delete audio clip";
  }
}
