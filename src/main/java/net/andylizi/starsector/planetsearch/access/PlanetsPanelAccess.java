package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PlanetsPanelAccess {
    private final Class<? extends UIPanelAPI> planetsPanelType;
    private final MethodHandle m_getPlanetList;
    private final MethodHandle f_planetFilter_get;
    private final MethodHandle f_planetFilter_set;

    public PlanetsPanelAccess(Class<? extends UIPanelAPI> planetsPanelType) throws ReflectiveOperationException {
        this.planetsPanelType = planetsPanelType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Method method = planetsPanelType.getMethod("getPlanetList");
        ReflectionUtil.trySetAccessible(method);
        this.m_getPlanetList = lookup.unreflect(method);

        Class<?> planetFilterType = planetsPanelType.getMethod("getPlanetFilter").getReturnType();
        Field field = ReflectionUtil.getFirstDeclaredFieldByType(planetsPanelType, planetFilterType);
        field.setAccessible(true);
        this.f_planetFilter_get = lookup.unreflectGetter(field);
        this.f_planetFilter_set = lookup.unreflectSetter(field);
    }

    public Class<? extends UIPanelAPI> planetsPanelType() {
        return planetsPanelType;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends UIPanelAPI> planetFilterType() {
        return (Class<? extends UIPanelAPI>) f_planetFilter_get.type().returnType();
    }

    @SuppressWarnings("unchecked")
    public Class<? extends UIPanelAPI> sortablePlanetListType() {
        return (Class<? extends UIPanelAPI>) m_getPlanetList.type().returnType();
    }

    public UIPanelAPI getPlanetList(UIPanelAPI planetsPanel) {
        try {
            return (UIPanelAPI) this.m_getPlanetList.invoke(planetsPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public UIPanelAPI getPlanetFilter(UIPanelAPI planetsPanel) {
        try {
            return (UIPanelAPI) this.f_planetFilter_get.invoke(planetsPanel);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setPlanetFilter(UIPanelAPI planetsPanel, @Nullable Object planetFilter) {
        try {
            this.f_planetFilter_set.invoke(planetsPanel, planetFilter);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
