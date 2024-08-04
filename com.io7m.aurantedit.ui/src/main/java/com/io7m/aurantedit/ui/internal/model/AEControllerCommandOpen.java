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

import com.io7m.aurantium.api.AUAudioFormatType.AUAudioFormatStandard;
import com.io7m.aurantium.api.AUAudioFormatType.AUAudioFormatUnknown;
import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipDescription;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUFileReadableType;
import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.aurantium.api.AUKeyAssignment;
import com.io7m.aurantium.parser.api.AUParseRequest;
import com.io7m.aurantium.parser.api.AUParsers;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import javafx.application.Platform;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Open a file.
 *
 * @param state The model state
 * @param file  The file
 */

public record AEControllerCommandOpen(
  AEModelState state,
  Path file)
  implements AEControllerCommandType
{
  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.WRITE,
    StandardOpenOption.READ,
  };

  private static final AUParsers PARSERS =
    new AUParsers();

  @Override
  public void execute(
    final AEControllerCommandContextType context)
    throws Exception
  {
    context.putAttribute("File", this.file);
    context.eventInProgress("Opening file...", 0.0);

    try (var channel =
           FileChannel.open(this.file, OPEN_OPTIONS)) {

      final var request =
        new AUParseRequest(
          channel,
          this.file.toUri(),
          1024L,
          1024L
        );

      try (var parser = PARSERS.createParser(request)) {
        context.eventInProgress("Parsing file...", 0.0);

        final var readable =
          parser.execute();
        final var identifier =
          parseIdentifier(context, readable);
        final var metaList =
          parseMetadata(context, readable);

        context.eventInProgress("Parsing clips...", 0.0);

        final var clipsOptional = readable.openClips();
        final List<AUClipDescription> clipList;
        final Map<AUClipID, SampleBufferType> clipData;
        final Map<AUClipID, ByteBuffer> clipRawData;
        if (clipsOptional.isPresent()) {
          final var clips =
            clipsOptional.get();

          clipList = clips.clips();
          clipData = new HashMap<>(clipList.size());
          clipRawData = new HashMap<>(clipList.size());

          final var index = 0L;
          for (final var clip : clipList) {
            context.eventInProgress(
              "Parsing clip", index, clipList.size());

            try (var data = clips.audioDataForClip(clip)) {
              clipData.put(clip.id(), decodeSampleData(clip, data));
            }
            try (var data = clips.audioDataForClip(clip)) {
              clipRawData.put(clip.id(), readSampleData(data));
            }
          }
        } else {
          clipList = List.of();
          clipData = Map.of();
          clipRawData = Map.of();
        }

        final var keyAssignments =
          parseKeys(context, readable);

        context.eventInProgress("Finished opening file.", 1.0);

        Platform.runLater(() -> {
          this.state.setFile(this.file);
          this.state.setMeta(metaList);
          this.state.setIdentifier(identifier);
          this.state.setClips(
            clipList.stream()
              .map(clip -> {
                return new AUClipDeclaration(
                  clip.id(),
                  clip.name(),
                  clip.format(),
                  clip.sampleRate(),
                  clip.sampleDepth(),
                  clip.channels(),
                  clip.endianness(),
                  clip.hash(),
                  clip.size()
                );
              })
              .toList()
          );
          this.state.setKeyAssignments(keyAssignments);
          this.state.setClipBuffers(clipData);
          this.state.setClipRawData(clipRawData);
          this.state.setSaved();
        });
      }
    }
  }

  private static List<AUKeyAssignment> parseKeys(
    final AEControllerCommandContextType context,
    final AUFileReadableType file)
    throws IOException
  {
    context.eventInProgress("Parsing keys...", 0.0);

    final var keysOpt = file.openKeyAssignments();
    final List<AUKeyAssignment> keyAssignments;
    if (keysOpt.isPresent()) {
      keyAssignments = List.copyOf(
        keysOpt.get()
          .keyAssignments()
          .assignments()
      );
    } else {
      keyAssignments = List.of();
    }
    return keyAssignments;
  }

  private static List<AEMetadata> parseMetadata(
    final AEControllerCommandContextType context,
    final AUFileReadableType file)
    throws IOException
  {
    context.eventInProgress("Parsing metadata...", 0.0);

    final var metaOpt = file.openMetadata();
    final List<AEMetadata> metaList;
    if (metaOpt.isPresent()) {
      final var meta = metaOpt.get();
      metaList = new ArrayList<>();
      final var metadata = meta.metadata();

      final var count =
        metadata.values()
          .stream()
          .flatMap(Collection::stream)
          .count();

      var index = 0L;
      for (var entry : metadata.entrySet()) {
        context.eventInProgress(
          "Parsing metadata entry", index, count);

        for (var value : entry.getValue()) {
          metaList.add(new AEMetadata(entry.getKey(), value));
          ++index;
        }
      }
    } else {
      metaList = List.of();
    }
    return metaList;
  }

  private static Optional<AUIdentifier> parseIdentifier(
    final AEControllerCommandContextType context,
    final AUFileReadableType file)
    throws IOException
  {
    context.eventInProgress("Parsing identifier...", 0.0);

    final var idOpt = file.openIdentifier();
    final Optional<AUIdentifier> identifier;
    if (idOpt.isPresent()) {
      final var id = idOpt.get();
      identifier = Optional.of(id.identifier());
    } else {
      identifier = Optional.empty();
    }
    return identifier;
  }

  private static ByteBuffer readSampleData(
    final SeekableByteChannel data)
    throws IOException
  {
    try (var stream = Channels.newInputStream(data)) {
      return ByteBuffer.wrap(stream.readAllBytes());
    }
  }

  private static SampleBufferType decodeSampleData(
    final AUClipDescription clip,
    final SeekableByteChannel data)
    throws UnsupportedAudioFileException, IOException
  {
    final var sourceEncoding =
      switch (clip.format()) {
        case final AUAudioFormatStandard standard -> switch (standard) {
          case AFPCMLinearIntegerSigned -> AudioFormat.Encoding.PCM_SIGNED;
          case AFPCMLinearIntegerUnsigned -> AudioFormat.Encoding.PCM_UNSIGNED;
          case AFPCMLinearFloat -> AudioFormat.Encoding.PCM_FLOAT;
          case AFFlac -> {
            throw new UnsupportedOperationException();
          }
        };
        case final AUAudioFormatUnknown ignored ->
          throw new UnsupportedOperationException();
      };

    final var sourceSampleRate =
      (float) clip.sampleRate();
    final var sourceSampleDepth =
      (int) clip.sampleDepth();
    final var sourceChannels =
      (int) clip.channels();
    final var sourceFrameSize =
      (int) clip.frameSizeOctets();
    final var sourceFrameRate =
      (float) clip.sampleRate();
    final var sourceBigEndian =
      switch (clip.endianness()) {
        case BIG_ENDIAN -> true;
        case LITTLE_ENDIAN -> false;
      };

    final var sourceFormat =
      new AudioFormat(
        sourceEncoding,
        sourceSampleRate,
        sourceSampleDepth,
        sourceChannels,
        sourceFrameSize,
        sourceFrameRate,
        sourceBigEndian
      );

    try (var dataStream =
           Channels.newInputStream(data)) {
      try (var sourceStream =
             new AudioInputStream(dataStream, sourceFormat, clip.frames())) {
        return SXMSampleBuffers.readSampleBufferFromStream(
          sourceStream,
          SampleBufferFloat::createWithHeapBuffer
        );
      }
    }
  }

  @Override
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void undo(
    final AEControllerCommandContextType context)
  {

  }

  @Override
  public String describe()
  {
    return "Open file";
  }
}
