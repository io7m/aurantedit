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

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * The application event service.
 */

public final class AEApplicationEvents implements AEApplicationEventsType
{
  private final SubmissionPublisher<AEApplicationEventType> events;

  private AEApplicationEvents()
  {
    this.events = new SubmissionPublisher<>();
  }

  /**
   * @return An application event service.
   */

  public static AEApplicationEventsType create()
  {
    return new AEApplicationEvents();
  }

  @Override
  public Flow.Publisher<AEApplicationEventType> events()
  {
    return this.events;
  }

  @Override
  public void publish(
    final AEApplicationEventType event)
  {
    this.events.submit(Objects.requireNonNull(event, "event"));
  }

  @Override
  public String description()
  {
    return "Application event service.";
  }
}
