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

import com.io7m.aurantium.api.AUAudioFormatType;
import com.io7m.aurantium.api.AUAudioFormatType.AUAudioFormatStandard;
import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUHashAlgorithm;
import com.io7m.aurantium.api.AUHashValue;
import com.io7m.aurantium.api.AUOctetOrder;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import javafx.application.Platform;
import org.kc7bfi.jflac.sound.spi.FlacEncoding;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;

/**
 * Add a new audio clip.
 */

public final class AEControllerCommandClipAdd
  implements AEControllerCommandType
{
  private final AEModelState state;
  private final Path file;
  private AUClipID clipId;

  /**
   * @param inState The model state
   * @param inFile  The file
   */

  public AEControllerCommandClipAdd(
    final AEModelState inState,
    final Path inFile)
  {
    this.state =
      Objects.requireNonNull(inState, "state");
    this.file =
      Objects.requireNonNull(inFile, "file");
  }

  @Override
  public void execute(
    final AEControllerCommandContextType context)
    throws Exception
  {
    context.putAttribute("File", this.file);

    context.eventInProgress("Opening audio clip...", 0.0);

    final byte[] audioBytes;
    final AudioFormat format;
    final SampleBufferType sampleBuffer;

    try (var resources =
           CloseableCollection.create()) {
      final var stream =
        resources.add(Files.newInputStream(this.file));
      final var bufStream =
        resources.add(new BufferedInputStream(stream));
      final var audioStream =
        AudioSystem.getAudioInputStream(bufStream);

      audioBytes =
        audioStream.readAllBytes();
      format =
        audioStream.getFormat();

      context.putAttribute("Big Endian", format.isBigEndian());
      context.putAttribute("Channels", format.getChannels());
      context.putAttribute("Encoding", format.getEncoding());
      context.putAttribute("Frame Rate", format.getFrameRate());
      context.putAttribute("Frame Size", format.getFrameSize());
      context.putAttribute("Sample Rate", format.getSampleRate());
      context.putAttribute("Sample Size", format.getSampleSizeInBits());

      sampleBuffer = this.createDisplaySampleBuffer();
    }

    context.eventInProgress("Opening audio clip...", 1.0);

    this.clipId =
      this.state.clipFreshID();

    final var clipDeclaration =
      new AUClipDeclaration(
        this.clipId,
        this.file.getFileName().toString(),
        this.audioFormatOf(format),
        this.sampleRateOf(format),
        this.sampleDepthOf(format),
        this.channelsOf(format),
        this.endiannessOf(format),
        this.hashOf(audioBytes),
        Integer.toUnsignedLong(audioBytes.length)
      );

    Platform.runLater(() -> {
      this.state.clipAdd(clipDeclaration);
      this.state.clipBufferPut(this.clipId, sampleBuffer);
      this.state.clipRawBufferPut(this.clipId, ByteBuffer.wrap(audioBytes));
    });
  }

  private SampleBufferType createDisplaySampleBuffer()
    throws Exception
  {
    try (var resources =
           CloseableCollection.create()) {
      final var stream =
        resources.add(Files.newInputStream(this.file));
      final var bufStream =
        resources.add(new BufferedInputStream(stream));
      final var audioStream =
        AudioSystem.getAudioInputStream(bufStream);
      final var sourceFormat =
        audioStream.getFormat();

      final var targetStream =
        AudioSystem.getAudioInputStream(
          new AudioFormat(
            sourceFormat.getSampleRate(),
            16,
            sourceFormat.getChannels(),
            true,
            false
          ),
          audioStream
        );

      return SXMSampleBuffers.readSampleBufferFromStream(
        targetStream,
        SampleBufferFloat::createWithHeapBuffer
      );
    }
  }

  private AUHashValue hashOf(
    final byte[] audioBytes)
    throws NoSuchAlgorithmException
  {
    final var messageDigest =
      MessageDigest.getInstance("SHA-256");

    return new AUHashValue(
      AUHashAlgorithm.HA_SHA256,
      HexFormat.of().formatHex(messageDigest.digest(audioBytes))
    );
  }

  private AUOctetOrder endiannessOf(
    final AudioFormat format)
  {
    if (format.isBigEndian()) {
      return AUOctetOrder.BIG_ENDIAN;
    }
    return AUOctetOrder.LITTLE_ENDIAN;
  }

  private long channelsOf(
    final AudioFormat format)
  {
    return format.getChannels();
  }

  private long sampleDepthOf(
    final AudioFormat format)
  {
    return format.getSampleSizeInBits();
  }

  private long sampleRateOf(
    final AudioFormat format)
  {
    return (long) format.getSampleRate();
  }

  private AUAudioFormatType audioFormatOf(
    final AudioFormat format)
    throws IOException
  {
    final var encoding = format.getEncoding();
    if (Objects.equals(encoding, PCM_SIGNED)) {
      return AUAudioFormatStandard.AFPCMLinearIntegerSigned;
    }
    if (Objects.equals(encoding, PCM_UNSIGNED)) {
      return AUAudioFormatStandard.AFPCMLinearIntegerUnsigned;
    }
    if (Objects.equals(encoding, PCM_FLOAT)) {
      return AUAudioFormatStandard.AFPCMLinearFloat;
    }
    if (encoding instanceof FlacEncoding) {
      return AUAudioFormatStandard.AFFlac;
    }

    throw new IOException("Unsupported audio format.");
  }

  @Override
  public boolean isUndoable()
  {
    return true;
  }

  @Override
  public void undo(
    final AEControllerCommandContextType context)
  {
    context.putAttribute("File", this.file);

    Platform.runLater(() -> {
      this.state.clipDelete(this.clipId);
    });
  }

  @Override
  public String describe()
  {
    return "Add audio clip";
  }
}
