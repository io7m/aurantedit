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

import com.io7m.aurantedit.ui.AEApplication;
import com.io7m.aurantedit.ui.internal.model.AEController;
import com.io7m.aurantedit.ui.internal.model.AEControllerEventFailed;
import com.io7m.aurantedit.ui.internal.model.AEControllerEventInProgress;
import com.io7m.aurantedit.ui.internal.model.AEControllerEventSucceeded;
import com.io7m.aurantedit.ui.internal.model.AEControllerEventType;
import com.io7m.aurantedit.ui.internal.model.AEControllerType;
import com.io7m.aurantedit.ui.internal.model.AEMetadata;
import com.io7m.aurantium.api.AUClipDeclaration;
import com.io7m.aurantium.api.AUIdentifier;
import com.io7m.brackish.core.WaveView;
import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.lanark.core.RDottedName;
import com.io7m.miscue.fx.seltzer.MSErrorDialogs;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.seltzer.api.SStructuredError;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;

import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_REDO;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_REDO_NAMED;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_TITLE_FILE_SAVED;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_TITLE_FILE_UNSAVED;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_UNDO;
import static com.io7m.aurantedit.ui.internal.AEStringConstants.AE_UNDO_NAMED;
import static com.io7m.aurantedit.ui.internal.AEUIThread.onUIThread;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * A file view.
 */

