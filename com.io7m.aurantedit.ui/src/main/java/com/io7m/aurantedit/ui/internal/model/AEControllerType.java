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

import com.io7m.aurantedit.ui.internal.AEUnit;
import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.aurantium.api.AUVersion;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.lanark.core.RDottedName;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.transformation.SortedList;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * A controller.
 */

public interface AEControllerType
  extends AutoCloseable
{
  /**
   * @return The controller events
   */

  Flow.Publisher<AEControllerEventType> events();

  /**
   * @return The current file
   */

  ReadOnlyObjectProperty<Optional<Path>> file();

  /**
   * @return The metadata list
   */

  SortedList<AEMetadata> metadata();

  /**
   * @return The clip list
   */

  SortedList<AUClipDeclaration> clips();

  /**
   * @param id The clip ID
   *
   * @return The buffer for the given clip
   */

  SampleBufferType clipBuffer(
    AUClipID id);

  /**
   * @return The identifier
   */

  ReadOnlyObjectProperty<AUIdentifier> identifier();

  /**
   * Set the metadata filter.
   *
   * @param text The metadata filter
   */

  void setMetadataFilter(
    String text);

  /**
   * Set the clip filter.
   *
   * @param text The clip filter
   */

  void setClipFilter(
    String text);

  /**
   * @return A property that indicates if there is unsaved data
   */

  ReadOnlyBooleanProperty isSaveRequired();

  /**
   * Set the current version.
   *
   * @param version The version
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> setVersion(
    AUVersion version);

  /**
   * Set the current name.
   *
   * @param name The name
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> setName(
    RDottedName name);

  @Override
  void close();

  /**
   * @return The tip of the current undo stack
   */

  ReadOnlyObjectProperty<Optional<String>> undoState();

  /**
   * @return The tip of the current redo stack
   */

  ReadOnlyObjectProperty<Optional<String>> redoState();

  /**
   * Undo the latest undoable operation.
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> undo();

  /**
   * Redo the latest undoable operation.
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> redo();

  /**
   * Clear the undo/redo stacks.
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> clearUndoStack();

  /**
   * Create a new file.
   *
   * @param file       The file
   * @param identifier The identifier
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> create(
    Path file,
    AUIdentifier identifier);

  /**
   * Open a file.
   *
   * @param file The file
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> open(
    Path file);

  /**
   * Save the current file.
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> save();

  /**
   * Save the current file under a new name.
   *
   * @param path The new file
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> saveAs(Path path);

  /**
   * Add a metadata value.
   *
   * @param meta The value
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> metadataAdd(
    AEMetadata meta);

  /**
   * Remove a metadata value.
   *
   * @param meta The value
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> metadataRemove(
    AEMetadata meta);

  /**
   * Replace a metadata value.
   *
   * @param meta The value
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> metadataReplace(
    AEMetadata meta);

  /**
   * Add a clip.
   *
   * @param path The path
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> clipAdd(
    Path path);

  /**
   * Replace a clip.
   *
   * @param clipID The clip ID
   * @param path   The path
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> clipReplace(
    AUClipID clipID,
    Path path);

  /**
   * Delete a clip.
   *
   * @param clipID The clip ID
   *
   * @return The operating in progress
   */

  CompletableFuture<AEUnit> clipDelete(
    AUClipID clipID);
}
