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

/**
 * The context of executing a command.
 */

public interface AEControllerCommandContextType
{
  /**
   * Set an attribute for error reporting.
   *
   * @param name  The name
   * @param value The value
   */

  void putAttribute(
    String name,
    Object value);

  /**
   * An operation is in progress.
   *
   * @param message  The message
   * @param progress The progress value
   */

  void eventInProgress(
    String message,
    double progress
  );

  /**
   * An operation is in progress.
   *
   * @param message The message
   * @param index   The operation number
   * @param count   The total number of operations
   */

  void eventInProgress(
    String message,
    long index,
    long count
  );
}
