package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;

public class UIPanelAccess {
    private final Class<? extends UIPanelAPI> uiPanelType;
    private final MethodHandle m_getChildrenNonCopy;
    private final MethodHandle m_remove;

    @SuppressWarnings("unchecked")
    public UIPanelAccess(Class<? extends UIPanelAPI> subclass) throws ReflectiveOperationException {
        Method method = subclass.getMethod("getChildrenNonCopy");
        ReflectionUtil.trySetAccessible(method);
        this.uiPanelType = (Class<? extends UIPanelAPI>) method.getDeclaringClass();
        this.m_getChildrenNonCopy = MethodHandles.publicLookup().unreflect(method);

        MethodHandle m_remove = null;
        for (Method m : uiPanelType.getMethods()) {
            Class<?>[] paramTypes;
            if ("remove".equals(m.getName()) && !m.isVarArgs() &&
                (paramTypes = m.getParameterTypes()).length == 1 &&
                !paramTypes[0].isArray()) {
                ReflectionUtil.trySetAccessible(m);
                m_remove = MethodHandles.publicLookup().unreflect(m);
                break;
            }
        }
        if (m_remove == null) throw new NoSuchMethodException("remove(UIComponent) in UIPanel " + uiPanelType);
        this.m_remove = m_remove;
    }

    public Class<? extends UIPanelAPI> uiPanelType() {
        return uiPanelType;
    }

    @SuppressWarnings("unchecked")
    public List<UIComponentAPI> getChildrenNonCopy(UIPanelAPI panel) {
        try {
            return (List<UIComponentAPI>) this.m_getChildrenNonCopy.invoke(panel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void remove(UIPanelAPI panel, UIComponentAPI child) {
        try {
            this.m_remove.invoke(panel, child);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
