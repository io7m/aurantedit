/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The string resource service.
 */

public final class AEStrings implements AEStringsType
{
  private final ResourceBundle resources;

  /**
   * The string resource service.
   *
   * @param locale The locale
   */

  public AEStrings(
    final Locale locale)
  {
    this.resources =
      ResourceBundle.getBundle(
        "/com/io7m/aurantedit/ui/internal/Strings",
        locale
      );
  }

  @Override
  public ResourceBundle resources()
  {
    return this.resources;
  }

  @Override
  public String toString()
  {
    return String.format(
      "[AEStrings 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }

  @Override
  public String description()
  {
    return "String resource service";
  }
}
