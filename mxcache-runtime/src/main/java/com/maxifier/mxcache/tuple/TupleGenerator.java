/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.asm.Label;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.*;
import gnu.trove.map.hash.THashMap;
import gnu.trove.strategy.HashingStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.*;
import static com.maxifier.mxcache.asm.commons.GeneratorAdapter.EQ;
import static com.maxifier.mxcache.asm.commons.GeneratorAdapter.NE;
import static com.maxifier.mxcache.asm.commons.Method.getMethod;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * TupleGenerator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class TupleGenerator {
    private static final Type FLOAT_WRAPPER_TYPE = getType(Float.class);
    private static final Type DOUBLE_WRAPPER_TYPE = getType(Double.class);
    private static final Type STRING_BUILDER_TYPE = getType(StringBuilder.class);
    private static final Type TUPLE_TYPE = getType(Tuple.class);
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION_TYPE = getType(IllegalArgumentException.class);

    private static final Method EQUALS_METHOD = getMethod("boolean equals(Object)");
    private static final Method HASH_CODE_METHOD = getMethod("int hashCode()");
    private static final Method GET_METHOD = getMethod("java.lang.Object get(int)");

    private static final Method GET_BOOLEAN_METHOD = getMethod("boolean getBoolean(int)");
    private static final Method GET_BYTE_METHOD = getMethod("byte getByte(int)");
    private static final Method GET_CHAR_METHOD = getMethod("char getChar(int)");
    private static final Method GET_SHORT_METHOD = getMethod("short getShort(int)");
    private static final Method GET_INT_METHOD = getMethod("int getInt(int)");
    private static final Method GET_LONG_METHOD = getMethod("long getLong(int)");
    private static final Method GET_FLOAT_METHOD = getMethod("float getFloat(int)");
    private static final Method GET_DOUBLE_METHOD = getMethod("double getDouble(int)");

    private static final Method SIZE_METHOD = getMethod("int size()");
    private static final Method FLOAT_IS_NAN_METHOD = getMethod("boolean isNaN(float)");
    private static final Method DOUBLE_IS_NAN_METHOD = getMethod("boolean isNaN(double)");
    private static final Method FLOAT_TO_INT_BITS_METHOD = getMethod("int floatToIntBits(float)");
    private static final Method DOUBLE_TO_LONG_BITS_METHOD = getMethod("long doubleToLongBits(double)");
    private static final Method APPEND_OBJECT_METHOD = getMethod("java.lang.StringBuilder append(java.lang.Object)");
    private static final Method APPEND_INT_METHOD = getMethod("java.lang.StringBuilder append(int)");
    private static final Method INIT_EXCEPTION_METHOD = getMethod("void <init>(String)");
    public static final Method TUPLE_GET_HASHING_STRATEGIES = Method.getMethod("gnu.trove.strategy.HashingStrategy[] getHashingStrategies()");

    private static final THashMap<String, TupleClass> CACHE = new THashMap<String, TupleClass>();

    private static final Type HASHING_STRATEGY_TYPE = Type.getType(HashingStrategy.class);
    private static final Type HASHING_STRATEGY_ARRAY_TYPE = Type.getType(HashingStrategy[].class);

    private static final int INT_BITS = 32;

    private static final int TRUE_HASHCODE = Boolean.TRUE.hashCode();
    private static final int FALSE_HASHCODE = Boolean.FALSE.hashCode();
    private static final Method EQUALS_OBJECT_OBJECT_METHOD = Method.getMethod("boolean equals(Object, Object)");
    private static final Method COMPUTE_HASH_CODE_METHOD = Method.getMethod("int computeHashCode(Object)");
    private static final String HASHING_STRATEGIES_FIELD = "hashingStrategies";

    private TupleGenerator() {
    }

    public static Class<Tuple> getTupleClass(Class... values) {
        return getTupleClass0(toErasedTypes(values)).getRealClass();
    }

    @PublicAPI
    public static Class<Tuple> getTupleClass(Type... values) {
        return getTupleClass0(erase(values)).getRealClass();
    }

    public static String getTupleClassName(Type... values) {
        return generateTupleClassName(erase(values));
    }

    public static TupleFactory createTupleFactory(HashingStrategy[] hashingStrategies, Class... types) {
        return getTupleClass0(toErasedTypes(types)).createFactory(hashingStrategies);
    }
    
    private static class TupleClass {
        private final Class<Tuple> realClass;

        public TupleClass(Class<Tuple> realClass) {
            this.realClass = realClass;
        }

        public Class<Tuple> getRealClass() {
            return realClass;
        }

        public synchronized TupleFactory createFactory(HashingStrategy[] hashingStrategies) {
            Constructor<?>[] ctors = realClass.getDeclaredConstructors();
            if (ctors.length != 1) {
                throw new IllegalStateException("No constructor for " + realClass);
            }
            //noinspection unchecked
            return new TupleFactoryImpl(hashingStrategies, (Constructor<? extends Tuple>) ctors[0]);
        }
    }

    private static TupleClass getTupleClass0(Type...types){
        String tupleClassName = generateTupleClassName(types);
        synchronized(CACHE) {
            TupleClass tupleClass = CACHE.get(tupleClassName);
            if (tupleClass == null) {
                tupleClass = new TupleClass(generateTupleClass(tupleClassName, types));
                CACHE.put(tupleClassName, tupleClass);
            }
            return tupleClass;
        }
    }

    private static Class<Tuple> generateTupleClass(String tupleClassName, Type[] types) {
        return loadClass(Tuple.class.getClassLoader(), generateTupleClassBytecode(tupleClassName, types));
    }

    private static byte[] generateTupleClassBytecode(String tupleClassName, Type[] types) {
        ClassGenerator classWriter = new ClassGenerator(ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, tupleClassName, TUPLE_TYPE);
        Type tupleType = classWriter.getThisType();
        MxField[] fields = new MxField[types.length];
        for (int i = 0; i < types.length; i++) {
            MxField field = classWriter.defineField(ACC_PUBLIC | ACC_FINAL, "$" + i, types[i]);
            classWriter.defineGetter(field, "getElement" + i);
            fields[i] = field;
        }
        generateConstructor(types, classWriter, fields);
        generateGet(types, tupleType, classWriter.defineMethod(ACC_PUBLIC, GET_METHOD));

        generateGetPrimitive(types, tupleType, Type.BOOLEAN_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_BOOLEAN_METHOD));
        generateGetPrimitive(types, tupleType, Type.BYTE_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_BYTE_METHOD));
        generateGetPrimitive(types, tupleType, Type.CHAR_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_CHAR_METHOD));
        generateGetPrimitive(types, tupleType, Type.SHORT_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_SHORT_METHOD));
        generateGetPrimitive(types, tupleType, Type.INT_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_INT_METHOD));
        generateGetPrimitive(types, tupleType, Type.LONG_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_LONG_METHOD));
        generateGetPrimitive(types, tupleType, Type.FLOAT_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_FLOAT_METHOD));
        generateGetPrimitive(types, tupleType, Type.DOUBLE_TYPE, classWriter.defineMethod(ACC_PUBLIC, GET_DOUBLE_METHOD));

        generateSize(types, classWriter.defineMethod(ACC_PUBLIC, SIZE_METHOD));
        generateHashCode(types, tupleType, classWriter.defineMethod(ACC_PUBLIC, HASH_CODE_METHOD));
        generateEquals(types, tupleType, classWriter.defineMethod(ACC_PUBLIC, EQUALS_METHOD));

        generateToString(types, tupleType, classWriter.defineMethod(ACC_PUBLIC, MxGeneratorAdapter.TO_STRING_METHOD));
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private static void generateConstructor(Type[] types, ClassGenerator classWriter, MxField[] fields) {
        Type[] ctorArgTypes = new Type[types.length + 1];
        System.arraycopy(types, 0, ctorArgTypes, 1, types.length);
        Type hashingStrategiesType = Type.getType(HashingStrategy[].class);
        ctorArgTypes[0] = hashingStrategiesType;
        MxConstructorGenerator ctor = classWriter.defineConstructor(ACC_PUBLIC, ctorArgTypes);
        ctor.callSuper(hashingStrategiesType);
        ctor.initFields(fields);
        ctor.returnValue();
        ctor.endMethod();
    }

    private static void generateSize(Type[] types, MxGeneratorAdapter visitor) {
        visitor.start();
        visitor.push(types.length);
        visitor.returnValue();
        visitor.endMethod();
    }

    private static void generateEquals(Type[] types, Type tupleType, MxGeneratorAdapter visitor) {
        Label equal = new Label();
        Label notEqual = new Label();
        Label end = new Label();

        visitor.start();
        visitor.loadThis();
        visitor.loadArg(0);
        visitor.ifCmp(OBJECT_TYPE, EQ, equal);

        visitor.loadArg(0);
        visitor.instanceOf(tupleType);
        visitor.ifZCmp(EQ, notEqual);

        visitor.loadArg(0);
        visitor.checkCast(tupleType);
        int other = visitor.newLocal(tupleType);
        visitor.storeLocal(other);

        for (int i = 0; i < types.length; i++) {
            Type type = types[i];

            Label next = new Label();
            if (isReferenceType(type)) {
                Label defaultEquals = new Label();

                visitor.loadThis();
                visitor.getField(tupleType, HASHING_STRATEGIES_FIELD, HASHING_STRATEGY_ARRAY_TYPE);
                visitor.push(i);
                visitor.arrayLoad(HASHING_STRATEGY_TYPE);
                visitor.dup();
                visitor.ifNull(defaultEquals);

                visitor.loadThis();
                visitor.getField(tupleType, "$" + i, type);
                visitor.loadLocal(other);
                visitor.getField(tupleType, "$" + i, type);
                visitor.invokeInterface(HASHING_STRATEGY_TYPE, EQUALS_OBJECT_OBJECT_METHOD);
                visitor.ifZCmp(EQ, notEqual);
                visitor.goTo(next);

                visitor.mark(defaultEquals);
                visitor.pop();
            } else {
                visitor.loadThis();
                visitor.getField(tupleType, HASHING_STRATEGIES_FIELD, HASHING_STRATEGY_ARRAY_TYPE);
                visitor.push(i);
                visitor.arrayLoad(HASHING_STRATEGY_TYPE);

                Label ok = new Label();
                visitor.ifNull(ok);

                visitor.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, "Primitives don't support hashing strategy: " + i + "th param of type " + type.getClassName());

                visitor.mark(ok);
            }

            visitor.loadThis();
            visitor.getField(tupleType, "$" + i, type);
            visitor.loadLocal(other);
            visitor.getField(tupleType, "$" + i, type);
            generateEquals(visitor, type, notEqual);
            visitor.mark(next);
        }

        visitor.mark(equal);
        visitor.push(true);
        visitor.goTo(end);

        visitor.mark(notEqual);
        visitor.push(false);

        visitor.mark(end);
        visitor.returnValue();
        visitor.endMethod();
    }

    private static void generateEquals(MxGeneratorAdapter visitor, Type type, Label notEqual) {
        switch (type.getSort()) {
            case OBJECT:
            case ARRAY:
                generateObjectEquals(visitor, notEqual);
                break;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case Type.LONG:
                visitor.ifCmp(type, NE, notEqual);
                break;
            case Type.FLOAT:
                generateFPEquals(visitor, notEqual, FLOAT_TYPE, FLOAT_WRAPPER_TYPE);
                break;
            case Type.DOUBLE:
                generateFPEquals(visitor, notEqual, DOUBLE_TYPE, DOUBLE_WRAPPER_TYPE);
                break;
            default:
                throw new UnsupportedOperationException("Unknown type: " + type);
        }
    }

    private static void generateFPEquals(MxGeneratorAdapter visitor, Label notEqual, Type type, Type wrapper) {
        assert type.getSort() == Type.FLOAT || type.getSort() == Type.DOUBLE;
        boolean isDouble = type.getSize() == 2;

        Label isNotNaN = new Label();
        Label end = new Label();

        visitor.dup(type);
        visitor.invokeStatic(wrapper, isDouble ? DOUBLE_IS_NAN_METHOD : FLOAT_IS_NAN_METHOD);
        visitor.ifZCmp(EQ, isNotNaN);

        visitor.pop(type);
        visitor.invokeStatic(wrapper, isDouble ? DOUBLE_IS_NAN_METHOD : FLOAT_IS_NAN_METHOD);
        visitor.ifZCmp(NE, end);
        visitor.goTo(notEqual);

        visitor.mark(isNotNaN);
        visitor.ifCmp(type, NE, notEqual);

        visitor.mark(end);
    }

    private static void generateObjectEquals(MxGeneratorAdapter visitor, Label notEqual) {
        Label isNull = new Label();
        Label endLabel = new Label();
        visitor.dup();
        visitor.ifNull(isNull);

        visitor.swap();
        visitor.invokeVirtual(OBJECT_TYPE, EQUALS_METHOD);
        visitor.ifZCmp(EQ, notEqual);
        visitor.goTo(endLabel);

        visitor.mark(isNull);
        visitor.pop();
        visitor.ifNonNull(notEqual);

        visitor.mark(endLabel);
    }

    private static void generateHashCode(Type[] types, Type tupleType, MxGeneratorAdapter visitor) {
        visitor.start();
        visitor.push(31);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (i != 0) {
                visitor.push(31);
                visitor.visitInsn(IMUL);
            }
            Label next = new Label();

            if (isReferenceType(type)) {
                Label defaultHashCode = new Label();

                visitor.loadThis();
                visitor.getField(tupleType, HASHING_STRATEGIES_FIELD, HASHING_STRATEGY_ARRAY_TYPE);
                visitor.push(i);
                visitor.arrayLoad(HASHING_STRATEGY_TYPE);
                visitor.dup();
                visitor.ifNull(defaultHashCode);

                visitor.loadThis();
                visitor.getField(tupleType, "$" + i, type);
                visitor.invokeInterface(HASHING_STRATEGY_TYPE, COMPUTE_HASH_CODE_METHOD);
                visitor.goTo(next);

                visitor.mark(defaultHashCode);
                visitor.pop();
            } else {
                visitor.loadThis();
                visitor.getField(tupleType, HASHING_STRATEGIES_FIELD, HASHING_STRATEGY_ARRAY_TYPE);
                visitor.push(i);
                visitor.arrayLoad(HASHING_STRATEGY_TYPE);

                Label ok = new Label();
                visitor.ifNull(ok);

                visitor.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, "No strategy expected for " + i + "th param of type " + type.getClassName());

                visitor.mark(ok);
            }
            visitor.loadThis();
            visitor.getField(tupleType, "$" + i, type);
            generateHashCode(visitor, type);
            visitor.mark(next);
            visitor.visitInsn(IADD);
        }
        visitor.returnValue();
        visitor.endMethod();
    }

    private static void generateHashCode(MxGeneratorAdapter visitor, Type type) {
        switch (type.getSort()) {
            case BYTE: 
            case CHAR:
            case SHORT:
            case INT:
                // the value itself is hashcode
                return;
            case BOOLEAN:
                generateBooleanHashCode(visitor);
                return;
            case Type.FLOAT:
                visitor.invokeStatic(FLOAT_WRAPPER_TYPE, FLOAT_TO_INT_BITS_METHOD);
                return;
            case Type.DOUBLE:
                visitor.invokeStatic(DOUBLE_WRAPPER_TYPE, DOUBLE_TO_LONG_BITS_METHOD);
                generateLongHashCode(visitor);
                return;
            case Type.LONG:
                generateLongHashCode(visitor);
                return;
            case OBJECT:
            case ARRAY:
                generateObjectHashCode(visitor);
                return;
            default:
                throw new UnsupportedOperationException("Unknown type: " + type);
        }
    }

    private static void generateObjectHashCode(MxGeneratorAdapter visitor) {
        Label labelNull = new Label();
        Label labelEnd = new Label();
        visitor.dup();

        visitor.ifNull(labelNull);

        visitor.invokeVirtual(OBJECT_TYPE, HASH_CODE_METHOD);
        visitor.goTo(labelEnd);

        visitor.mark(labelNull);
        visitor.pop();
        visitor.push(0);

        visitor.mark(labelEnd);
    }

    /**
     * Generates code that calculates hash code of boolean from the top of stack.
     * (true.hashCode() = 1231, false.hashCode() = 1237)   
     * @param visitor method visitor
     * @see Boolean#hashCode()
     */
    private static void generateBooleanHashCode(MxGeneratorAdapter visitor) {
        Label labelFalse = new Label();
        Label next = new Label();

        visitor.ifZCmp(EQ, labelFalse);

        visitor.push(TRUE_HASHCODE);
        visitor.goTo(next);

        visitor.mark(labelFalse);
        visitor.push(FALSE_HASHCODE);

        visitor.mark(next);
    }

    private static void generateLongHashCode(MxGeneratorAdapter visitor) {
        visitor.dup2();
        visitor.push(INT_BITS);
        visitor.visitInsn(LUSHR);
        visitor.visitInsn(LXOR);
        visitor.cast(LONG_TYPE, INT_TYPE);
    }

    private static void generateToString(Type[] types, Type tupleType, MxGeneratorAdapter visitor) {
        visitor.start();
        visitor.createWithDefaultConstructor(STRING_BUILDER_TYPE);
        String s = getTupleName(types) + "(";
        generateConstAppend(visitor, s);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            visitor.loadThis();
            visitor.getField(tupleType, "$" + i, type);
            generateAppend(visitor, type);
            if (i < types.length - 1) {
                generateConstAppend(visitor, ", ");
            }
        }
        generateConstAppend(visitor, ")");
        visitor.invokeToString();
        visitor.returnValue();
        visitor.endMethod();
    }

    private static void generateConstAppend(MxGeneratorAdapter visitor, String s) {
        visitor.push(s);
        generateObjectAppend(visitor);
    }

    private static void generateAppend(MxGeneratorAdapter visitor, Type type) {
        switch (type.getSort()) {
            case OBJECT:
            case ARRAY:
                generateObjectAppend(visitor);
                break;
            case BYTE:
            case SHORT:
            case INT:
                generateIntAppend(visitor);
                break;
            default:
                visitor.invokeVirtual(STRING_BUILDER_TYPE, new Method("append", STRING_BUILDER_TYPE, new Type[] {type}));
        }
    }

    private static void generateIntAppend(MxGeneratorAdapter visitor) {
        visitor.invokeVirtual(STRING_BUILDER_TYPE, APPEND_INT_METHOD);
    }

    private static void generateObjectAppend(MxGeneratorAdapter visitor) {
        visitor.invokeVirtual(STRING_BUILDER_TYPE, APPEND_OBJECT_METHOD);
    }

    private static String getTupleName(Type[] types) {
        StringBuilder b = new StringBuilder("Tuple<");
        for (Type type : types) {
            b.append(type.getClassName()).append(", ");
        }
        b.setLength(b.length() - 2);
        b.append(">");
        return b.toString();
    }

    private static void generateGet(Type[] types, Type tupleType, MxGeneratorAdapter visitor) {
        visitor.start();
        Label[] labels = createLabels(types.length);
        Label invalidArgument = new Label();
        visitor.loadArg(0);
        visitor.visitTableSwitchInsn(0, types.length - 1, invalidArgument, labels);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            visitor.mark(labels[i]);
            visitor.loadThis();
            visitor.getField(tupleType, "$" + i, type);
            visitor.valueOf(type);
            visitor.returnValue();
        }
        visitor.mark(invalidArgument);
        generateInvalidTupleIndex(visitor);
        visitor.endMethod();
    }

    private static final Generator[][] CONVERTERS = new Generator[Type.DOUBLE + 1][Type.DOUBLE + 1];
    static {
        addCast(Type.BYTE_TYPE, Type.CHAR_TYPE);
        addCast(Type.BYTE_TYPE, Type.SHORT_TYPE);
        addCast(Type.BYTE_TYPE, Type.INT_TYPE);
        addCast(Type.BYTE_TYPE, Type.LONG_TYPE);

        addCast(Type.SHORT_TYPE, Type.CHAR_TYPE);
        addCast(Type.SHORT_TYPE, Type.INT_TYPE);
        addCast(Type.SHORT_TYPE, Type.LONG_TYPE);

        addCast(Type.CHAR_TYPE, Type.SHORT_TYPE);
        addCast(Type.CHAR_TYPE, Type.INT_TYPE);
        addCast(Type.CHAR_TYPE, Type.LONG_TYPE);

        addCast(Type.INT_TYPE, Type.LONG_TYPE);

        addCast(Type.FLOAT_TYPE, Type.DOUBLE_TYPE);
    }

    private static void addCast(Type from, Type to) {
        CONVERTERS[from.getSort()][to.getSort()] = new CastConverter(from, to);
    }

    private static void generateGetPrimitive(Type[] types, Type tupleType, Type primType, MxGeneratorAdapter visitor) {
        visitor.start();
        Label invalidArgument = new Label();
        visitor.loadArg(0);

        Label[] labels = new Label[types.length];
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type == primType || (type.getSort() <= Type.DOUBLE && CONVERTERS[type.getSort()][primType.getSort()] != null)) {
                labels[i] = new Label();
            } else {
                labels[i] = invalidArgument;
            }
        }
        visitor.visitTableSwitchInsn(0, types.length - 1, invalidArgument, labels);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type == primType) {
                visitor.mark(labels[i]);
                visitor.loadThis();
                visitor.getField(tupleType, "$" + i, type);
                visitor.returnValue();
            } else if (type.getSort() <= Type.DOUBLE) {
                Generator c = CONVERTERS[type.getSort()][primType.getSort()];
                if (c != null) {
                    visitor.mark(labels[i]);
                    visitor.loadThis();
                    visitor.getField(tupleType, "$" + i, type);
                    c.generate(visitor);
                    visitor.returnValue();
                } else {
                    assert labels[i] == invalidArgument;
                }
            } else {
                assert labels[i] == invalidArgument;
            }
        }
        visitor.mark(invalidArgument);
        generateInvalidTupleIndex(visitor);
        visitor.endMethod();
    }

    private static Label[] createLabels(int n) {
        Label[] labels = new Label[n];
        for (int i = 0; i < n; i++) {
            labels[i] = new Label();
        }
        return labels;
    }

    private static void generateInvalidTupleIndex(MxGeneratorAdapter visitor) {
        visitor.newInstance(ILLEGAL_ARGUMENT_EXCEPTION_TYPE);
        visitor.dup();
        generateInvalidArgumentMessage(visitor);
        visitor.invokeConstructor(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, INIT_EXCEPTION_METHOD);
        visitor.throwException();
    }

    private static void generateInvalidArgumentMessage(MxGeneratorAdapter visitor) {
        visitor.createWithDefaultConstructor(STRING_BUILDER_TYPE);
        generateConstAppend(visitor, "Tuple ");
        visitor.loadThis();
        generateObjectAppend(visitor);
        generateConstAppend(visitor, " doesn't contain element with index ");
        visitor.loadArg(0);
        generateIntAppend(visitor);
        visitor.invokeToString();
    }

    private static String generateTupleClassName(Type[] types) {
        StringBuilder b = new StringBuilder(TUPLE_TYPE.getInternalName()).append("$");
        for (Type type : types) {
            if (isReferenceType(type)) {
                b.append('A');
            } else {
                b.append(type.getDescriptor());
            }
        }
        return b.toString();
    }

    private static class TupleFactoryImpl implements TupleFactory {
        private final Constructor<? extends Tuple> ctor;
        private final HashingStrategy[] hs;

        TupleFactoryImpl(HashingStrategy[] hs, Constructor<? extends Tuple> ctor) {
            this.hs = hs;
            this.ctor = ctor;
        }

        @Override
        public Tuple create(Object... values) {
            try {
                Object[] args = new Object[values.length + 1];
                System.arraycopy(values, 0, args, 1, values.length);
                args[0] = hs;
                return ctor.newInstance(args);
            } catch (InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Class<? extends Tuple> getTupleClass() {
            return ctor.getDeclaringClass();
        }
    }

    private static class CastConverter extends Generator {
        private final Type from;
        private final Type to;

        public CastConverter(Type from, Type to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void generate(MxGeneratorAdapter mv) {
            mv.cast(from, to);
        }
    }
}
