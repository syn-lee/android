/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.dsl.model.dependencies;

import com.android.tools.idea.gradle.dsl.parser.elements.*;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Gradle artifact dependency. There are two notations supported for declaring a dependency on an external module. One is a string
 * notation formatted this way:
 * <pre>
 * configurationName "group:name:version:classifier@extension"
 * </pre>
 * The other is a map notation:
 * <pre>
 * configurationName group: group:, name: name, version: version, classifier: classifier, ext: extension
 * </pre>
 * For more details, visit:
 * <ol>
 *  <li><a href="https://docs.gradle.org/2.4/userguide/dependency_management.html">Gradle Dependency Management</a></li>
 *  <li><a href="https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.dsl.DependencyHandler.html">Gradle
 * DependencyHandler</a></li>
 * </ol>
 */
public abstract class ArtifactDependencyModel extends DependencyModel {
  @NotNull
  public abstract String name();

  @Nullable
  public abstract String group();

  @Nullable
  public abstract String version();

  public abstract void setVersion(@NotNull String name);

  @Nullable
  public abstract String classifier();

  @Nullable
  public abstract String extension();

  @NotNull
  protected static List<ArtifactDependencyModel> create(@NotNull GradleDslElement element) {
    List<ArtifactDependencyModel> results = Lists.newArrayList();
    assert element instanceof GradleDslExpression || element instanceof GradleDslExpressionMap;
    if (element instanceof GradleDslExpressionMap) {
      results.add(new MapNotation((GradleDslExpressionMap)element));
    }
    else if (element instanceof GradleDslMethodCall) {
      String name = element.getName();
      if (!"project".equals(name) && !"fileTree".equals(name)) {
        for (GradleDslElement argument : ((GradleDslMethodCall)element).getArguments()) {
          results.addAll(create(argument));
        }
      }
    } else {
      GradleDslExpression dslLiteral = (GradleDslLiteral)element;
      String value = dslLiteral.getValue(String.class);
      if (value != null) {
        ArtifactDependencySpec spec = ArtifactDependencySpec.create(value);
        if (spec != null) {
          results.add(new CompactNotation((GradleDslLiteral)element, spec));
        }
      }
    }
    return results;
  }

  @NotNull
  public ArtifactDependencySpec getSpec() {
    return new ArtifactDependencySpec(name(), group(), version(), classifier(), extension());
  }

  private static class MapNotation extends ArtifactDependencyModel {
    @NotNull private GradleDslExpressionMap myDslElement;

    MapNotation(@NotNull GradleDslExpressionMap dslElement) {
      myDslElement = dslElement;
    }

    @Override
    @NotNull
    public String name() {
      String value = myDslElement.getProperty("name", String.class);
      assert value != null;
      return value;
    }

    @Override
    @Nullable
    public String group() {
      return myDslElement.getProperty("group", String.class);
    }

    @Override
    @Nullable
    public String version() {
      return myDslElement.getProperty("version", String.class);
    }

    @Override
    public void setVersion(@NotNull String name) {
      myDslElement.setNewLiteral("version", name);
    }

    @Override
    @Nullable
    public String classifier() {
      return myDslElement.getProperty("classifier", String.class);
    }

    @Override
    @Nullable
    public String extension() {
      return myDslElement.getProperty("ext", String.class);
    }

    @Override
    @NotNull
    protected GradleDslElement getDslElement() {
      return myDslElement;
    }
  }

  private static class CompactNotation extends ArtifactDependencyModel {
    @NotNull private GradleDslExpression myDslExpression;
    @NotNull private ArtifactDependencySpec mySpec;

    private CompactNotation(@NotNull GradleDslExpression dslExpression, @NotNull ArtifactDependencySpec spec) {
      myDslExpression = dslExpression;
      mySpec = spec;
    }

    @Override
    @NotNull
    public String name() {
      return mySpec.name;
    }

    @Override
    @Nullable
    public String group() {
      return mySpec.group;
    }

    @Override
    @Nullable
    public String version() {
      return mySpec.version;
    }

    @Override
    public void setVersion(@NotNull String version) {
      mySpec.version = version;
      myDslExpression.setValue(mySpec.toString());
    }

    @Override
    @Nullable
    public String classifier() {
      return mySpec.classifier;
    }

    @Override
    @Nullable
    public String extension() {
      return mySpec.extension;
    }

    @NotNull
    @Override
    protected GradleDslElement getDslElement() {
      return myDslExpression;
    }
  }
}