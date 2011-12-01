package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.util.CodegenHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import javax.tools.*;
import javax.tools.JavaFileObject.Kind;

public final class CompileHelper extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private CompileHelper(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject originatingSource) throws IOException {
        return new OutputJavaFileObject(name);
    }

    private Class loadedClass;

    public static Class compile(String className, String source) throws IOException {
        // buffer, not builder, cause compilation may be performed in parallel
        final StringBuffer buffer = new StringBuffer();
        DiagnosticListener<JavaFileObject> listener = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                buffer.append(diagnostic).append('\n');
            }
        };
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        CompileHelper instance = new CompileHelper(compiler.getStandardFileManager(listener, null, null));
        JavaFileObject fileObject = new InputJavaFileObject(className, source);

        JavaCompiler.CompilationTask task = compiler.getTask(null, instance, listener, null, null, Arrays.asList(fileObject));
        if (!task.call()) {
            throw new MxCacheException("Cannot compile fragment <" + source + ">: " + buffer);
        }
        return instance.loadedClass;
    }

    private static URI createUri(String name, Kind kind) throws IOException {
        try {
            return new URI("mxcache:///" + name.replace('.', '/') + kind.extension);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private class OutputJavaFileObject extends SimpleJavaFileObject {
        OutputJavaFileObject(String name) throws IOException {
            super(createUri(name, Kind.CLASS), Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    loadedClass = CodegenHelper.loadClass(AccessController.doPrivileged(new CreateClassLoaderAction()), toByteArray());
                }
            };
        }
    }

    private static class CreateClassLoaderAction implements PrivilegedAction<ClassLoader> {
        @Override
        public ClassLoader run() {
            return new ClassLoader() {};
        }
    }

    private static class InputJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        public InputJavaFileObject(String name, String code) throws IOException {
            super(createUri(name, Kind.SOURCE), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

}