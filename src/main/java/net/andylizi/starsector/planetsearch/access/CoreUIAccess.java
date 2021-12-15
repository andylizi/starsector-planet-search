package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;
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
        ReflectionUtil.trySetAccessible(method);
        this.m_getButtons = MethodHandles.publicLookup().unreflect(method);

        method = coreUIType.getMethod("getCurrentTab");
        ReflectionUtil.trySetAccessible(method);
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
