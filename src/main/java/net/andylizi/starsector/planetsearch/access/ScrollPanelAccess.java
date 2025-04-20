/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class ScrollPanelAccess {
    private final Class<? extends ScrollPanelAPI> scrollPanelType;
    private final MethodHandle m_getContentContainer;

    @SuppressWarnings("unchecked")
    public ScrollPanelAccess(Class<? extends ScrollPanelAPI> subclass) throws ReflectiveOperationException {
        Method method = subclass.getMethod("getContentContainer");
        method.trySetAccessible();
        this.scrollPanelType = (Class<? extends ScrollPanelAPI>) method.getDeclaringClass();
        this.m_getContentContainer = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends ScrollPanelAPI> scrollPanelType() {
        return scrollPanelType;
    }

    public UIPanelAPI getContentContainer(UIPanelAPI panel) {
        try {
            return (UIPanelAPI) this.m_getContentContainer.invoke(panel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
