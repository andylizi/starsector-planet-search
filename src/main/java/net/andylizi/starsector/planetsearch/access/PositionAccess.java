/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.PositionAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public class PositionAccess {
    private final Class<? extends PositionAPI> positionType;
    private final MethodHandle m_set;
    private final MethodHandle m_getBase;

    public PositionAccess(Class<? extends PositionAPI> positionType) throws ReflectiveOperationException {
        this.positionType = positionType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        this.m_set = lookup.findVirtual(positionType, "set", methodType(void.class, positionType));
        this.m_getBase = lookup.findVirtual(positionType, "getBase", methodType(positionType));
    }

    public Class<? extends PositionAPI> positionType() {
        return positionType;
    }

    public void set(PositionAPI base, PositionAPI target) {
        try {
            this.m_set.invoke(base, target);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public PositionAPI getBase(PositionAPI self) {
        try {
            return (PositionAPI) this.m_getBase.invoke(self);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
