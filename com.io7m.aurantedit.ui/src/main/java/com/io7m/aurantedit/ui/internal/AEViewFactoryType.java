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

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * The type of view controller factories.
 *
 * @param <A> The type of arguments required to instantiate view controllers
 * @param <C> The type of underlying controller
 */

public interface AEViewFactoryType<A, C>
{
  /**
   * Create a view controller.
   *
   * @param arguments The arguments used to instantiate the view controller
   *
   * @return A view controller added to the given stage
   *
   * @throws IOException On I/O errors
   */

  AEViewAndStage<C> createViewController(
    A arguments)
    throws IOException;

  /**
   * Create a view controller.
   *
   * @param arguments The arguments used to instantiate the view controller
   * @param stage     The stage
   *
   * @return A view controller
   *
   * @throws IOException On I/O errors
   */

  C createViewControllerForStage(
    A arguments,
    Stage stage)
    throws IOException;

  /**
   * @return The FXML layout used for this type of view controller
   */

  URL fxmlResource();
}
