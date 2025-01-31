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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * A save confirmation view.
 */

public final class AESaveConfirmView implements AEViewType
{
  private final Stage stage;
  private final Path file;
  private AESaveConfirmation result;

  @FXML private Button cancel;
  @FXML private Button discard;
  @FXML private Button save;
  @FXML private Label fileLabel;

  /**
   * A save confirmation view.
   *
   * @param inStage The stage
   * @param inFile  The file
   */

  public AESaveConfirmView(
    final Stage inStage,
    final Path inFile)
  {
    this.stage =
      Objects.requireNonNull(inStage, "stage");
    this.result =
      AESaveConfirmation.CANCEL;
    this.file =
      Objects.requireNonNull(inFile, "file");
  }

  /**
   * @return The response to confirmation
   */

  public AESaveConfirmation result()
  {
    return this.result;
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.fileLabel.setText(this.file.toAbsolutePath().toString());

    Platform.runLater(() -> {
      this.cancel.requestFocus();
    });
  }

  @FXML
  private void onSaveSelected()
  {
    this.result = AESaveConfirmation.SAVE;
    this.stage.close();
  }

  @FXML
  private void onDiscardSelected()
  {
    this.result = AESaveConfirmation.DISCARD;
    this.stage.close();
  }

  @FXML
  private void onCancelSelected()
  {
    this.result = AESaveConfirmation.CANCEL;
    this.stage.close();
  }
}
