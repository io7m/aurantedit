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
import java.util.function.Consumer;

/**
 * A perpetual event subscriber.
 *
 * @param <T> The type of events
 */

public final class AEPerpetualSubscriber<T> implements Flow.Subscriber<T>
{
  private final Consumer<T> onEvent;

  /**
   * A perpetual event subscriber.
   *
   * @param inOnEvent The event consumer
   */

  public AEPerpetualSubscriber(
    final Consumer<T> inOnEvent)
  {
    this.onEvent =
      Objects.requireNonNull(inOnEvent, "onEvent");
  }

  @Override
  public void onSubscribe(
    final Flow.Subscription subscription)
  {
    subscription.request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(
    final T item)
  {
    this.onEvent.accept(Objects.requireNonNull(item, "item"));
  }

  @Override
  public void onError(
    final Throwable throwable)
  {

  }

  @Override
  public void onComplete()
  {

  }
}