public final class AEFileView implements AEViewType,
  Flow.Subscriber<AEControllerEventType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEApplication.class);

  private final AEFileChoosersType choosers;
  private final AEStringsType strings;
  private final AEWaveModel waveModel;
  private final Stage stage;
  private final ChangeListener<Boolean> unsavedChangeListener;
  private final AEVersionSetDialogs versionSet;
  private final ChangeListener<Optional<String>> undoListener;
  private final ChangeListener<Optional<String>> redoListener;
  private final AESaveConfirmDialogs saveDialogs;
  private final RPServiceDirectoryType services;
  private final AEControllerType controller;
  private final AECreateDialogs createDialogs;
  private final AENameSetDialogs nameSet;
  private final AEMetadataEditDialogs metadataEditDialogs;

  @FXML private MenuItem undo;
  @FXML private MenuItem redo;

  @FXML private Button error;

  @FXML private TextField status;
  @FXML private ProgressBar progress;

  @FXML private TextField metaName;
  @FXML private TextField metaVersionMajor;
  @FXML private TextField metaVersionMinor;
  @FXML private TextField metaSearch;
  @FXML private Button metaAdd;
  @FXML private Button metaRemove;
  @FXML private TableView<AEMetadata> metaTable;
  @FXML private TableColumn<AEMetadata, String> metaTableName;
  @FXML private TableColumn<AEMetadata, String> metaTableValue;

  @FXML private Button clipAdd;
  @FXML private Button clipRemove;
  @FXML private Button clipReplace;
  @FXML private TextField clipSearch;
  @FXML private TextField fieldClipName;
  @FXML private TextField fieldClipID;
  @FXML private TextField fieldClipFormat;
  @FXML private TextField fieldClipEndianness;
  @FXML private TextField fieldClipHash;
  @FXML private TextField fieldClipSampleRate;
  @FXML private TextField fieldClipSampleDepth;
  @FXML private TextField fieldClipChannels;
  @FXML private TextField fieldClipSize;

  @FXML private AnchorPane waveCanvasHolder;

  @FXML private TabPane fileTabs;
  @FXML private Tab fileTabMetadata;
  @FXML private Tab fileTabClips;
  @FXML private Tab fileTabKeyAssignments;
  @FXML private MenuItem itemNew;
  @FXML private MenuItem itemOpen;
  @FXML private MenuItem itemSave;
  @FXML private MenuItem itemSaveAs;
  @FXML private MenuItem itemClose;
  @FXML private MenuItem itemExit;

  @FXML private TableView<AUClipDeclaration> clipTable;
  @FXML private TableColumn<AUClipDeclaration, String> clipTableID;
  @FXML private TableColumn<AUClipDeclaration, String> clipTableReferences;
  @FXML private TableColumn<AUClipDeclaration, String> clipTableName;

  private Set<TextField> fieldsClip;
  private WaveView waveView;
  private SStructuredError<String> errorLast;

  /**
   * A file view.
   *
   * @param inServices The service directory
   * @param inStage    The stage
   */

  public AEFileView(
    final RPServiceDirectoryType inServices,
    final Stage inStage)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.stage =
      Objects.requireNonNull(inStage, "stage");
    this.choosers =
      inServices.requireService(AEFileChoosersType.class);
    this.strings =
      inServices.requireService(AEStringsType.class);
    this.versionSet =
      inServices.requireService(AEVersionSetDialogs.class);
    this.nameSet =
      inServices.requireService(AENameSetDialogs.class);
    this.saveDialogs =
      inServices.requireService(AESaveConfirmDialogs.class);
    this.createDialogs =
      inServices.requireService(AECreateDialogs.class);
    this.metadataEditDialogs =
      inServices.requireService(AEMetadataEditDialogs.class);

    this.controller =
      AEController.empty(this.strings);
    this.waveModel =
      new AEWaveModel();

    this.undoListener =
      (observable, oldValue, newValue) -> {
        this.undo.setDisable(newValue.isEmpty());

        if (newValue.isPresent()) {
          this.undo.setText(this.strings.format(AE_UNDO_NAMED, newValue.get()));
        } else {
          this.undo.setText(this.strings.format(AE_UNDO));
        }
      };

    this.redoListener =
      (observable, oldValue, newValue) -> {
        this.redo.setDisable(newValue.isEmpty());

        if (newValue.isPresent()) {
          this.redo.setText(this.strings.format(AE_REDO_NAMED, newValue.get()));
        } else {
          this.redo.setText(this.strings.format(AE_REDO));
        }
      };

    this.unsavedChangeListener =
      (observable, oldValue, newValue) -> {
        this.itemSave.setDisable(newValue != TRUE);
        this.setWindowTitle();
      };
  }

  /**
   * Open a new file view for the given stage.
   *
   * @param services The service directory
   * @param stage    The stage
   *
   * @return A view and stage
   *
   * @throws Exception On errors
   */

  public static AEViewAndStage<AEFileView> openForStage(
    final RPServiceDirectoryType services,
    final Stage stage)
    throws Exception
  {
    final var strings =
      services.requireService(AEStringsType.class);

    final var xml =
      AEFileView.class.getResource(
        "/com/io7m/aurantedit/ui/internal/main.fxml"
      );
    final var resources =
      strings.resources();
    final var loader =
      new FXMLLoader(xml, resources);

    final AEControllerFactoryType<AEViewType> controllers =
      AEControllerFactoryMapped.create(
        Map.entry(
          AEFileView.class,
          () -> new AEFileView(services, stage)
        )
      );

    loader.setControllerFactory(param -> {
      return controllers.call((Class<? extends AEViewType>) param);
    });

    final var pane = loader.<Pane>load();
    AECSS.setCSS(pane);
    stage.setScene(new Scene(pane));
    stage.setTitle(strings.format(AEStringConstants.AE_TITLE));
    return new AEViewAndStage<>(loader.getController(), stage);
  }

  private void setWindowTitle()
  {
    if (this.controller.isSaveRequired().get()) {
      this.stage.setTitle(
        this.strings.format(
          AE_TITLE_FILE_UNSAVED,
          this.controller.file().getValue().orElse(null)
        )
      );
      return;
    }

    this.stage.setTitle(
      this.strings.format(
        AE_TITLE_FILE_SAVED,
        this.controller.file().getValue().orElse(null)
      )
    );
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.fieldsClip = Set.of(
      this.fieldClipChannels,
      this.fieldClipEndianness,
      this.fieldClipFormat,
      this.fieldClipHash,
      this.fieldClipID,
      this.fieldClipName,
      this.fieldClipSampleDepth,
      this.fieldClipSampleRate,
      this.fieldClipSize
    );

    this.status.setText(AEVersionStrings.VERSION);

    this.waveView = new WaveView();
    this.waveView.setWaveModel(this.waveModel);
    this.waveCanvasHolder.getChildren().add(this.waveView);

    AnchorPane.setBottomAnchor(this.waveView, 0.0);
    AnchorPane.setLeftAnchor(this.waveView, 0.0);
    AnchorPane.setRightAnchor(this.waveView, 0.0);
    AnchorPane.setTopAnchor(this.waveView, 0.0);

    this.fileTabs.setDisable(true);
    this.fileTabs.setVisible(false);

    this.itemSave.setDisable(true);
    this.itemSaveAs.setDisable(true);
    this.itemClose.setDisable(true);

    this.initializeMetaTable();
    this.initializeClipTable();

    this.controller.file()
      .addListener((observable, oldValue, newValue) -> {
        if (newValue.isPresent()) {
          this.fileTabs.setDisable(false);
          this.fileTabs.setVisible(true);
          this.itemSaveAs.setDisable(false);
          this.setWindowTitle();
        } else {
          this.fileTabs.setDisable(true);
          this.fileTabs.setVisible(false);
          this.itemSaveAs.setDisable(true);
          this.setWindowTitle();
        }
      });

    this.controller.isSaveRequired()
      .addListener(this.unsavedChangeListener);
    this.controller.undoState()
      .addListener(this.undoListener);
    this.controller.redoState()
      .addListener(this.redoListener);

    this.metaName.textProperty()
      .bind(
        this.controller.identifier()
          .map(AUIdentifier::name)
          .map(RDottedName::value)
      );

    this.controller.identifier()
      .addListener((___, oldId, newId) -> {
        this.metaVersionMajor.setText(
          Long.toUnsignedString(
            Integer.toUnsignedLong(newId.version().major())
          )
        );
        this.metaVersionMinor.setText(
          Long.toUnsignedString(
            Integer.toUnsignedLong(newId.version().minor())
          )
        );
      });

    this.controller.events().subscribe(this);

    this.error.setDisable(true);
    this.error.setVisible(false);
  }

  private void initializeMetaTable()
  {
    this.metaTableName.setSortable(true);
    this.metaTableName.setReorderable(false);
    this.metaTableName.setCellValueFactory(param -> {
      return new ReadOnlyStringWrapper(param.getValue().name());
    });

    this.metaTableValue.setSortable(true);
    this.metaTableValue.setReorderable(false);
    this.metaTableValue.setCellValueFactory(param -> {
      return new ReadOnlyStringWrapper(param.getValue().value());
    });

    this.metaTable.setPlaceholder(new Label());
    this.metaTable.getSelectionModel()
      .selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.metaRemove.setDisable(newValue == null);
      });

    this.metaSearch.textProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.controller.setMetadataFilter(newValue);
      });

    final var metadata = this.controller.metadata();
    this.metaTable.setItems(metadata);
    metadata.comparatorProperty()
      .bind(this.metaTable.comparatorProperty());
  }

  private void initializeClipTable()
  {
    this.clipTableID.setSortable(true);
    this.clipTableID.setReorderable(false);
    this.clipTableID.setCellValueFactory(param -> {
      return new ReadOnlyStringWrapper(param.getValue().id().toString());
    });

    this.clipTableReferences.setSortable(true);
    this.clipTableReferences.setReorderable(false);
    this.clipTableReferences.setCellValueFactory(param -> {
      return new ReadOnlyStringWrapper("0");
    });

    this.clipTableName.setSortable(true);
    this.clipTableName.setReorderable(false);
    this.clipTableName.setCellValueFactory(param -> {
      return new ReadOnlyStringWrapper(param.getValue().name());
    });

    this.clipTable.setPlaceholder(new Label());
    this.clipTable.getSelectionModel()
      .selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.onClipSelectionChanged(oldValue, newValue);
      });
    this.clipTable.getSelectionModel()
      .selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.clipReplace.setDisable(newValue == null);
        this.clipRemove.setDisable(newValue == null);
      });

    this.clipSearch.textProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.controller.setClipFilter(newValue);
      });

    final var clips = this.controller.clips();
    this.clipTable.setItems(clips);
    clips.comparatorProperty()
      .bind(this.clipTable.comparatorProperty());
  }

  private void onClipSelectionChanged(
    final AUClipDeclaration oldValue,
    final AUClipDeclaration newValue)
  {
    if (newValue == null) {
      for (final var field : this.fieldsClip) {
        field.setText("");
      }
      this.waveModel.setWave(Optional.empty());
      this.waveView.setWaveModel(this.waveModel);
      this.waveView.redraw();
      return;
    }

    this.fieldClipID
      .setText(newValue.id().toString());
    this.fieldClipChannels
      .setText(Long.toUnsignedString(newValue.channels()));
    this.fieldClipEndianness
      .setText(newValue.endianness().name());
    this.fieldClipFormat
      .setText(newValue.format().descriptor().value());
    this.fieldClipHash
      .setText(
        String.format(
          "%s: %s",
          newValue.hash().algorithm().name(),
          newValue.hash().value()
        )
      );
    this.fieldClipName
      .setText(newValue.name());
    this.fieldClipSampleDepth
      .setText(Long.toUnsignedString(newValue.sampleDepth()));
    this.fieldClipSampleRate
      .setText(Long.toUnsignedString(newValue.sampleRate()));
    this.fieldClipSize
      .setText(Long.toUnsignedString(newValue.size()));

    final var buffer = this.controller.clipBuffer(newValue.id());
    this.waveModel.setWave(Optional.ofNullable(buffer));
    this.waveView.setViewRange(0L, buffer.frames());
    this.waveView.setWaveModel(this.waveModel);
    this.waveView.redraw();
  }

  private AEUnit logException(
    final Throwable throwable)
  {
    if (throwable instanceof final CompletionException e) {
      if (e.getCause() instanceof CancellationException) {
        return AEUnit.UNIT;
      }
    }

    LOG.debug("Error: ", throwable);
    return AEUnit.UNIT;
  }

  private CompletableFuture<AEUnit> tryNew()
  {
    return onUIThread(this::tryNewActual);
  }

  private AEUnit tryNewActual()
    throws Exception
  {
    final var createDialog =
      this.createDialogs.createDialog(AEUnit.UNIT);

    createDialog.stage().showAndWait();

    final var chosenOpt = createDialog.view().result();
    if (chosenOpt.isEmpty()) {
      return null;
    }

    final var chosen =
      chosenOpt.get();
    final var existingFileOpt =
      this.controller.file().getValue();

    if (existingFileOpt.isEmpty()) {
      this.controller.create(chosen.file(), chosen.identifier());
    } else {
      final var viewAndStage =
        AEFileView.openForStage(this.services, new Stage());
      viewAndStage.view().controller.create(chosen.file(), chosen.identifier());
      viewAndStage.stage().show();
    }
    return AEUnit.UNIT;
  }

  private CompletableFuture<AEUnit> trySave()
  {
    return onUIThread(() -> {
      final var viewAndStage =
        this.saveDialogs.createDialog(AEUnit.UNIT);

      viewAndStage.stage().showAndWait();
      return switch (viewAndStage.view().result()) {
        case SAVE -> {
          yield AEUnit.UNIT;
        }
        case DISCARD -> {
          yield AEUnit.UNIT;
        }
        case CANCEL -> {
          throw new CancellationException();
        }
      };
    });
  }

  private CompletableFuture<Boolean> isSaveRequired()
  {
    return CompletableFuture.completedFuture(
      Optional.ofNullable(this.controller)
        .map(AEControllerType::isSaveRequired)
        .map(ObservableBooleanValue::get)
        .orElse(FALSE)
    );
  }

  private AEUnit tryOpenActual()
    throws Exception
  {
    final var chooser =
      this.choosers.create(
        JWFileChooserConfiguration.builder()
          .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
          .build()
      );

    final var chosen = chooser.showAndWait();
    if (chosen.isEmpty()) {
      return AEUnit.UNIT;
    }

    final var file =
      chosen.get(0);
    final var existingFileOpt =
      this.controller.file().getValue();

    if (existingFileOpt.isEmpty()) {
      this.controller.open(file);
    } else {
      final var viewAndStage =
        AEFileView.openForStage(this.services, new Stage());
      viewAndStage.view().controller.open(file);
      viewAndStage.stage().show();
    }

    return AEUnit.UNIT;
  }

  private CompletableFuture<AEUnit> tryOpen()
  {
    return onUIThread(this::tryOpenActual);
  }

  private CompletableFuture<AEUnit> tryClose()
  {
    return CompletableFuture.failedFuture(new CancellationException());
  }

  private CompletableFuture<AEUnit> tryExit()
  {
    return CompletableFuture.failedFuture(new CancellationException());
  }

  @FXML
  private void onNewSelected()
  {
    this.tryNew();
  }

  @FXML
  private void onOpenSelected()
  {
    this.tryOpen();
  }

  @FXML
  private void onSaveSelected()
  {
    this.controller.save();
  }

  @FXML
  private void onSaveAsSelected()
  {
    final var chooser =
      this.choosers.create(
        JWFileChooserConfiguration.builder()
          .setAction(JWFileChooserAction.CREATE)
          .setConfirmFileSelection(true)
          .build()
      );

    final var chosen = chooser.showAndWait();
    if (chosen.isEmpty()) {
      return;
    }

    this.controller.saveAs(chosen.get(0));
  }

  @FXML
  private void onCloseSelected()
  {
    this.isSaveRequired()
      .thenCompose(saveRequired -> {
        if (saveRequired) {
          return this.trySave().thenCompose(__ -> this.tryClose());
        } else {
          return this.tryClose();
        }
      }).exceptionally(this::logException);
  }

  @FXML
  private void onExitSelected()
  {
    this.isSaveRequired()
      .thenCompose(saveRequired -> {
        if (saveRequired) {
          return this.trySave().thenCompose(__ -> this.tryExit());
        } else {
          return this.tryExit();
        }
      }).exceptionally(this::logException);
  }

  @FXML
  private void onClipAddSelected()
  {
    final var chooser =
      this.choosers.create(
        JWFileChooserConfiguration.builder()
          .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
          .build()
      );

    final var chosen = chooser.showAndWait();
    if (chosen.isEmpty()) {
      return;
    }

    this.controller.clipAdd(chosen.get(0));
  }

  @FXML
  private void onClipRemoveSelected()
  {

  }

  @FXML
  private void onClipReplaceSelected()
  {

  }

  @FXML
  private void onMetaAddSelected()
    throws Exception
  {
    final var viewAndStage =
      this.metadataEditDialogs.createDialog(AEUnit.UNIT);

    final var view = viewAndStage.view();
    viewAndStage.stage().showAndWait();
    view.result().ifPresent(this.controller::metadataAdd);
  }

  @FXML
  private void onMetaRemoveSelected()
  {
    this.controller.metadataRemove(
      this.metaTable.getSelectionModel()
        .getSelectedItem()
    );
  }

  @FXML
  private void onMetaReplaceSelected()
  {

  }

  @FXML
  private void onNameSetSelected()
    throws Exception
  {
    final var viewAndStage =
      this.nameSet.createDialog(AEUnit.UNIT);

    final var view = viewAndStage.view();
    view.setNameInitial(
      this.controller.identifier()
        .get()
        .name()
    );

    viewAndStage.stage().showAndWait();
    view.result().ifPresent(this.controller::setName);
  }

  @FXML
  private void onVersionSetSelected()
    throws Exception
  {
    final var viewAndStage =
      this.versionSet.createDialog(AEUnit.UNIT);

    final var view = viewAndStage.view();
    view.setVersionInitial(
      this.controller.identifier()
        .get()
        .version()
    );

    viewAndStage.stage().showAndWait();
    view.result().ifPresent(this.controller::setVersion);
  }

  @FXML
  private void onUndoSelected()
  {
    this.controller.undo();
  }

  @FXML
  private void onRedoSelected()
  {
    this.controller.redo();
  }

  @Override
  public void onSubscribe(
    final Flow.Subscription subscription)
  {
    subscription.request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(
    final AEControllerEventType item)
  {
    Platform.runLater(() -> {
      switch (item) {
        case final AEControllerEventFailed failed -> {
          this.progress.setProgress(0.0);
          this.status.setText(failed.message());
          this.error.setDisable(false);
          this.error.setVisible(true);
          this.errorLast = failed.error();
        }
        case final AEControllerEventInProgress inProgress -> {
          this.progress.setProgress(inProgress.progress());
          this.status.setText(inProgress.message());
          this.error.setDisable(true);
          this.error.setVisible(false);
        }
        case final AEControllerEventSucceeded success -> {
          this.progress.setProgress(0.0);
          this.status.setText(success.message());
          this.error.setDisable(true);
          this.error.setVisible(false);
        }
      }
    });
  }

  @Override
  public void onError(
    final Throwable throwable)
  {
    LOG.error("Error: ", throwable);
  }

  @Override
  public void onComplete()
  {
    Platform.runLater(() -> {
      this.progress.setProgress(0.0);
      this.status.setText("");
    });
  }

  @FXML
  private void onErrorDetailsSelected()
  {
    final var dialog =
      MSErrorDialogs.builder(this.errorLast)
        .setCSS(AECSS.defaultCSS())
        .setTitle(this.errorLast.message())
        .build();

    dialog.showAndWait();
  }

  @FXML
  private void onAboutSelected()
    throws IOException
  {
    final var newStage = new Stage();

    final var xml =
      AEFileView.class.getResource(
        "/com/io7m/aurantedit/ui/internal/about.fxml"
      );
    final var resources =
      this.strings.resources();
    final var loader =
      new FXMLLoader(xml, resources);

    loader.setControllerFactory(clazz -> new AEAboutView());

    final Pane pane = loader.load();
    AECSS.setCSS(pane);

    loader.getController();
    newStage.setScene(new Scene(pane));
    newStage.setResizable(false);
    newStage.setTitle("Aurantedit");
    newStage.show();
  }
}
