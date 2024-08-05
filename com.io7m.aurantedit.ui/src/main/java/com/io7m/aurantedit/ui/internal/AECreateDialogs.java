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


package com.io7m.aurantedit.ui.internal;

import com.io7m.repetoir.core.RPServiceDirectoryType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_CREATE_TITLE;

/**
 * A version set dialog.
 */

public final class AECreateDialogs
  extends AEDialogFactoryAbstract<AEUnit, AECreateView>
{
  /**
   * A version set dialog.
   *
   * @param services The service directory
   */

  public AECreateDialogs(
    final RPServiceDirectoryType services)
  {
    super(
      AECreateView.class,
      "/com/io7m/aurantedit/ui/internal/create.fxml",
      services,
      Modality.NONE
    );
  }

  @Override
  protected String createStageTitle(
    final AEUnit arguments)
  {
    return this.strings().format(AE_CREATE_TITLE);
  }

  @Override
  protected AEControllerFactoryType<AEViewType> controllerFactory(
    final AEUnit arguments,
    final Stage stage)
  {
    return AEControllerFactoryMapped.create(
      Map.entry(
        AECreateView.class,
        () -> new AECreateView(this.services(), stage)
      )
    );
  }

  @Override
  public String description()
  {
    return "Creation dialog service.";
  }

  @Override
  public String toString()
  {
    return String.format(
      "[%s 0x%08x]",
      this.getClass().getName(),
      Integer.valueOf(this.hashCode())
    );
  }
}
