/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class IntelPanelAccess {
    private final Class<? extends UIPanelAPI> intelPanelType;
    private final MethodHandle m_getPlanetsPanel;

    public IntelPanelAccess(Class<? extends UIPanelAPI> intelPanelType) throws ReflectiveOperationException {
        this.intelPanelType = intelPanelType;

        Method method = intelPanelType.getMethod("getPlanetsPanel");
        method.trySetAccessible();
        this.m_getPlanetsPanel = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends UIPanelAPI> intelPanelType() {
        return intelPanelType;
    }

    public UIPanelAPI getPlanetsPanel(UIPanelAPI intelPanel) {
        try {
            return (UIPanelAPI) this.m_getPlanetsPanel.invoke(intelPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
