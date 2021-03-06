// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.actions;

import com.google.devtools.build.lib.concurrent.ThreadSafety.ThreadSafe;

import javax.annotation.Nullable;

/**
 * An interface for an {@link Action}, containing only side-effect-free query methods for
 * information needed during both action analysis and execution.
 *
 * <p>The split between {@link Action} and {@link ActionExecutionMetadata} is somewhat arbitrary,
 * other than that all methods with side effects must belong to the former.
 */
public interface ActionExecutionMetadata extends ActionAnalysisMetadata {
  /**
   * If this executable can supply verbose information, returns a string that can be used as a
   * progress message while this executable is running. A return value of {@code null} indicates no
   * message should be reported.
   */
  @Nullable
  String getProgressMessage();

  /**
   * <p>Returns a string encoding all of the significant behaviour of this
   * Action that might affect the output.  The general contract of
   * <code>getKey</code> is this: if the work to be performed by the
   * execution of this action changes, the key must change. </p>
   *
   * <p>As a corollary, the build system is free to omit the execution of an
   * Action <code>a1</code> if (a) at some time in the past, it has already
   * executed an Action <code>a0</code> with the same key as
   * <code>a1</code>, and (b) the names and contents of the input files listed
   * by <code>a1.getInputs()</code> are identical to the names and contents of
   * the files listed by <code>a0.getInputs()</code>. </p>
   *
   * <p>Examples of changes that should affect the key are:
   * <ul>
   *  <li>Changes to the BUILD file that materially affect the rule which gave
   *  rise to this Action.</li>
   *
   *  <li>Changes to the command-line options, environment, or other global
   *  configuration resources which affect the behaviour of this kind of Action
   *  (other than changes to the names of the input/output files, which are
   *  handled externally).</li>
   *
   *  <li>An upgrade to the build tools which changes the program logic of this
   *  kind of Action (typically this is achieved by incorporating a UUID into
   *  the key, which is changed each time the program logic of this action
   *  changes).</li>
   *
   * </ul></p>
   */
  String getKey();

  /**
   * Returns a human-readable description of the inputs to {@link #getKey()}.
   * Used in the output from '--explain', and in error messages for
   * '--check_up_to_date' and '--check_tests_up_to_date'.
   * May return null, meaning no extra information is available.
   *
   * <p>If the return value is non-null, for consistency it should be a multiline message of the
   * form:
   * <pre>
   *   <var>Summary</var>
   *     <var>Fieldname</var>: <var>value</var>
   *     <var>Fieldname</var>: <var>value</var>
   *     ...
   * </pre>
   * where each line after the first one is intended two spaces, and where any fields that might
   * contain newlines or other funny characters are escaped using {@link
   * com.google.devtools.build.lib.shell.ShellUtils#shellEscape}.
   * For example:
   * <pre>
   *   Compiling foo.cc
   *     Command: /usr/bin/gcc
   *     Argument: '-c'
   *     Argument: foo.cc
   *     Argument: '-o'
   *     Argument: foo.o
   * </pre>
   */
  @Nullable String describeKey();

  /**
   * Get the {@link RunfilesSupplier} providing runfiles needed by this action.
   */
  RunfilesSupplier getRunfilesSupplier();

  /**
   * Returns true iff the getInputs set is known to be complete.
   *
   * <p>For most Actions, this always returns true, but in some cases (e.g. C++ compilation), inputs
   * are dynamically discovered from the previous execution of the Action, and so before the initial
   * execution, this method will return false in those cases.
   *
   * <p>Any builder <em>must</em> unconditionally execute an Action for which inputsKnown() returns
   * false, regardless of all other inferences made by its dependency analysis. In addition, all
   * prerequisites mentioned in the (possibly incomplete) value returned by getInputs must also be
   * built first, as usual.
   */
  @ThreadSafe
  boolean inputsKnown();

  /**
   * Returns true iff inputsKnown() may ever return false.
   */
  @ThreadSafe
  boolean discoversInputs();
}
