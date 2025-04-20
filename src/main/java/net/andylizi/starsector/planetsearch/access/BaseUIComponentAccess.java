/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class BaseUIComponentAccess {
    private final Class<? extends UIComponentAPI> baseUIComponentType;
    private final MethodHandle m_getParent;

    @SuppressWarnings("unchecked")
    public BaseUIComponentAccess(Class<? extends UIComponentAPI> subclass) throws ReflectiveOperationException {
        var method = ReflectionUtil.getMethodByName(subclass, "getParent");
        ReflectionUtil.trySetAccessible(method);
        this.m_getParent = MethodHandles.publicLookup().unreflect(method);
        this.baseUIComponentType = (Class<? extends UIComponentAPI>) method.getDeclaringClass();
    }

    public Class<? extends UIComponentAPI> baseUIComponentType() {
        return baseUIComponentType;
    }

    public UIPanelAPI getParent(UIComponentAPI self) {
        try {
            return (UIPanelAPI) this.m_getParent.invoke(self);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
