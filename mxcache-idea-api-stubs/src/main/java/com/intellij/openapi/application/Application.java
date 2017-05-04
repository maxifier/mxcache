/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.application;

import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.util.Computable;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Application extends ComponentManager {
     <T> T runReadAction(Computable<T> p1);

     void runReadAction(Runnable p1);

     void runWriteAction(Runnable p1);

}
