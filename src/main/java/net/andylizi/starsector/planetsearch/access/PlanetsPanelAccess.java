/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIPanelAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodType.methodType;

public class PlanetsPanelAccess {
    private final Class<? extends UIPanelAPI> planetsPanelType;
    private final MethodHandle m_getPlanetList2;
    private final MethodHandle m_createUI;
    private final MethodHandle f_planetFilterPanel_get;
    private final MethodHandle f_planetFilterPanel_set;

    public PlanetsPanelAccess(Class<? extends UIPanelAPI> planetsPanelType) throws ReflectiveOperationException {
        this.planetsPanelType = planetsPanelType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Method method = planetsPanelType.getMethod("getPlanetList2");
        method.trySetAccessible();
        this.m_getPlanetList2 = lookup.unreflect(method);
        this.m_createUI = lookup.findVirtual(planetsPanelType, "createUI", methodType(void.class));

        Field planetFilterPanelField = null;
        for (Field f : planetsPanelType.getDeclaredFields()) {
            Class<?> type = f.getType();
            if (UIPanelAPI.class.isAssignableFrom(type)) {
                try {
                    type.getDeclaredMethod("getFilteredPlanets");
                    planetFilterPanelField = f;
                } catch (NoSuchMethodException ignored) {
                }
            }
        }

        if (planetFilterPanelField == null)
            throw new NoSuchFieldException(
                    "failed to locate PlanetFilterPanel field in " + planetsPanelType.getName());

        planetFilterPanelField.setAccessible(true);
        this.f_planetFilterPanel_get = lookup.unreflectGetter(planetFilterPanelField);
        this.f_planetFilterPanel_set = lookup.unreflectSetter(planetFilterPanelField);
    }

    public Class<? extends UIPanelAPI> planetsPanelType() {
        return planetsPanelType;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends UIPanelAPI> planetsListType() {
        return (Class<? extends UIPanelAPI>) m_getPlanetList2.type().returnType();
    }

    @SuppressWarnings("unchecked")
    public Class<? extends UIPanelAPI> planetsFilterPanelType() {
        return (Class<? extends UIPanelAPI>) f_planetFilterPanel_get.type().returnType();
    }

    public UIPanelAPI getPlanetList(UIPanelAPI planetsPanel) {
        try {
            return (UIPanelAPI) this.m_getPlanetList2.invoke(planetsPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void createUI(UIPanelAPI planetsPanel) {
        try {
            this.m_createUI.invoke(planetsPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public UIPanelAPI getPlanetFilterPanelPanel(UIPanelAPI planetsPanel) {
        try {
            return (UIPanelAPI) this.f_planetFilterPanel_get.invoke(planetsPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setPlanetFilterPanel(UIPanelAPI planetsPanel, @Nullable UIPanelAPI planetFilterPanel) {
        try {
            this.f_planetFilterPanel_set.invoke(planetsPanel, planetFilterPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
