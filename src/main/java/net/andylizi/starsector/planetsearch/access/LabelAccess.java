package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.LabelAPI;

import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LabelAccess {
    private final Class<? extends LabelAPI> labelType;
    private final MethodHandle m_createWithColor;
    private final MethodHandle m_setOpacity;

    public LabelAccess(Class<? extends LabelAPI> labelType) throws ReflectiveOperationException {
        this.labelType = labelType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        this.m_createWithColor = lookup
                .findStatic(labelType, "create", MethodType.methodType(labelType, String.class, Color.class));
        this.m_setOpacity = lookup
                .findVirtual(labelType, "setOpacity", MethodType.methodType(void.class, float.class));
    }

    public Class<? extends LabelAPI> labelType() {
        return labelType;
    }

    public LabelAPI create(String text, Color color) {
        try {
            return (LabelAPI) this.m_createWithColor.invoke(text, color);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setOpacity(LabelAPI label, float opacity) {
        try {
            this.m_setOpacity.invoke(label, opacity);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
