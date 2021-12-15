package net.andylizi.starsector.planetsearch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtil {
    private static final MethodHandle TRY_SET_ACCESSIBLE;

    static {
        MethodHandle trySetAccessible = null;
        //noinspection RedundantSuppression
        try {
            //noinspection JavaLangInvokeHandleSignature
            trySetAccessible = MethodHandles.lookup()
                .findVirtual(AccessibleObject.class, "trySetAccessible", MethodType.methodType(boolean.class));
        } catch (NoSuchMethodException ignored) {
            // Below Java 9
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
        TRY_SET_ACCESSIBLE = trySetAccessible;
    }

    public static boolean trySetAccessible(AccessibleObject object) {
        try {
            if (TRY_SET_ACCESSIBLE != null) {
                return (boolean) TRY_SET_ACCESSIBLE.invokeExact(object);
            } else {
                object.setAccessible(true);
                return true;
            }
        } catch (SecurityException ex) {
            return false;
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }

    public static Field getFirstDeclaredFieldByType(Class<?> owner, Class<?> type) throws NoSuchFieldException {
        for (Field f : owner.getDeclaredFields()) {
            if (type == f.getType()) {
                return f;
            }
        }
        throw new NoSuchFieldException("field with type " + type.getName() + " in " + owner);
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getFirstConstructorByParameterCount(Class<T> type, int count) throws
            NoSuchMethodException {
        for (Constructor<?> ctor : type.getConstructors()) {
            if (ctor.getParameterTypes().length == count)
                return (Constructor<T>) ctor;
        }
        throw new NoSuchMethodException("constructor with " + count + " parameters in " + type);
    }

    private ReflectionUtil() {
        throw new AssertionError();
    }
}
