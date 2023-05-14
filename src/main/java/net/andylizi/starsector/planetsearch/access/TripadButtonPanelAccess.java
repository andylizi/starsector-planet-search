/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class TripadButtonPanelAccess {
    private final Class<? extends UIPanelAPI> tripadButtonPanelType;
    private final MethodHandle m_getButton;

    public TripadButtonPanelAccess(Class<? extends UIPanelAPI> tripadButtonPanelType) throws ReflectiveOperationException {
        this.tripadButtonPanelType = tripadButtonPanelType;

        Method method = tripadButtonPanelType.getMethod("getButton", Object.class);
        ReflectionUtil.trySetAccessible(method);
        this.m_getButton = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends UIPanelAPI> tripadButtonPanelType() {
        return tripadButtonPanelType;
    }

    public ButtonAPI getButton(UIPanelAPI panel, Object tabId) {
        try {
                return (ButtonAPI) this.m_getButton.invoke(panel, tabId);
            } catch (RuntimeException | Error ex) {
                throw ex;
            } catch (Throwable t) {
                throw new AssertionError("unreachable", t);
        }
    }
}
