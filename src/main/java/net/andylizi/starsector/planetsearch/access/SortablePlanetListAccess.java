/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Comparator;

public class SortablePlanetListAccess {
    private final Class<? extends UIPanelAPI> sortablePlanetListType;
    private final MethodHandle f_planetFilter_set;
    private final MethodHandle m_recreateList;

    public SortablePlanetListAccess(Class<? extends UIPanelAPI> sortablePlanetListType, Class<?> planetFilterType)
            throws ReflectiveOperationException {
        this.sortablePlanetListType = sortablePlanetListType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Field field = ReflectionUtil.getFirstDeclaredFieldByType(sortablePlanetListType, planetFilterType);
        field.setAccessible(true);
        this.f_planetFilter_set = lookup.unreflectSetter(field);
        this.m_recreateList = lookup.findVirtual(sortablePlanetListType, "recreateList",
                MethodType.methodType(void.class, Comparator.class));
    }

    public Class<? extends UIPanelAPI> sortablePlanetListType() {
        return sortablePlanetListType;
    }

    public void setPlanetFilter(UIPanelAPI planetList, @Nullable Object planetFilter) {
        try {
            this.f_planetFilter_set.invoke(planetList, planetFilter);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void recreateList(UIPanelAPI planetList, @Nullable Comparator<?> comparator) {
        try {
            this.m_recreateList.invoke(planetList, comparator);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
