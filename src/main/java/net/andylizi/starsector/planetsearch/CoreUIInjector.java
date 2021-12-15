package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import net.andylizi.starsector.planetsearch.access.ButtonAccess;
import net.andylizi.starsector.planetsearch.access.CoreUIAccess;
import net.andylizi.starsector.planetsearch.access.IntelPanelAccess;
import net.andylizi.starsector.planetsearch.access.TripadButtonPanelAccess;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class CoreUIInjector {
    private static final Logger logger = Logger.getLogger(CoreUIWatchScript.class);

    private static CoreUIAccess acc_CoreUI;
    private static TripadButtonPanelAccess acc_TripadButtonPanel;
    private static ButtonAccess acc_Button;

    public static boolean inject(CoreUIAPI coreUI) throws ReflectiveOperationException {
        if (acc_CoreUI == null) acc_CoreUI = new CoreUIAccess(coreUI.getClass());
        UIPanelAPI buttons = acc_CoreUI.getButtons(coreUI);

        if (acc_TripadButtonPanel == null) acc_TripadButtonPanel = new TripadButtonPanelAccess(buttons.getClass());
        ButtonAPI intelButton = acc_TripadButtonPanel.getButton(buttons, CoreUITabId.INTEL);
        if (intelButton == null) return false;

        if (acc_Button == null) acc_Button = new ButtonAccess(intelButton.getClass());
        Object originalListener = acc_Button.getListener(intelButton);
        if (Proxy.isProxyClass(originalListener.getClass())) {
            return false; // already injected
        }

        IntelButtonHandler handler = new IntelButtonHandler(coreUI, originalListener);
        Object newListener = Proxy.newProxyInstance(originalListener.getClass().getClassLoader(),
                new Class<?>[] { acc_Button.actionListenerType() }, handler);
        acc_Button.setListener(intelButton, newListener);
        logger.info("CoreUI injected");
        return true;
    }

    public static boolean uninject(CoreUIAPI coreUI) {
        if (acc_CoreUI == null || acc_TripadButtonPanel == null || acc_Button == null) return false;

        UIPanelAPI buttons = acc_CoreUI.getButtons(coreUI);
        ButtonAPI intelButton = acc_TripadButtonPanel.getButton(buttons, CoreUITabId.INTEL);
        if (intelButton == null) return false;

        Object listener = acc_Button.getListener(intelButton);
        if (!Proxy.isProxyClass(listener.getClass())) return false;

        InvocationHandler handler = Proxy.getInvocationHandler(listener);
        if (handler instanceof IntelButtonHandler) {
            acc_Button.setListener(intelButton, ((IntelButtonHandler) handler).originalListener);
            logger.info("CoreUI uninjected");
            return true;
        }
        return false;
    }

    private static class IntelButtonHandler implements InvocationHandler {
        final CoreUIAPI coreUI;
        final Object originalListener;

        IntelButtonHandler(@NotNull CoreUIAPI coreUI, @NotNull Object originalListener) {
            this.coreUI = coreUI;
            this.originalListener = originalListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // ActionListener interface only has one method: actionPerformed().
            // In the original listener, a new Intel panel is created and passed to showPanelAsDialog(),
            // which then updates the currentTab. And that's our target here.
            Object result = method.invoke(originalListener, args);
            UIPanelAPI currentTab = acc_CoreUI.getCurrentTab(coreUI);
            if (currentTab != null) {
                injectIntelPanel(currentTab);
            }
            return result;
        }
    }

    private static IntelPanelAccess acc_IntelPanel;

    private static void injectIntelPanel(UIPanelAPI intelPanel) throws ReflectiveOperationException {
        if (acc_IntelPanel == null) acc_IntelPanel = new IntelPanelAccess(intelPanel.getClass());
        UIPanelAPI planetsPanel = acc_IntelPanel.getPlanetsPanel(intelPanel);
        PlanetsPanelInjector.inject(planetsPanel);
    }

    private CoreUIInjector() {throw new AssertionError();}
}
