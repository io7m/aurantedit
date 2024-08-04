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

import com.io7m.aurantium.api.AUVersion;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A version set view.
 */

public final class AEVersionSetView implements AEViewType
{
  private final Stage stage;
  private Optional<AUVersion> version;

  @FXML private Spinner<Long> versionMajor;
  @FXML private Spinner<Long> versionMinor;

  /**
   * A version set view.
   *
   * @param inStage The stage
   */

  public AEVersionSetView(
    final Stage inStage)
  {
    this.stage =
      Objects.requireNonNull(inStage, "stage");
    this.version =
      Optional.empty();
  }

  /**
   * @return The result
   */

  public Optional<AUVersion> result()
  {
    return this.version;
  }

  /**
   * Set the initial value
   *
   * @param initialVersion The initial value
   */

  public void setVersionInitial(
    final AUVersion initialVersion)
  {
    this.versionMajor.getValueFactory()
      .setValue(Integer.toUnsignedLong(initialVersion.major()));
    this.versionMinor.getValueFactory()
      .setValue(Integer.toUnsignedLong(initialVersion.minor()));
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.versionMajor.setValueFactory(new AEUnsignedIntValueFactory());
    this.versionMinor.setValueFactory(new AEUnsignedIntValueFactory());
  }

  @FXML
  private void onUpdateSelected()
  {
    this.version = Optional.of(
      new AUVersion(
        Math.toIntExact(this.versionMajor.getValue()),
        Math.toIntExact(this.versionMinor.getValue())
      )
    );
    this.stage.close();
  }

  @FXML
  private void onCancelSelected()
  {
    this.stage.close();
  }
}
