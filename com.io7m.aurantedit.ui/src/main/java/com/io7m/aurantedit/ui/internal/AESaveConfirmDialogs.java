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

import java.nio.file.Path;
import java.util.Map;

import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_SAVE_TITLE;

/**
 * A version set dialog.
 */

public final class AESaveConfirmDialogs
  extends AEDialogFactoryAbstract<Path, AESaveConfirmView>
{
  /**
   * A version set dialog.
   *
   * @param services The service directory
   */

  public AESaveConfirmDialogs(
    final RPServiceDirectoryType services)
  {
    super(
      AESaveConfirmView.class,
      "/com/io7m/aurantedit/ui/internal/saveConfirm.fxml",
      services,
      Modality.NONE
    );
  }

  @Override
  protected String createStageTitle(
    final Path arguments)
  {
    return this.strings().format(AE_SAVE_TITLE);
  }

  @Override
  protected AEControllerFactoryType<AEViewType> controllerFactory(
    final Path arguments,
    final Stage stage)
  {
    return AEControllerFactoryMapped.create(
      Map.entry(
        AESaveConfirmView.class,
        () -> new AESaveConfirmView(stage, arguments)
      )
    );
  }

  @Override
  public String description()
  {
    return "Save confirmation dialog service.";
  }
}
