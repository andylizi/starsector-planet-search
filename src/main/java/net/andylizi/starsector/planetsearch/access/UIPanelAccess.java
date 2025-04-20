/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

public class UIPanelAccess {
    private final Class<? extends UIPanelAPI> uiPanelType;
    private final MethodHandle m_getChildrenNonCopy;
    private final MethodHandle m_remove;

    @SuppressWarnings("unchecked")
    public UIPanelAccess(Class<? extends UIPanelAPI> subclass) throws ReflectiveOperationException {
        Method method = subclass.getMethod("getChildrenNonCopy");
        method.trySetAccessible();
        this.uiPanelType = (Class<? extends UIPanelAPI>) method.getDeclaringClass();
        this.m_getChildrenNonCopy = MethodHandles.publicLookup().unreflect(method);

        MethodHandle m_remove = null;
        for (Method m : uiPanelType.getMethods()) {
            Class<?>[] paramTypes;
            if ("remove".equals(m.getName()) && !m.isVarArgs() &&
                    (paramTypes = m.getParameterTypes()).length == 1 &&
                    !paramTypes[0].isArray()) {
                method.trySetAccessible();
                m_remove = MethodHandles.publicLookup().unreflect(m);
                break;
            }
        }
        if (m_remove == null) throw new NoSuchMethodException("remove(UIComponent) in UIPanel " + uiPanelType);
        this.m_remove = m_remove;
    }

    public Class<? extends UIPanelAPI> uiPanelType() {
        return uiPanelType;
    }

    @SuppressWarnings("unchecked")
    public List<UIComponentAPI> getChildrenNonCopy(UIPanelAPI panel) {
        try {
            return (List<UIComponentAPI>) this.m_getChildrenNonCopy.invoke(panel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void remove(UIPanelAPI panel, UIComponentAPI child) {
        try {
            this.m_remove.invoke(panel, child);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
