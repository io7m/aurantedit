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

import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.aurantium.api.AUKeyAssignment;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A mutable model state.
 */

public final class AEModelState
{
  private final ObservableList<AUClipDeclaration> clips;
  private final FilteredList<AUClipDeclaration> clipsFiltered;
  private final SortedList<AUClipDeclaration> clipsSorted;
  private final ObservableList<AEMetadata> metas;
  private final FilteredList<AEMetadata> metasFiltered;
  private final SortedList<AEMetadata> metasSorted;
  private final SimpleObjectProperty<AUIdentifier> identifier;
  private final Map<AUClipID, SampleBufferType> clipBuffers;
  private final Map<AUClipID, ByteBuffer> clipRawBuffers;
  private final SimpleBooleanProperty isSaveRequired;
  private final SimpleObjectProperty<Optional<Path>> file;
  private final ObservableList<AUKeyAssignment> keyAssignments;

  /**
   * A mutable model state.
   */

  public AEModelState()
  {
    this.file =
      new SimpleObjectProperty<>(Optional.empty());
    this.clips =
      FXCollections.observableArrayList();
    this.clipsFiltered =
      new FilteredList<>(this.clips);
    this.clipsSorted =
      new SortedList<>(this.clipsFiltered);
    this.clipBuffers =
      new HashMap<>();
    this.clipRawBuffers =
      new HashMap<>();

    this.keyAssignments =
      FXCollections.observableArrayList();

    this.identifier =
      new SimpleObjectProperty<>();
    this.metas =
      FXCollections.observableArrayList();
    this.metasFiltered =
      new FilteredList<>(this.metas);
    this.metasSorted =
      new SortedList<>(this.metasFiltered);

    this.isSaveRequired =
      new SimpleBooleanProperty();
  }

  /**
   * Set the model clips.
   *
   * @param newClips The clips
   */

  public void setClips(
    final List<AUClipDeclaration> newClips)
  {
    this.edit(() -> {
      this.clips.setAll(Objects.requireNonNull(newClips, "clips"));
    });
  }

  /**
   * @return The current identifier
   */

  public ReadOnlyObjectProperty<AUIdentifier> identifier()
  {
    return this.identifier;
  }

  /**
   * @return The clip list
   */

  public SortedList<AUClipDeclaration> clips()
  {
    return this.clipsSorted;
  }

  /**
   * @return The metadata list
   */

  public SortedList<AEMetadata> metadata()
  {
    return this.metasSorted;
  }

  /**
   * Set the model clip buffers.
   *
   * @param clipData The clip buffers
   */

  public void setClipBuffers(
    final Map<AUClipID, SampleBufferType> clipData)
  {
    this.edit(() -> {
      this.clipBuffers.clear();
      this.clipBuffers.putAll(Map.copyOf(clipData));
    });
  }

  private void edit(
    final Runnable runnable)
  {
    try {
      runnable.run();
    } finally {
      this.dirty();
    }
  }

  private void dirty()
  {
    this.isSaveRequired.set(true);
  }

  /**
   * @param id The clip ID
   *
   * @return A clip buffer
   */

  public SampleBufferType clipBuffer(
    final AUClipID id)
  {
    return this.clipBuffers.get(id);
  }

  /**
   * @param id The clip ID
   *
   * @return A raw clip buffer
   */

  public ByteBuffer clipRawBuffer(
    final AUClipID id)
  {
    return this.clipRawBuffers.get(id);
  }

  /**
   * Set the metadata
   *
   * @param metaList The metadata
   */

  public void setMeta(
    final List<AEMetadata> metaList)
  {
    this.edit(() -> {
      this.metas.setAll(List.copyOf(metaList));
    });
  }

  /**
   * Set the metadata filter.
   *
   * @param text The filter text
   */

  public void setMetadataFilter(
    final String text)
  {
    final var textClean =
      Objects.requireNonNullElse(text, "")
        .trim()
        .toUpperCase();

    if (textClean.isEmpty()) {
      this.metasFiltered.setPredicate(m -> true);
    } else {
      this.metasFiltered.setPredicate(meta -> {
        return meta.name().toUpperCase().contains(textClean)
               || meta.value().toUpperCase().contains(textClean);
      });
    }
  }

  /**
   * Set the clip filter.
   *
   * @param text The filter text
   */

  public void setClipFilter(
    final String text)
  {
    final var textClean =
      Objects.requireNonNullElse(text, "")
        .trim()
        .toUpperCase();

    if (textClean.isEmpty()) {
      this.clipsFiltered.setPredicate(m -> true);
    } else {
      this.clipsFiltered.setPredicate(clip -> {
        return clip.name().toUpperCase().contains(textClean);
      });
    }
  }

  /**
   * Set the identifier.
   *
   * @param newIdentifier The identifier
   */

  public void setIdentifier(
    final Optional<AUIdentifier> newIdentifier)
  {
    this.edit(() -> {
      this.identifier.set(newIdentifier.orElse(null));
    });
  }

  /**
   * Set the current model as being up-to-date and fully saved.
   */

  public void setSaved()
  {
    this.isSaveRequired.set(false);
  }

  /**
   * @return A property indicating if there is unsaved data
   */

  public ReadOnlyBooleanProperty isSaveRequired()
  {
    return this.isSaveRequired;
  }

  /**
   * Set the file.
   *
   * @param newFile The file
   */

  public void setFile(
    final Path newFile)
  {
    this.file.set(Optional.of(newFile));
  }

  /**
   * @return The current file
   */

  public ReadOnlyObjectProperty<Optional<Path>> file()
  {
    return this.file;
  }

  /**
   * Set the model clip buffers.
   *
   * @param clipRawData The clip buffers
   */

  public void setClipRawData(
    final Map<AUClipID, ByteBuffer> clipRawData)
  {
    this.edit(() -> {
      this.clipRawBuffers.clear();
      this.clipRawBuffers.putAll(Map.copyOf(clipRawData));
    });
  }

  /**
   * Set the key assignments.
   *
   * @param newKeyAssignments The key assignments
   */

  public void setKeyAssignments(
    final List<AUKeyAssignment> newKeyAssignments)
  {
    this.edit(() -> {
      this.keyAssignments.clear();
      this.keyAssignments.setAll(List.copyOf(newKeyAssignments));
    });
  }

  /**
   * @return The current key assignments
   */

  public List<AUKeyAssignment> keyAssignments()
  {
    return List.copyOf(this.keyAssignments);
  }

  /**
   * Add a metadata value.
   *
   * @param meta The value
   */

  public void metadataAdd(
    final AEMetadata meta)
  {
    this.edit(() -> {
      this.metas.add(meta);
    });
  }

  /**
   * Remove a metadata value.
   *
   * @param meta The value
   */

  public void metadataRemove(
    final AEMetadata meta)
  {
    this.edit(() -> {
      this.metas.remove(meta);
    });
  }
}
