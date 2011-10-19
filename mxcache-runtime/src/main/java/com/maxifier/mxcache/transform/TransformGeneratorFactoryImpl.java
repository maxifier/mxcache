package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.tuple.Tuple;
import com.maxifier.mxcache.tuple.TupleGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.maxifier.mxcache.transform.InvocationType.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.09.2010
 * Time: 18:59:42
 */
public final class TransformGeneratorFactoryImpl implements TransformGeneratorFactory {
    private static final TransformGeneratorFactoryImpl INSTANCE = new TransformGeneratorFactoryImpl();

    private TransformGeneratorFactoryImpl() {}

    public static TransformGeneratorFactoryImpl getInstance() {
        return INSTANCE;
    }

    @Override
    @NotNull
    public TransformGenerator forMethod(java.lang.reflect.Method method) throws InvalidTransformAnnotations {
        try {
            Class[] params = method.getParameterTypes();
            switch (params.length) {
                case 0:
                    return TransformGenerator.NO_TRANSFORM;
                case 1:
                    return forArgument(method.getParameterAnnotations()[0], params[0]);
                default:
                    return createMultiParam(method.getParameterAnnotations(), params);
            }
        } catch (InvalidTransformAnnotations e) {
            throw new InvalidTransformAnnotations("Cannot create transform generator for " + method, e);
        }
    }

    private abstract static class TransformGeneratorRef<A extends Annotation> {
        final A annotation;

        final Class paramType;

        abstract TransformGenerator get(Annotation[] allAnnotations);

        TransformGeneratorRef(A annotation, Class paramType) {
            this.annotation = annotation;
            this.paramType = paramType;
        }

        @Override
        public String toString() {
            return annotation.toString();
        }
    }

    private class SimpleTransformGeneratorRef extends TransformGeneratorRef<Transform> {
        SimpleTransformGeneratorRef(Transform annotation, Class paramType) {
            super(annotation, paramType);
        }

        @Override
        TransformGenerator get(Annotation[] allAnnotations) {
            return getTransformator(paramType, annotation);
        }
    }

    private final class IndirectTransformGeneratorRef extends TransformGeneratorRef<Annotation> {
        private final SimpleTransformGeneratorRef ref;

        IndirectTransformGeneratorRef(Annotation annotation, Class paramType, Transform transform) {
            super(annotation, paramType);
            this.ref = new SimpleTransformGeneratorRef(transform, paramType);
        }

        @Override
        TransformGenerator get(Annotation[] allAnnotations) {
            return ref.get(allAnnotations);
        }
    }

    private final class IndirectReversibleTransformGeneratorRef extends TransformGeneratorRef<Annotation> {
        private final ReversibleTransformGeneratorRef ref;

        IndirectReversibleTransformGeneratorRef(Annotation annotation, Class paramType, ReversibleTransform transform) {
            super(annotation, paramType);
            this.ref = new ReversibleTransformGeneratorRef(transform, paramType);
        }

        @Override
        TransformGenerator get(Annotation[] allAnnotations) {
            return ref.get(allAnnotations);
        }
    }

    private static final class CustomTranformGeneratorRef extends TransformGeneratorRef<Annotation> {
        private final CustomTransformAnnotation customTransform;

        CustomTranformGeneratorRef(Annotation annotation, Class paramType, CustomTransformAnnotation customTransform) {
            super(annotation, paramType);
            this.customTransform = customTransform;
        }

        @Override
        TransformGenerator get(Annotation[] allAnnotations) {
            Class<? extends TransformFactory> generatorClass = customTransform.transformGenerator();
            //noinspection unchecked
            return CacheFactory.getDefaultContext().getInstanceProvider().forClass(generatorClass).create(customTransform, allAnnotations, paramType);
        }
    }

    private final class ReversibleTransformGeneratorRef extends TransformGeneratorRef<ReversibleTransform> {
        ReversibleTransformGeneratorRef(ReversibleTransform annotation, Class paramType) {
            super(annotation, paramType);
        }

        @Override
        TransformGenerator get(Annotation[] allAnnotations) {
            TransformGenerator forward = getTransformator(paramType, annotation.forward());
            TransformGenerator backward = getTransformator(forward.getTransformedType(paramType), annotation.backward());
            return new CompositeTransformGenerator(forward, backward);
        }
    }

