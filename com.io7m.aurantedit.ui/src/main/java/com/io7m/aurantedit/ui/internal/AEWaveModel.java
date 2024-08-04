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

import com.io7m.brackish.core.WaveModelType;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jsamplebuffer.api.SampleBufferReadableType;
import com.io7m.jsamplebuffer.api.SampleBufferType;

import java.util.Objects;
import java.util.Optional;

/**
 * A wave model that can display a sample buffer.
 */

public final class AEWaveModel implements WaveModelType
{
  private Optional<SampleBufferType> sample;
  private double[] frame;

  AEWaveModel()
  {
    this.sample = Optional.empty();
    this.frame = new double[1];
  }

  @Override
  public long frameCount()
  {
    return this.sample.map(SampleBufferReadableType::frames)
      .orElse(0L);
  }

  @Override
  public int channelCount()
  {
    return this.sample.map(SampleBufferReadableType::channels)
      .orElse(1);
  }

  @Override
  public double sample(
    final int channel,
    final long frameIndex)
    throws RangeCheckException
  {
    final var opt = this.sample;
    if (opt.isEmpty()) {
      return 0.0;
    }
    final var buffer = opt.get();
    buffer.frameGetExact(frameIndex, this.frame);
    return this.frame[channel];
  }

  @Override
  public double sampleOrDefault(
    final int channel,
    final long frameIndex,
    final double orElse)
  {
    try {
      return this.sample(channel, frameIndex);
    } catch (final Exception e) {
      return orElse;
    }
  }

  /**
   * Set the sample buffer.
   *
   * @param buffer The buffer
   */

  public void setWave(
    final Optional<SampleBufferType> buffer)
  {
    this.sample = Objects.requireNonNull(buffer, "buffer");
    if (buffer.isPresent()) {
      this.frame = new double[buffer.get().channels()];
    }
  }
}
