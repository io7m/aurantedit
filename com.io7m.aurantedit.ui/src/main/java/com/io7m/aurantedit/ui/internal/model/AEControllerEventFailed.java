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

import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An operation failed.
 *
 * @param error The error
 */

public record AEControllerEventFailed(
  SStructuredError<String> error)
  implements AEControllerEventType, SStructuredErrorType<String>
{
  /**
   * An operation failed.
   *
   * @param error The error
   */

  public AEControllerEventFailed
  {
    Objects.requireNonNull(error, "error");
  }

  /**
   * Convert an exception into a failure event.
   *
   * @param e The exception
   *
   * @return An event based on the given exception
   */

  public static AEControllerEventFailed ofException(
    final Exception e)
  {
    if (e instanceof final SStructuredErrorType<?> error) {
      return new AEControllerEventFailed(
        new SStructuredError<>(
          error.errorCode().toString(),
          error.message(),
          error.attributes(),
          error.remediatingAction(),
          error.exception()
        )
      );
    }

    return new AEControllerEventFailed(
      new SStructuredError<>(
        "error-exception",
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
        Map.of(),
        Optional.empty(),
        Optional.of(e)
      )
    );
  }

  @Override
  public String errorCode()
  {
    return this.error.errorCode();
  }

  @Override
  public String message()
  {
    return this.error.message();
  }

  @Override
  public Map<String, String> attributes()
  {
    return this.error.attributes();
  }

  @Override
  public Optional<String> remediatingAction()
  {
    return this.error.remediatingAction();
  }

  @Override
  public Optional<Throwable> exception()
  {
    return this.error.exception();
  }
}
