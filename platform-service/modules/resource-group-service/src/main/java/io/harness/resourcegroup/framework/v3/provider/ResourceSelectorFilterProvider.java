/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */
package io.harness.resourcegroup.framework.v3.provider;

import io.harness.spec.server.platform.model.ResourceSelectorFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

public class ResourceSelectorFilterProvider implements ParamConverterProvider {
  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (rawType == ResourceSelectorFilter.class) {
      return new ParamConverter<T>() {
        @Override
        public T fromString(String s) {
          return null;
        }

        @Override
        public String toString(T t) {
          return null;
        }
      };
    }
    return null;
  }
}
