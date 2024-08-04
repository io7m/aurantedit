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


package com.io7m.aurantedit.ui;

import com.io7m.aurantedit.ui.internal.AECreateDialogs;
import com.io7m.aurantedit.ui.internal.AEFileChoosers;
import com.io7m.aurantedit.ui.internal.AEFileChoosersType;
import com.io7m.aurantedit.ui.internal.AEFileView;
import com.io7m.aurantedit.ui.internal.AEMetadataEditDialogs;
import com.io7m.aurantedit.ui.internal.AENameSetDialogs;
import com.io7m.aurantedit.ui.internal.AESaveConfirmDialogs;
import com.io7m.aurantedit.ui.internal.AEStrings;
import com.io7m.aurantedit.ui.internal.AEStringsType;
import com.io7m.aurantedit.ui.internal.AEVersionSetDialogs;
import com.io7m.aurantedit.ui.internal.database.AEDatabaseConfiguration;
import com.io7m.aurantedit.ui.internal.database.AEDatabaseFactory;
import com.io7m.aurantedit.ui.internal.database.AEDatabaseType;
import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.jade.api.ApplicationDirectoriesType;
import com.io7m.repetoir.core.RPServiceDirectory;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;

/**
 * The main application class responsible for starting up the "main" view.
 */

public final class AEApplication extends Application
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AEApplication.class);

  private final ApplicationDirectoriesType directories;

  /**
   * The main application class responsible for starting up the "main" view.
   *
   * @param inConfiguration The configuration
   */

  public AEApplication(
    final ApplicationDirectoriesType inConfiguration)
  {
    this.directories =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public void start(
    final Stage stage)
    throws Exception
  {
    final var strings =
      new AEStrings(Locale.getDefault());
    final var services =
      new RPServiceDirectory();

    Files.createDirectories(this.directories.configurationDirectory());

    final var database =
      new AEDatabaseFactory()
        .open(
          new AEDatabaseConfiguration(
            DDatabaseTelemetryNoOp.get(),
            DDatabaseCreate.CREATE_DATABASE,
            DDatabaseUpgrade.UPGRADE_DATABASE,
            this.directories.configurationDirectory()
              .resolve("database.db")
          ),
          event -> {

          }
        );
    services.register(
      AEDatabaseType.class, database);
    services.register(
      AEFileChoosersType.class, new AEFileChoosers(services));
    services.register(
      AEStringsType.class, strings);
    services.register(
      AEVersionSetDialogs.class, new AEVersionSetDialogs(services));
    services.register(
      AENameSetDialogs.class, new AENameSetDialogs(services));
    services.register(
      AESaveConfirmDialogs.class, new AESaveConfirmDialogs(services));
    services.register(
      AECreateDialogs.class, new AECreateDialogs(services));
    services.register(
      AEMetadataEditDialogs.class, new AEMetadataEditDialogs(services));

    final var viewAndStage =
      AEFileView.openForStage(services, stage);

    viewAndStage.stage().show();
  }
}
