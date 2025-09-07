/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.PlanetSearchData;
import com.fs.starfarer.api.ui.*;
import net.andylizi.starsector.planetsearch.access.*;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class PlanetsPanelInjector {
    private static final Logger logger = Logger.getLogger(PlanetsPanelInjector.class);

    private static PlanetsPanelAccess acc_PlanetsPanel;
    private static PlanetFilterPanelAccess acc_PlanetFilterPanel;
    private static BaseUIComponentAccess acc_BaseUIComponent;
    private static UIPanelAccess acc_UIPanel;
    private static TextFieldAccess acc_TextField;
    private static PositionAccess acc_Position;

    private static MethodHandle newSearchableFilterPanel;

    public static void inject(UIPanelAPI planetsPanel) throws ReflectiveOperationException {
        if (acc_PlanetsPanel == null) acc_PlanetsPanel = new PlanetsPanelAccess(planetsPanel.getClass());
        if (acc_PlanetFilterPanel == null) acc_PlanetFilterPanel = new PlanetFilterPanelAccess(
                acc_PlanetsPanel.planetsFilterPanelType());
        if (acc_UIPanel == null) acc_UIPanel = new UIPanelAccess(planetsPanel.getClass());
        if (acc_BaseUIComponent == null) acc_BaseUIComponent = new BaseUIComponentAccess(planetsPanel.getClass());
        if (acc_Position == null) acc_Position = new PositionAccess(planetsPanel.getPosition().getClass());

        acc_PlanetsPanel.createUI(planetsPanel);
        UIPanelAPI planetList = acc_PlanetsPanel.getPlanetList(planetsPanel);
        UIPanelAPI oldFilterPanel = acc_PlanetsPanel.getPlanetFilterPanel(planetsPanel);
        if (planetList == null || oldFilterPanel == null)
            throw new NullPointerException("fields was not initialized by createUI()");

        if (newSearchableFilterPanel == null) {
            newSearchableFilterPanel =
                    SearchablePlanetFilterPanelFactory.create(acc_PlanetsPanel.planetsFilterPanelType(),
                            acc_PlanetsPanel.planetsListType(), acc_Position.positionType());
        } else if (newSearchableFilterPanel.type().returnType().isAssignableFrom(oldFilterPanel.getClass())) {
            return; // Already injected
        }

        UIPanelAPI newFilterPanel;
        try {
            var oldPanelPos = oldFilterPanel.getPosition();
            newFilterPanel = (UIPanelAPI) newSearchableFilterPanel.invoke(
                    oldPanelPos.getWidth(), oldPanelPos.getHeight(), planetList);
        } catch (RuntimeException | Error | ReflectiveOperationException e) {
            throw e;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }

        acc_Position.set(newFilterPanel.getPosition(), oldFilterPanel.getPosition());
        acc_PlanetsPanel.setPlanetFilterPanel(planetsPanel, newFilterPanel);
        acc_BaseUIComponent.setOpacity(newFilterPanel, acc_BaseUIComponent.getOpacity(oldFilterPanel));
        acc_UIPanel.remove(planetsPanel, oldFilterPanel);
        planetsPanel.addComponent(newFilterPanel);
    }

    private static final String SEARCH_FILTER_PARAM_KEY = "search_term";

    static final PlanetSearchData.PlanetFilter SEARCH_FILTER = new PlanetSearchData.PlanetFilter() {
        @Override
        public boolean accept(SectorEntityToken entity, Map<String, String> params) {
            var search = params.get(SEARCH_FILTER_PARAM_KEY);
            return search == null || entity.getName().toLowerCase().contains(search);
        }

        @Override
        public boolean shouldShow() {
            return true;
        }

        @Override
        public void createTooltip(TooltipMakerAPI info, float width, String param) {
        }
    };

    static TextFieldAPI __injectFilterPanel(UIPanelAPI self) throws ReflectiveOperationException {
        var labels = new TreeMap<LabelAPI, UIPanelAPI>((a, b) ->
                Float.compare(a.getPosition().getY(), b.getPosition().getY()));
        collectTopLevelLabels(self, labels);

        var label = labels.firstKey();
        var parent = labels.firstEntry().getValue();

        class SearchBoxHandler implements InvocationHandler {
            TextFieldAPI textField;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if ("charTyped".equals(methodName) || "backspacePressed".equals(methodName) ||
                        "textChanged".equals(methodName)) {
                    try {
                        acc_PlanetFilterPanel.updatePlanetList(self);
                    } catch (Throwable ignored) {}
                }
                return method.getDeclaringClass() == Object.class ? method.invoke(this, args) : null;
            }
        }

        final TextFieldAPI textField = Global.getSettings().createTextField("", Fonts.DEFAULT_SMALL);
        textField.setUndoOnEscape(false);

        if (acc_TextField == null) acc_TextField = new TextFieldAccess(textField.getClass());
        var handler = new SearchBoxHandler();
        Object listener = Proxy.newProxyInstance(SearchBoxHandler.class.getClassLoader(),
                new Class<?>[] { acc_TextField.textListenerType() }, handler);
        acc_TextField.setTextListener(textField, listener);

        var hintText = Objects.requireNonNullElse(Global.getSettings().getString("planetSearch",
                "searchHint"), "Search...");
        acc_TextField.setHint(textField, hintText);

        handler.textField = textField;

        var topLabelPos = label.getPosition();
        acc_Position.set(parent.addComponent(textField), topLabelPos);
        textField.getPosition().setYAlignOffset(0);
        topLabelPos.belowLeft(textField, 10);
        return textField;
    }

    private static ScrollPanelAccess acc_ScrollPanel;

    private static void collectTopLevelLabels(UIPanelAPI panel, TreeMap<LabelAPI, UIPanelAPI> labels)
            throws ReflectiveOperationException {
        for (var component : acc_UIPanel.getChildrenNonCopy(panel)) {
            if (component instanceof LabelAPI label) {
                if (acc_Position.getBase(label.getPosition()) == null)
                    labels.put(label, panel);
            } else if (component instanceof ScrollPanelAPI scrollPanel) {
                if (acc_ScrollPanel == null) acc_ScrollPanel = new ScrollPanelAccess(scrollPanel.getClass());
                collectTopLevelLabels(acc_ScrollPanel.getContentContainer(scrollPanel), labels);
            }
        }
    }

    static void __hook_getParams(Map<String, String> params, TextFieldAPI searchBox) {
        String term;
        if (searchBox != null && !(term = searchBox.getText().trim().toLowerCase()).isEmpty()) {
            params.put(SEARCH_FILTER_PARAM_KEY, term);
        }
    }

    static void __hook_syncWithParams(TextFieldAPI searchBox, Map<String, String> params) {
        if (searchBox == null) return;
        String term;
        searchBox.setText((params != null && (term = params.get(SEARCH_FILTER_PARAM_KEY)) != null) ? term : "");
    }

    private PlanetsPanelInjector() {throw new AssertionError();}
}
