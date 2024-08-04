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

import com.io7m.jaffirm.core.Preconditions;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A model to manage an undo/redo stack.
 */

public final class AEUndoModel
{
  private final ConcurrentLinkedDeque<AEControllerCommandType> undoStack;
  private final ConcurrentLinkedDeque<AEControllerCommandType> redoStack;
  private final SimpleObjectProperty<Optional<String>> undoState;
  private final SimpleObjectProperty<Optional<String>> redoState;

  /**
   * A model to manage an undo/redo stack.
   */

  public AEUndoModel()
  {
    this.undoStack =
      new ConcurrentLinkedDeque<>();
    this.redoStack =
      new ConcurrentLinkedDeque<>();

    this.undoState =
      new SimpleObjectProperty<>(Optional.empty());
    this.redoState =
      new SimpleObjectProperty<>(Optional.empty());
  }

  /**
   * Add a command to the undo stack.
   *
   * @param command The command
   */

  public void undoAdd(
    final AEControllerCommandType command)
  {
    Preconditions.checkPrecondition(
      command.isUndoable(),
      "Command must be undoable.");

    this.undoStack.push(command);
    Platform.runLater(() -> {
      this.undoState.set(Optional.of(command.describe()));
    });
  }

  /**
   * Pop a command from the undo stack.
   *
   * @return The command
   */

  public AEControllerCommandType undoPop()
  {
    final var command = this.undoStack.pop();
    Platform.runLater(() -> {
      this.undoState.set(
        Optional.ofNullable(this.undoStack.peek())
          .map(AEControllerCommandType::describe)
      );
    });
    return command;
  }

  /**
   * Add a command to the redo stack.
   *
   * @param command The command
   */

  public void redoAdd(
    final AEControllerCommandType command)
  {
    Preconditions.checkPrecondition(
      command.isUndoable(),
      "Command must be undoable.");

    this.redoStack.push(command);
    Platform.runLater(() -> {
      this.redoState.set(Optional.of(command.describe()));
    });
  }

  /**
   * Pop a command from the redo stack.
   *
   * @return The command
   */

  public AEControllerCommandType redoPop()
  {
    final var command = this.redoStack.pop();
    Platform.runLater(() -> {
      this.redoState.set(
        Optional.ofNullable(this.redoStack.peek())
          .map(AEControllerCommandType::describe)
      );
    });
    return command;
  }

  /**
   * Clear the undo/redo stack.
   */

  public void clear()
  {
    this.undoStack.clear();
    this.redoStack.clear();

    Platform.runLater(() -> this.undoState.set(Optional.empty()));
    Platform.runLater(() -> this.redoState.set(Optional.empty()));
  }

  /**
   * @return The name of the command at the top of the undo stack
   */

  public ReadOnlyObjectProperty<Optional<String>> undoState()
  {
    return this.undoState;
  }

  /**
   * @return The name of the command at the top of the redo stack
   */

  public ReadOnlyObjectProperty<Optional<String>> redoState()
  {
    return this.redoState;
  }
}