    @Override
    @NotNull
    public TransformGenerator forArgument(Annotation[] annotations, Class argType) throws InvalidTransformAnnotations {
        List<TransformGeneratorRef> refs = new ArrayList<TransformGeneratorRef>();
        for (Annotation annotation : annotations) {
            TransformGeneratorRef res = forAnnotation(argType, annotation);
            if (res != null) {
                refs.add(res);
            }
        }
        if (refs.isEmpty()) {
            return TransformGenerator.NO_TRANSFORM;
        }
        if (refs.size() > 1) {
            throw new InvalidTransformAnnotations("Too many transform annotations: " + refs);
        }
        return refs.get(0).get(annotations);
    }

    @Nullable
    private TransformGeneratorRef forAnnotation(Class argType, Annotation annotation) {
        if (annotation instanceof Transform) {
            return new SimpleTransformGeneratorRef((Transform) annotation, argType);
        }
        if (annotation instanceof ReversibleTransform) {
            return new ReversibleTransformGeneratorRef((ReversibleTransform) annotation, argType);
        }
        return forCustomTransformAnnotation(argType, annotation);
    }

    @Nullable
    private TransformGeneratorRef forCustomTransformAnnotation(Class argType, Annotation annotation) {
        CustomTransformAnnotation a = annotation.annotationType().getAnnotation(CustomTransformAnnotation.class);
        Transform t = annotation.annotationType().getAnnotation(Transform.class);
        ReversibleTransform r = annotation.annotationType().getAnnotation(ReversibleTransform.class);
        if (a != null) {
            if (t != null) {
                throw new InvalidTransformAnnotations(annotation.annotationType() + " has both @CustomTransformAnnotation and @Transform");
            }
            if (r != null) {
                throw new InvalidTransformAnnotations(annotation.annotationType() + " has both @CustomTransformAnnotation and @ReversibleTransform");
            }
            return new CustomTranformGeneratorRef(annotation, argType, a);
        }
        if (t != null) {
            if (r != null) {
                throw new InvalidTransformAnnotations(annotation.annotationType() + " has both @Transform and @ReversibleTransform");
            }
            return new IndirectTransformGeneratorRef(annotation, argType, t);
        }
        if (r != null) {
            return new IndirectReversibleTransformGeneratorRef(annotation, argType, r);
        }
        return null;
    }

    @NotNull
    TransformGenerator createMultiParam(Annotation[][] paramAnnotations, Class[] params) throws InvalidTransformAnnotations {
        int transformatorCount = 0;
        TransformGenerator[] transformGenerators = new TransformGenerator[params.length];
        Class[] transformedParams = Arrays.copyOf(params, params.length);
        for (int i = 0; i < paramAnnotations.length; i++) {
            Annotation[] paramAnnotation = paramAnnotations[i];
            Class paramType = params[i];
            TransformGenerator transformGenerator = forArgument(paramAnnotation, paramType);
            if (transformGenerator != null) {
                transformGenerators[i] = transformGenerator;
                transformedParams[i] = transformGenerator.getTransformedType(paramType);
                transformatorCount++;
            }
        }
        if (transformatorCount == 0) {
            return TransformGenerator.NO_TRANSFORM;
        }

        Class<? extends Tuple> tupleIn = TupleGenerator.getTupleClass(params);
        Class<? extends Tuple> tupleOut = TupleGenerator.getTupleClass(transformedParams);

        return new TupleTransformGenerator(transformGenerators, params, transformedParams, tupleIn, tupleOut);
    }

    @Override
    public TransformGenerator getTransformator(Class param, @NotNull Transform key) {
        Class owner = key.owner();
        String name = key.method();
        return getTransformator(param, owner == Transform.KEY_ITSELF.class ? null : owner, name.equals(Transform.ONLY_PUBLIC_METHOD) ? null : name);
    }

    /**
     * Note: params cannot be non-null while name is null.
     * @param paramType type of parameter to convert
     * @param owner owner of converter method; if null, method is looked up in key itself
     * @param name name of method; if null, the only public method of transformator is used
     * @return transform generator which converts parameter of type <code>paramType</code> with method
     * <code>owner.name(params)</code>
     */
    @Override
    public TransformGenerator getTransformator(Class<?> paramType, Class owner, String name) {
        boolean keyInstanceMethods = owner == null;
        if (keyInstanceMethods) {
            owner = paramType;
        }
        java.lang.reflect.Method method = findMethod(owner, name, paramType, keyInstanceMethods);
        InvocationType invocationType = getKeyInvocationType(owner, method, keyInstanceMethods);
        Method forward = Method.getMethod(method);
        Type ownerType = Type.getType(owner);
        return new ExternalTransformGenerator(invocationType, ownerType, forward);
    }

