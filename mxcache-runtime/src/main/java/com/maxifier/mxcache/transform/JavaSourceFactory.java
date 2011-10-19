package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.tuple.Tuple;
import com.maxifier.mxcache.tuple.TupleFactory;
import com.maxifier.mxcache.tuple.TupleGenerator;
import gnu.trove.THashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 13:18:29
 */
public class JavaSourceFactory implements TransformFactory {
    private static final TupleFactory KEY_FACTORY = TupleGenerator.getTupleFactory(Object.class, Object.class);
    private static final String CLASS_NAME = "$$$TransformHolder$$$";
    private static final Map<Tuple, Class> METHOD_HOLDERS = new THashMap<Tuple, Class>();

    static String generateSource(Annotation instance, Class param) throws InvocationTargetException, IllegalAccessException {
        Class<? extends Annotation> annotationType = instance.annotationType();
        SourceCode sourceCode = annotationType.getAnnotation(SourceCode.class);
        if (sourceCode == null) {
            throw new IllegalStateException("Source-based custom transform annotations should have @SourceCode annotation");
        }
        StringBuilder definition = new StringBuilder();
        definition.append("public class " + CLASS_NAME + " {\n");
        definition.append("\tpublic static ").append(sourceCode.target().getCanonicalName()).append(" transform(").append(param.getCanonicalName()).append(" key) {\n");
        for (Method method : annotationType.getDeclaredMethods()) {
            definition.append("\t\t").append(method.getReturnType().getCanonicalName()).append(" ").append(method.getName()).append(" = ").append(toSourceString(method.invoke(instance))).append(";\n");
        }
        definition.append("\t\treturn ").append(sourceCode.value()).append(";\n");
        definition.append("\t}\n");
        definition.append("}");
        return definition.toString();
    }

    private static String toSourceString(Object o) {
        if (o instanceof String) {
            return ((String) o).replace("\"", "\\\"").replace("\n", "\\n");
        } else if (o instanceof Class) {
            return ((Class) o).getCanonicalName() + ".class";
        } else if (o instanceof Enum) {
            Enum e = (Enum) o;
            return e.getDeclaringClass().getCanonicalName() + "." + e.name();
        } else if (o.getClass().isArray()) {
            StringBuilder b = new StringBuilder();
            b.append("new ").append(o.getClass().getComponentType().getCanonicalName()).append("[] {");
            int length = Array.getLength(o);
            for (int i = 0; i<length; i++) {
                if (i != 0) {
                    b.append(", ");
                }
                b.append(toSourceString(Array.get(o, i)));
            }
            b.append("}");
            return b.toString();
        } else if (o instanceof Character) {
            char c = (Character) o;
            if (c == '\'') {
                return "'\\''";
            }
            if (c == '\n') {
                return "'\\n'";
            }
            return "\'" + c + "\'";
        } else if (o instanceof Annotation) {
            throw new UnsupportedOperationException();
        } else if (o instanceof Float) {
            return o + "f";
        } else if (o instanceof Double) {
            return o + "d";
        } else if (o instanceof Byte) {
            return "((byte)" + o + ")";
        } else if (o instanceof Short) {
            return "((short)" + o + ")";
        } else if (o instanceof Long) {
            return o + "L";
        }
        // int, boolean, smth else?
        return o.toString();
    }

    private static synchronized Class getMethodHolderFor(Annotation a, Class param) {
        Class<? extends Annotation> annotationType = a.annotationType();
        Tuple t0 = KEY_FACTORY.create(annotationType, param);
        Class gen = METHOD_HOLDERS.get(t0);
        if (gen != null) {
            return gen;
        }
        Tuple t = KEY_FACTORY.create(a, param);
        Class res = METHOD_HOLDERS.get(t);
        if (res == null) {
            res = generateMethodHolderFor(a, param);
            METHOD_HOLDERS.put(annotationType.getDeclaredMethods().length == 0 ? t0 : t, res);
        }
        return res;
    }

    private static Class generateMethodHolderFor(Annotation a, Class param) {
        try {
            String source = generateSource(a, param);
            return CompileHelper.compile(CLASS_NAME, source);
        } catch (Exception e) {
            throw new MxCacheException(e);
        }
    }

    @Override
    public TransformGenerator create(Annotation annotation, Annotation[] allAnnotations, Class paramType) {
        return TransformGeneratorFactoryImpl.getInstance().getTransformator(paramType, getMethodHolderFor(annotation, paramType), "transform");
    }
}
