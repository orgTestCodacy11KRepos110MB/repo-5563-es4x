/*
 * Copyright 2018 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.reactiverse.es4x.jul;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.*;

import static java.util.logging.Level.*;

public class ANSIFormatter extends Formatter {

  // are ANSI colors allowed?
  private static final boolean colors;

  static {
    if (Boolean.getBoolean("noTTY")) {
      // in this case rely on the system property to DISABLE the colors.
      colors = false;
    } else {
      String term = System.getenv("TERM");
      if (term != null) {
        term = term.toLowerCase();
        colors =
          // this is where the most common config will be on unices
          term.equals("xterm-color")
            // however as there are lots of terminal emulators, it seems
            // safer to look up for the suffix "-256color" as it covers:
            // vte, linux, tmux, screen, putty, rxvt, nsterm, ...
            || term.endsWith("-256color");
      } else {
        // there's no env variable (we're running either embedded (no shell)
        // or on a OS that doesn't set the TERM variable (Windows maybe)
        colors = false;
      }
    }
  }

  @Override
  public synchronized String format(LogRecord record) {

    Throwable thrown = record.getThrown();
    String message = record.getMessage();

    String thrownMessage = null;
    String thrownTrace = null;

    if (thrown != null) {
      // collect the trace back to a string
      try (StringWriter sw = new StringWriter()) {
        PrintWriter pw = new PrintWriter(sw);
        // print the thrown to String
        thrown.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        int idx = sStackTrace.indexOf("\n\tat");
        if (idx != -1) {
          thrownMessage = sStackTrace.substring(0, idx);
          thrownTrace = sStackTrace.substring(idx);
        } else {
          thrownTrace = sStackTrace;
        }
      } catch (IOException e) {
        // ignore...
      }
    }

    StringBuilder sb = new StringBuilder();

    if (colors) {
      sb.append(prefix(record.getLevel()));
    }
    sb.append(message);
    if (thrownMessage != null) {
      sb.append(" caused by ");
      sb.append(thrownMessage);
    }
    if (colors) {
      sb.append(suffix(record.getLevel()));
    }
    if (thrownTrace != null) {
      sb.append(thrownTrace);
    } else {
      sb.append(System.lineSeparator());
    }

    return sb.toString();
  }

  private static String prefix(Level l) {
    if (SEVERE.equals(l)) {
      return "\u001B[1m\u001B[31m";
    }
    if (WARNING.equals(l)) {
      return "\u001B[1m\u001B[33m";
    }
    if (INFO.equals(l)) {
      return "";
    }
    if (CONFIG.equals(l)) {
      return "\u001B[1m\u001B[34m";
    }
    if (FINE.equals(l)) {
      return "\u001B[1m\u001B[32m";
    }
    if (FINER.equals(l)) {
      return "\u001B[1m\u001B[94m";
    }
    if (FINEST.equals(l)) {
      return "\u001B[94m";
    }

    return "[" + l.getName().toUpperCase() + "] ";
  }

  private static String suffix(Level l) {
    if (SEVERE.equals(l)) {
      return "\u001B[0m";
    }
    if (WARNING.equals(l)) {
      return "\u001B[0m";
    }
    if (INFO.equals(l)) {
      return "";
    }
    if (CONFIG.equals(l)) {
      return "\u001B[0m";
    }
    if (FINE.equals(l)) {
      return "\u001B[0m";
    }
    if (FINER.equals(l)) {
      return "\u001B[0m";
    }
    if (FINEST.equals(l)) {
      return "\u001B[0m";
    }

    return "";
  }
}
