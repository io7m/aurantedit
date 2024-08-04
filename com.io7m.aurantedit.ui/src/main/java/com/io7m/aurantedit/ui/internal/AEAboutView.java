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
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The "about" view.
 */

public final class AEAboutView
  implements AEViewType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEAboutView.class);

  @FXML private TextField version;
  @FXML private TextField build;

  /**
   * The "about" view.
   */

  public AEAboutView()
  {

  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    Thread.startVirtualThread(() -> {
      final var p = new Properties();

      try {
        final var u =
          AEAboutView.class.getResource(
            "/com/io7m/aurantedit/ui/internal/version.properties"
          );
        try (var s = u.openStream()) {
          p.load(s);
        }
      } catch (final IOException e) {
        LOG.error("I/O error: ", e);
      }

      Platform.runLater(() -> {
        this.version
          .setText(p.getProperty("version"));
        this.build
          .setText(p.getProperty("build"));
      });
    });
  }
}
