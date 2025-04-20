/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class CoreUIAccess {
    private final Class<? extends CoreUIAPI> coreUIType;
    private final MethodHandle m_getButtons;
    private final MethodHandle m_getCurrentTab;

    public CoreUIAccess(Class<? extends CoreUIAPI> coreUIType) throws ReflectiveOperationException {
        this.coreUIType = coreUIType;

        Method method = coreUIType.getMethod("getButtons");
        method.trySetAccessible();
        this.m_getButtons = MethodHandles.publicLookup().unreflect(method);

        method = coreUIType.getMethod("getCurrentTab");
        method.trySetAccessible();
        this.m_getCurrentTab = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends CoreUIAPI> coreUIType() {
        return coreUIType;
    }

    public UIPanelAPI getButtons(CoreUIAPI coreUI) {
        try {
            return (UIPanelAPI) this.m_getButtons.invoke(coreUI);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    @Nullable
    public UIPanelAPI getCurrentTab(CoreUIAPI coreUI) {
        try {
            return (UIPanelAPI) this.m_getCurrentTab.invoke(coreUI);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
