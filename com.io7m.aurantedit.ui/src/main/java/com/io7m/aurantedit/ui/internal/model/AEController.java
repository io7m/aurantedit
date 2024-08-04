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

import com.io7m.aurantedit.ui.internal.AEStringsType;
import com.io7m.aurantedit.ui.internal.AEUnit;
import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.aurantium.api.AUVersion;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.lanark.core.RDottedName;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.transformation.SortedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_EVENT_COMMAND_IN_PROGRESS;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_EVENT_COMMAND_SUCCEEDED;
import static com.io7m.aurantedit.ui.internal.AEUnit.UNIT;
import static com.io7m.aurantedit.ui.internal.model.AEUndoRedo.CLEAR_ALL;
import static com.io7m.aurantedit.ui.internal.model.AEUndoRedo.REDO;
import static com.io7m.aurantedit.ui.internal.model.AEUndoRedo.UNDO;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A controller.
 */

public final class AEController
  implements AEControllerType, AEControllerCommandContextType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEController.class);

  private final ExecutorService executor;
  private final LinkedBlockingQueue<AEQueueSubmission> executorCommandQueue;
  private final AEStringsType strings;
  private final AtomicBoolean closed;
  private final AEModelState state;
  private final SubmissionPublisher<AEControllerEventType> events;
  private final CloseableCollectionType<IllegalStateException> resources;
  private final ConcurrentHashMap<String, String> attributes;
  private final AEUndoModel undoModel;

  private record AEQueueSubmission(
    AEControllerExecutableType executable,
    CompletableFuture<AEUnit> future)
  {
    AEQueueSubmission
    {
      Objects.requireNonNull(executable, "executable");
      Objects.requireNonNull(future, "future");
    }
  }

  private AEController(
    final AEStringsType inStrings)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "strings");

    this.events =
      new SubmissionPublisher<>();
    this.attributes =
      new ConcurrentHashMap<>();

    this.state =
      new AEModelState();
    this.executor =
      Executors.newSingleThreadExecutor(
        Thread.ofVirtual()
          .name("com.io7m.aurantedit.model-", 0L)
          .factory()
      );
    this.executorCommandQueue =
      new LinkedBlockingQueue<>();
    this.closed =
      new AtomicBoolean(false);

    this.undoModel =
      new AEUndoModel();

    this.resources =
      CloseableCollection.create(() -> {
        return new IllegalStateException("Resource exception.");
      });

    this.resources.add(this.executor);
    this.resources.add(this.events);
  }

  /**
   * Create a new controller.
   *
   * @param strings The strings
   *
   * @return A controller
   */

  public static AEControllerType empty(
    final AEStringsType strings)
  {
    final var controller = new AEController(strings);
    controller.executor.execute(controller::processCommands);
    return controller;
  }

  private void processCommands()
  {
    while (!this.closed.get()) {
      try {
        final var submission =
          this.executorCommandQueue.poll(1L, SECONDS);

        if (submission == null) {
          continue;
        }

        this.attributes.clear();

        switch (submission.executable()) {
          case final AEControllerCommandType command -> {
            try {
              this.events.submit(this.eventCommandInProgress(command));
              command.execute(this);
              if (command.isUndoable()) {
                this.undoModel.undoAdd(command);
              }

              submission.future.complete(UNIT);
              this.events.submit(this.eventCommandSucceeded(command));
            } catch (final Throwable e) {
              LOG.error("Error: ", e);
              submission.future.completeExceptionally(e);
              this.events.submit(
                AEControllerEventFailed.ofException(
                  Map.copyOf(this.attributes),
                  e
                )
              );
            }
          }

          case UNDO -> {
            try {
              final var command = this.undoModel.undoPop();
              this.events.submit(this.eventCommandInProgress(command));
              command.undo(this);
              this.undoModel.redoAdd(command);

              submission.future.complete(UNIT);
              this.events.submit(this.eventCommandSucceeded(command));
            } catch (final Throwable e) {
              LOG.error("Error: ", e);
              submission.future.completeExceptionally(e);
              this.events.submit(
                AEControllerEventFailed.ofException(
                  Map.copyOf(this.attributes),
                  e
                )
              );
            }
          }

          case REDO -> {
            try {
              final var command = this.undoModel.redoPop();
              this.events.submit(this.eventCommandInProgress(command));
              command.execute(this);
              this.undoModel.undoAdd(command);

              submission.future.complete(UNIT);
              this.events.submit(this.eventCommandSucceeded(command));
            } catch (final Throwable e) {
              LOG.error("Error: ", e);
              submission.future.completeExceptionally(e);
              this.events.submit(
                AEControllerEventFailed.ofException(
                  Map.copyOf(this.attributes),
                  e
                )
              );
            }
          }

          case CLEAR_ALL -> {
            try {
              this.undoModel.clear();
              submission.future.complete(UNIT);
            } catch (final Throwable e) {
              LOG.error("Error: ", e);
              submission.future.completeExceptionally(e);
              this.events.submit(
                AEControllerEventFailed.ofException(
                  Map.copyOf(this.attributes),
                  e
                )
              );
            }
          }
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private AEControllerEventType eventCommandInProgress(
    final AEControllerCommandType command)
  {
    return new AEControllerEventSucceeded(
      this.strings.format(AE_EVENT_COMMAND_IN_PROGRESS, command.describe())
    );
  }

  private AEControllerEventType eventCommandSucceeded(
    final AEControllerCommandType command)
  {
    return new AEControllerEventSucceeded(
      this.strings.format(AE_EVENT_COMMAND_SUCCEEDED, command.describe())
    );
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      try {
        this.enqueue(new AEControllerCommandClose(this.state))
          .get(1L, SECONDS);
      } catch (final Exception e) {
        LOG.debug("Failed to close: ", e);
      }
      this.resources.close();
    }
  }

  @Override
  public Flow.Publisher<AEControllerEventType> events()
  {
    return this.events;
  }

  @Override
  public ReadOnlyObjectProperty<Optional<Path>> file()
  {
    return this.state.file();
  }

  @Override
  public SortedList<AEMetadata> metadata()
  {
    return this.state.metadata();
  }

  @Override
  public SortedList<AUClipDeclaration> clips()
  {
    return this.state.clips();
  }

  @Override
  public SampleBufferType clipBuffer(
    final AUClipID id)
  {
    return this.state.clipBuffer(id);
  }

  @Override
  public ReadOnlyObjectProperty<AUIdentifier> identifier()
  {
    return this.state.identifier();
  }

  @Override
  public void setMetadataFilter(
    final String text)
  {
    this.state.setMetadataFilter(text);
  }

  @Override
  public void setClipFilter(
    final String text)
  {
    this.state.setClipFilter(text);
  }

  @Override
  public ReadOnlyBooleanProperty isSaveRequired()
  {
    return this.state.isSaveRequired();
  }

  @Override
  public CompletableFuture<AEUnit> setVersion(
    final AUVersion version)
  {
    Objects.requireNonNull(version, "version");

    return this.enqueue(
      new AEControllerCommandSetVersion(this.state, version)
    );
  }

  @Override
  public CompletableFuture<AEUnit> setName(
    final RDottedName name)
  {
    Objects.requireNonNull(name, "name");

    return this.enqueue(
      new AEControllerCommandSetName(this.state, name)
    );
  }

  @Override
  public ReadOnlyObjectProperty<Optional<String>> undoState()
  {
    return this.undoModel.undoState();
  }

  @Override
  public ReadOnlyObjectProperty<Optional<String>> redoState()
  {
    return this.undoModel.redoState();
  }

  @Override
  public CompletableFuture<AEUnit> undo()
  {
    return this.enqueue(UNDO);
  }

  @Override
  public CompletableFuture<AEUnit> redo()
  {
    return this.enqueue(REDO);
  }

  @Override
  public CompletableFuture<AEUnit> clearUndoStack()
  {
    return this.enqueue(CLEAR_ALL);
  }

  @Override
  public CompletableFuture<AEUnit> create(
    final Path file,
    final AUIdentifier identifier)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(identifier, "identifier");

    return this.enqueue(
      new AEControllerCommandCreate(this.state, identifier, file)
    );
  }

  @Override
  public CompletableFuture<AEUnit> open(
    final Path file)
  {
    Objects.requireNonNull(file, "file");

    return this.enqueue(
      new AEControllerCommandOpen(this.state, file)
    );
  }

  @Override
  public CompletableFuture<AEUnit> save()
  {
    final var outputFile =
      this.file().get().orElseThrow();
    final var outputFileTemp =
      outputFile.resolveSibling(".aurantedit_" + UUID.randomUUID());

    return this.enqueue(
      new AEControllerCommandSave(
        this.state,
        outputFile,
        outputFileTemp
      )
    );
  }

  @Override
  public CompletableFuture<AEUnit> saveAs(
    final Path path)
  {
    Objects.requireNonNull(path, "path");

    final var outputFileTemp =
      path.resolveSibling(".aurantedit_" + UUID.randomUUID());

    return this.enqueue(
      new AEControllerCommandSaveAs(
        this.state,
        path,
        outputFileTemp
      )
    );
  }

  @Override
  public CompletableFuture<AEUnit> metadataAdd(
    final AEMetadata meta)
  {
    Objects.requireNonNull(meta, "meta");

    return this.enqueue(
      new AEControllerCommandMetadataAdd(this.state, meta)
    );
  }

  @Override
  public CompletableFuture<AEUnit> metadataRemove(
    final AEMetadata meta)
  {
    Objects.requireNonNull(meta, "meta");

    return this.enqueue(
      new AEControllerCommandMetadataRemove(this.state, meta)
    );
  }

  @Override
  public CompletableFuture<AEUnit> metadataReplace(
    final AEMetadata meta)
  {
    Objects.requireNonNull(meta, "meta");

    return this.enqueue(
      new AEControllerCommandMetadataReplace(this.state, meta)
    );
  }

  @Override
  public CompletableFuture<AEUnit> clipAdd(
    final Path path)
  {
    Objects.requireNonNull(path, "path");

    return this.enqueue(
      new AEControllerCommandClipAdd(this.state, path)
    );
  }

  @Override
  public CompletableFuture<AEUnit> clipReplace(
    final AUClipID clipID,
    final Path path)
  {
    Objects.requireNonNull(clipID, "clipID");
    Objects.requireNonNull(path, "path");

    return this.enqueue(
      new AEControllerCommandClipReplace(this.state, clipID, path)
    );
  }

  @Override
  public CompletableFuture<AEUnit> clipDelete(
    final AUClipID clipID)
  {
    Objects.requireNonNull(clipID, "clipID");

    return this.enqueue(
      new AEControllerCommandClipDelete(this.state, clipID)
    );
  }

  @Override
  public void putAttribute(
    final String name,
    final Object value)
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");

    this.attributes.put(name, value.toString());
  }

  @Override
  public void eventInProgress(
    final String message,
    final double progress)
  {
    this.events.submit(new AEControllerEventInProgress(message, progress));
  }

  @Override
  public void eventInProgress(
    final String message,
    final long index,
    final long count)
  {
    if (count == 0L) {
      this.events.submit(
        new AEControllerEventInProgress(message, 0.0)
      );
      return;
    }

    this.events.submit(
      new AEControllerEventInProgress(message, (double) index / (double) count)
    );
  }

  private CompletableFuture<AEUnit> enqueue(
    final AEControllerExecutableType executable)
  {
    final var future = new CompletableFuture<AEUnit>();
    this.executorCommandQueue.add(new AEQueueSubmission(executable, future));
    return future;
  }
}
