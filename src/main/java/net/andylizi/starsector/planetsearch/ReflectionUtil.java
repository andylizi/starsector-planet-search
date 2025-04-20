/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

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

    public static Method getMethodByName(Class<?> owner, String name) throws NoSuchMethodException {
        Method result = null;
        for (Method m : owner.getMethods()) {
            if (name.equals(m.getName()))
                if (result != null) {
                    throw new NoSuchMethodException("more than one method named " + name + " in " + owner.getName());
                } else {
                    result = m;
                }
        }
        if (result == null) throw new NoSuchMethodException("method named " + name + " in " + owner.getName());
        return result;
    }

    private ReflectionUtil() {
        throw new AssertionError();
    }
}
