/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class InteractionDialogAccess {
    private final Class<? extends InteractionDialogAPI> interactionDialogType;
    private final MethodHandle m_getCoreUI;

    public InteractionDialogAccess(Class<? extends InteractionDialogAPI> interactionDialogType)
            throws ReflectiveOperationException {
        this.interactionDialogType = interactionDialogType;

        Method method = interactionDialogType.getMethod("getCoreUI");
        method.trySetAccessible();
        this.m_getCoreUI = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends InteractionDialogAPI> interactionDialogType() {
        return interactionDialogType;
    }

    @Nullable
    public CoreUIAPI getCoreUI(InteractionDialogAPI dialog) {
        try {
            return (CoreUIAPI) this.m_getCoreUI.invoke(dialog);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
