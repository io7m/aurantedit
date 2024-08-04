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

import com.io7m.aurantium.api.AUClipDeclarations;
import com.io7m.aurantium.api.AUFileWritableType;
import com.io7m.aurantium.api.AUKeyAssignments;
import com.io7m.aurantium.api.AUVersion;
import com.io7m.aurantium.writer.api.AUWriteRequest;
import com.io7m.aurantium.writer.api.AUWriters;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Save a file.
 *
 * @param state          The state
 * @param outputFile     The output file
 * @param outputFileTemp The temporary output file
 */

public record AEControllerCommandSave(
  AEModelState state,
  Path outputFile,
  Path outputFileTemp)
  implements AEControllerCommandType
{
  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
    StandardOpenOption.WRITE,
    StandardOpenOption.READ,
  };

  private static final AUWriters WRITERS =
    new AUWriters();

  private static final AUVersion VERSION =
    new AUVersion(1, 0);

  @Override
  public void execute(
    final AEControllerCommandContextType context)
    throws Exception
  {
    context.eventInProgress("Saving file...", 0.0);

    final var writersOpt =
      WRITERS.findWriterFactoryFor(VERSION);

    if (writersOpt.isEmpty()) {
      throw new UnsupportedOperationException();
    }

    final var writers = writersOpt.get();
    try (var channel =
           FileChannel.open(this.outputFileTemp, OPEN_OPTIONS)) {

      final var writeRequest =
        new AUWriteRequest(
          channel,
          this.outputFile.toUri(),
          VERSION
        );

      context.eventInProgress("Writing file...", 0.0);

      try (var writer = writers.createWriter(writeRequest)) {
        try (var writable = writer.execute()) {
          this.writeIdentifier(context, writable);
          this.writeClips(context, writable);
          this.writeKeyAssignments(context, writable);
          this.writeMetadata(context, writable);
          this.writeEnd(context, writable);
        }
      }

      context.eventInProgress("Writing file...", 1.0);
    }

    Files.move(
      this.outputFileTemp,
      this.outputFile,
      StandardCopyOption.REPLACE_EXISTING,
      StandardCopyOption.ATOMIC_MOVE
    );

    Platform.runLater(this.state::setSaved);
  }

  private void writeEnd(
    final AEControllerCommandContextType context,
    final AUFileWritableType file)
    throws IOException
  {
    context.eventInProgress("Writing end section...", 0.0);

    try (var ignored = file.createSectionEnd()) {
      // Nothing required.
    }

    context.eventInProgress("Writing end section...", 1.0);
  }

  private void writeMetadata(
    final AEControllerCommandContextType context,
    final AUFileWritableType file)
    throws IOException
  {
    context.eventInProgress("Writing metadata...", 0.0);

    try (var section = file.createSectionMetadata()) {
      final var meta =
        new HashMap<String, ArrayList<String>>();
      final var metas =
        this.state.metadata();

      context.eventInProgress("Writing metadata...", 0, meta.size());

      int index = 0;
      for (var value : metas) {
        context.eventInProgress("Writing metadata...", index, meta.size());
        var existing = meta.get(value.name());
        if (existing == null) {
          existing = new ArrayList<>();
        }
        existing.add(value.value());
        meta.put(value.name(), existing);
        ++index;
      }
      section.setMetadata(Collections.unmodifiableMap(meta));
      context.eventInProgress("Writing metadata...", 1.0);
    }
  }

  private void writeKeyAssignments(
    final AEControllerCommandContextType context,
    final AUFileWritableType file)
    throws IOException
  {
    context.eventInProgress("Writing key assignments...", 0.0);

    try (var section = file.createSectionKeyAssignments()) {
      section.setKeyAssignments(
        new AUKeyAssignments(this.state.keyAssignments())
      );
    }

    context.eventInProgress("Writing key assignments...", 1.0);
  }

  private void writeClips(
    final AEControllerCommandContextType context,
    final AUFileWritableType file)
    throws IOException
  {
    context.eventInProgress("Writing clips...", 0.0);

    try (var section = file.createSectionClips()) {
      final var declarations =
        new AUClipDeclarations(this.state().clips());
      final var writable =
        section.createClips(declarations);

      context.eventInProgress(
        "Writing clips...",
        0,
        declarations.declarations().size());

      int index = 0;
      for (var declaration : declarations.declarations()) {
        try (var channel = writable.writeAudioDataForClip(declaration.id())) {
          channel.write(this.state.clipRawBuffer(declaration.id()));
          ++index;
        }
        context.eventInProgress(
          "Writing clips...",
          index,
          declarations.declarations().size());
      }
    }
  }

  private void writeIdentifier(
    final AEControllerCommandContextType context,
    final AUFileWritableType file)
    throws IOException
  {
    context.eventInProgress("Writing identifier...", 0.0);

    try (var section = file.createSectionIdentifier()) {
      section.setIdentifier(this.state.identifier().get());
    }

    context.eventInProgress("Writing identifier...", 1.0);
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
    return "Save file";
  }
}
