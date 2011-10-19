package com.maxifier.mxcache.instrumentation;

import com.magenta.dataserializator.*;
import com.magenta.dataserializator.link.LinkContext;
import com.magenta.dataserializator.link.LinkContextImpl;
import com.magenta.dataserializator.stream.MxStreamReader;
import com.maxifier.mxcache.context.CacheContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Map;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 10.03.11
* Time: 17:34
*/
public class TestMxObjectInput extends ObjectInputStream implements MxObjectInput {
    private final LinkContext linkContext;
    private final ClassLoader classLoader;

    public TestMxObjectInput(InputStream in, CacheContext context, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
        linkContext = new LinkContextImpl();
        linkContext.registerConst(CacheContext.class, context);
    }

    @Override
    public <T> T deserialize() throws ClassNotFoundException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MxStreamReader getStreamReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkContext getLinkContext() {
        return linkContext;
    }

    @Override
    public NumberReader getIntReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberReader getLongReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasOption(MxOption option) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getOption(MxParametricOption<T> option) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<MxFlagOption> getFlagOptions() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Map<MxParametricOption, Object> getParametricOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        Class clazz = Class.forName(desc.getName(), false, classLoader);

        if (clazz != null) {
            // the classloader knows of the class
            return clazz;
        } else {
            // classloader knows not of class, let the super classloader do it
            return super.resolveClass(desc);
        }
    }
}
