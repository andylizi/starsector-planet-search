package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.PositionAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class PositionAccess {
    private final Class<? extends PositionAPI> positionType;
    private final MethodHandle m_set;

    public PositionAccess(Class<? extends PositionAPI> positionType) throws ReflectiveOperationException {
        this.positionType = positionType;

        this.m_set = MethodHandles.publicLookup()
                .findVirtual(positionType, "set", MethodType.methodType(void.class, positionType));
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
}
