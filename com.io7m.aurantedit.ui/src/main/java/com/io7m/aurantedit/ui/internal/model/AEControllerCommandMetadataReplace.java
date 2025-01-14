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

import java.util.Objects;

/**
 * Replace a metadata value.
 */

public final class AEControllerCommandMetadataReplace
  implements AEControllerCommandType
{
  private final AEMetadata meta;
  private final AEModelState state;
  private AEMetadata oldMeta;

  /**
   * Replace a metadata value.
   *
   * @param inState The model state
   * @param inMeta  The value
   */

  public AEControllerCommandMetadataReplace(
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
    context.putAttribute("Metadata (Name)", this.meta.name());
    context.putAttribute("Metadata (Value)", this.meta.value());
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
    context.putAttribute("Metadata (Name)", this.meta.name());
    context.putAttribute("Metadata (Value)", this.meta.value());
  }

  @Override
  public String describe()
  {
    return "Replace metadata";
  }
}
