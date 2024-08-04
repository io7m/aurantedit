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

import com.io7m.lanark.core.RDottedName;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A name set view.
 */

public final class AENameSetView implements AEViewType
{
  private final Stage stage;
  private Optional<RDottedName> result;

  @FXML private Button update;
  @FXML private TextField text;

  /**
   * A name set view.
   *
   * @param inStage The stage
   */

  public AENameSetView(
    final Stage inStage)
  {
    this.stage =
      Objects.requireNonNull(inStage, "stage");
    this.result =
      Optional.empty();
  }

  /**
   * @return The result
   */

  public Optional<RDottedName> result()
  {
    return this.result;
  }

  /**
   * Set the initial value
   *
   * @param nameInitial The initial value
   */

  public void setNameInitial(
    final RDottedName nameInitial)
  {
    this.text.setText(nameInitial.value());
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.text.textProperty()
      .addListener(observable -> this.validate());
  }

  private void validate()
  {
    this.update.setDisable(true);

    try {
      new RDottedName(this.text.getText().trim());
      this.update.setDisable(false);
    } catch (final Exception e) {
      this.update.setDisable(true);
    }
  }

  @FXML
  private void onUpdateSelected()
  {
    this.result = Optional.of(new RDottedName(this.text.getText().trim()));
    this.stage.close();
  }

  @FXML
  private void onCancelSelected()
  {
    this.stage.close();
  }
}
