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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Access to version strings.
 */

public final class AEVersionStrings
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEVersionStrings.class);

  private AEVersionStrings()
  {

  }

  /**
   * The application version.
   */

  public static final String VERSION;

  /**
   * The application build.
   */

  public static final String BUILD;

  static {
    String version = "aurantedit 0.0.0";
    String build = "";

    final var p = new Properties();
    try {
      final var u =
        AEVersionStrings.class.getResource(
          "/com/io7m/aurantedit/ui/internal/version.properties"
        );
      try (var stream = u.openStream()) {
        p.load(stream);
        version =
          "aurantedit %s".formatted(
            Objects.requireNonNullElse(
              p.getProperty("version"), "0.0.0")
          );
        build =
          Objects.requireNonNullElse(p.getProperty("build"), "");
      }
    } catch (final IOException e) {
      LOG.error("I/O error: ", e);
    }

    VERSION = version;
    BUILD = build;
  }
}
