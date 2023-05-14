/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIComponentAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class BaseUIComponentAccess {
    private final Class<? extends UIComponentAPI> baseUIComponentType;
    private final MethodHandle m_getOpacity;
    private final MethodHandle m_setOpacity;

    @SuppressWarnings("unchecked")
    public BaseUIComponentAccess(Class<? extends UIComponentAPI> subclass) throws ReflectiveOperationException {
        Method method = subclass.getMethod("getOpacity");
        ReflectionUtil.trySetAccessible(method);
        this.m_getOpacity = MethodHandles.publicLookup().unreflect(method);

        method = subclass.getMethod("setOpacity", float.class);
        ReflectionUtil.trySetAccessible(method);
        this.m_setOpacity = MethodHandles.publicLookup().unreflect(method);

        this.baseUIComponentType = (Class<? extends UIComponentAPI>) method.getDeclaringClass();
    }

    public Class<? extends UIComponentAPI> baseUIComponentType() {
        return baseUIComponentType;
    }

    public float getOpacity(UIComponentAPI component) {
        try {
            return (float) this.m_getOpacity.invoke(component);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setOpacity(UIComponentAPI component, float opacity) {
        try {
            this.m_setOpacity.invoke(component, opacity);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
