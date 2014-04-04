/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.compiler;

import java.io.DataOutput;
import java.io.IOException;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ValidityState {
     public abstract void save(DataOutput p1) throws IOException;

     public abstract boolean equalsTo(ValidityState p1);

}