    private InvocationType getKeyInvocationType(Class owner, java.lang.reflect.Method forwardMethod, boolean keyInstanceMethods) {
        switch (forwardMethod.getParameterTypes().length) {
            case 0:
                return getZeroArgKeyInvocationType(owner, forwardMethod, keyInstanceMethods);
            case 1:
                return getSingleArgKeyInvocationType(owner, forwardMethod, keyInstanceMethods);
            default:
                throw new IllegalArgumentException("Invalid transform found: " + forwardMethod);
        }
    }

    private InvocationType getZeroArgKeyInvocationType(Class owner, java.lang.reflect.Method forwardMethod, boolean keyInstanceMethods) {
        if (!keyInstanceMethods) {
            throw new IllegalArgumentException("Invalid transform found: " + forwardMethod);
        }
        return owner.isInterface() ? KEY_INTERFACE : KEY_VIRTUAL;
    }

    private InvocationType getSingleArgKeyInvocationType(Class owner, java.lang.reflect.Method forwardMethod, boolean keyInstanceMethods) {
        if (Modifier.isStatic(forwardMethod.getModifiers())) {
            return STATIC;
        }
        if (keyInstanceMethods) {
            // у ключа не статические методы мы не смотрим!
            throw new IllegalArgumentException("Invalid transform found: " + forwardMethod);
        }
        return owner.isInterface() ? INTERFACE : VIRTUAL;
    }

    static java.lang.reflect.Method findMethod(Class owner, String name, Class paramType, boolean keyInstanceMethods) {
        java.lang.reflect.Method[] methods = owner.getMethods();
        List<java.lang.reflect.Method> suitable = getSuitableMethods(name, paramType, keyInstanceMethods, methods);
        if (suitable.isEmpty()) {
            String ref = owner + "." + (name == null ? "<only public>" : name);
            throw new IllegalArgumentException("No such public method " + ref + "(? super " + paramType.getCanonicalName() + (keyInstanceMethods ? ") or " + ref + "()" : ")"));
        }
        if (suitable.size() > 1) {
            String ref = owner + "." + (name == null ? "<only public>" : name);
            throw new IllegalArgumentException("Too many public methods " + ref + "(? super " + paramType.getCanonicalName() + (keyInstanceMethods ? ") or " + ref + "(): " : "): ") + suitable);
        }
        return suitable.get(0);
    }

    private static List<java.lang.reflect.Method> getSuitableMethods(String name, Class paramType, boolean keyInstanceMethods, java.lang.reflect.Method[] methods) {
        List<java.lang.reflect.Method> suitable = new ArrayList<java.lang.reflect.Method>();
        for (java.lang.reflect.Method method : methods) {
            if (nameMatches(name, method) && isParamsSuites(paramType, keyInstanceMethods, method)) {
                suitable.add(method);
            }
        }
        return suitable;
    }

    private static boolean isParamsSuites(Class paramType, boolean keyInstanceMethods, java.lang.reflect.Method method) {
        Class<?>[] params = method.getParameterTypes();
        switch (params.length) {
            case 0:
                return keyInstanceMethods && !Modifier.isStatic(method.getModifiers());
            case 1:
                // в качестве трансформаторов у самого ключа могут примен€тьс€ “ќЋ№ ќ статические методы
                // (было бы странно брать экземпл€р ключа откуда-то, когда ты сам ключ!)
                return (!keyInstanceMethods || Modifier.isStatic(method.getModifiers())) && params[0].isAssignableFrom(paramType);
            default:
                // методы с большим числом аргументов мы не рассматриваем
                return false;
        }
    }

    private static boolean nameMatches(String name, java.lang.reflect.Method method) {
        return name == null ? !isObjectMethod(method) : method.getName().equals(name);
    }

    private static boolean isObjectMethod(java.lang.reflect.Method method) {
        String name = method.getName();
        Class<?>[] parameters = method.getParameterTypes();
        for (java.lang.reflect.Method o : Object.class.getMethods()) {
            if (name.equals(o.getName()) && Arrays.equals(parameters, o.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }
}
