/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {
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
