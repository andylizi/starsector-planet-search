/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public final class PlanetFilterPanelAccess {
    private final Class<? extends UIPanelAPI> planetsFilterPanelType;
    private final MethodHandle m_updatePlanetList;

    public PlanetFilterPanelAccess(Class<? extends UIPanelAPI> planetsFilterPanelType) throws ReflectiveOperationException {
        this.planetsFilterPanelType = planetsFilterPanelType;
        this.m_updatePlanetList = MethodHandles.publicLookup()
                .findVirtual(planetsFilterPanelType, "updatePlanetList", methodType(void.class));
    }

    public Class<? extends UIPanelAPI> planetsFilterPanelType() {
        return planetsFilterPanelType;
    }

    public void updatePlanetList(UIPanelAPI self) {
        try {
            this.m_updatePlanetList.invoke(self);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
