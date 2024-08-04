/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.repetoir.core.RPServiceDirectoryType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * A convenient abstract dialog factory.
 *
 * @param <A> The type of dialog arguments
 * @param <C> The type of dialog controllers
 */

public abstract class AEDialogFactoryAbstract<A, C>
  implements AEDialogFactoryType<A, C>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEDialogFactoryAbstract.class);

  private final RPServiceDirectoryType services;
  private final Class<C> controllerClass;
  private final String fxmlResource;
  private final AEStringsType strings;
  private final Modality modality;

  protected AEDialogFactoryAbstract(
    final Class<C> inControllerClass,
    final String inFXMLResource,
    final RPServiceDirectoryType inMainServices,
    final Modality inModality)
  {
    this.controllerClass =
      Objects.requireNonNull(inControllerClass, "inControllerClass");
    this.fxmlResource =
      Objects.requireNonNull(inFXMLResource, "inFXMLResource");
    this.services =
      Objects.requireNonNull(inMainServices, "inMainServices");
    this.strings =
      inMainServices.requireService(AEStringsType.class);
    this.modality =
      Objects.requireNonNull(inModality, "modality");
  }

  protected final RPServiceDirectoryType services()
  {
    return this.services;
  }

  @Override
  public final URL fxmlResource()
  {
    return this.controllerClass.getResource(this.fxmlResource);
  }

  protected final AEStringsType strings()
  {
    return this.strings;
  }

  protected abstract String createStageTitle(
    A arguments
  );

  protected abstract AEControllerFactoryType<AEViewType> controllerFactory(
    A arguments,
    Stage stage);

  @Override
  public final C createDialogForStage(
    final A arguments,
    final Stage stage)
    throws IOException
  {
    Objects.requireNonNull(arguments, "arguments");
    Objects.requireNonNull(stage, "stage");

    final var xml =
      this.fxmlResource();
    final var resources =
      this.strings.resources();
    final var loader =
      new FXMLLoader(xml, resources);

    final var controllerFactory =
      this.controllerFactory(arguments, stage);

    loader.setControllerFactory(param -> {
      return controllerFactory.call((Class<? extends AEViewType>) param);
    });

    final Pane pane = loader.load();
    AECSS.setCSS(pane);

    final C controller = loader.getController();
    stage.setScene(new Scene(pane));
    stage.setTitle(this.createStageTitle(arguments));
    return controller;
  }

  @Override
  public final C openDialogAndWait(
    final A arguments)
    throws IOException
  {
    Objects.requireNonNull(arguments, "arguments");

    final var result = this.createDialog(arguments);
    result.stage().showAndWait();
    return result.view();
  }

  @Override
  public final AEViewAndStage<C> createDialog(
    final A arguments)
    throws IOException
  {
    Objects.requireNonNull(arguments, "arguments");

    final var stage = new Stage();
    final var controller =
      this.createDialogForStage(arguments, stage);
    stage.initModality(this.modality);
    return new AEViewAndStage<>(controller, stage);
  }
}
