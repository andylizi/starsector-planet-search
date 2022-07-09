package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.*;
import net.andylizi.starsector.planetsearch.access.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.ProtectionDomain;
import java.util.*;

public final class PlanetsPanelInjector {
    private static final float PLACEHOLDER_OPACITY = 0.45f;
    private static final Logger logger = Logger.getLogger(CoreUIWatchScript.class);

    private static PlanetsPanelAccess acc_PlanetsPanel;
    private static SortablePlanetListAccess acc_SortablePlanetList;
    private static UIPanelAccess acc_UIPanel;
    private static TextFieldAccess acc_TextField;
    private static PositionAccess acc_Position;
    private static BaseUIComponentAccess acc_BaseUIComponent;

    private static Class<?> expandablePlanetFilter;
    private static MethodHandle expandablePlanetFilterCtor;

    public static void inject(UIPanelAPI planetsPanel) throws ReflectiveOperationException {
        if (acc_PlanetsPanel == null) acc_PlanetsPanel = new PlanetsPanelAccess(planetsPanel.getClass());
        if (acc_SortablePlanetList == null) acc_SortablePlanetList = new SortablePlanetListAccess(
                acc_PlanetsPanel.sortablePlanetListType(), acc_PlanetsPanel.planetFilterType());
        if (acc_UIPanel == null) acc_UIPanel = new UIPanelAccess(planetsPanel.getClass());
        if (acc_BaseUIComponent == null) acc_BaseUIComponent = new BaseUIComponentAccess(planetsPanel.getClass());

        if (expandablePlanetFilter == null) {
            try {
                String className = PlanetsPanelInjector.class.getPackage().getName().concat(".ExpandablePlanetFilter");
                expandablePlanetFilter = loadCustomFilterClass(className,
                        acc_PlanetsPanel.planetFilterType(), acc_PlanetsPanel.planetsPanelType());
            } catch (IOException | IllegalClassFormatException | NullPointerException e) {
                logger.error("Failed to transform SearchablePlanetFilter", e);
                return;
            }

            expandablePlanetFilterCtor = MethodHandles.lookup().findConstructor(expandablePlanetFilter,
                    MethodType.methodType(void.class, acc_PlanetsPanel.planetsPanelType(), List.class));
        }

        final UIPanelAPI planetList = acc_PlanetsPanel.getPlanetList(planetsPanel);
        UIPanelAPI oldFilter = acc_PlanetsPanel.getPlanetFilter(planetsPanel);
        if (oldFilter == null || oldFilter.getClass() == expandablePlanetFilter) return;

        class SearchBoxHandler implements InvocationHandler {
            TextFieldAPI textField;
            LabelAPI placeholder;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if ("charTyped".equals(methodName) || "backspacePressed".equals(methodName)) {
                    updateSearchBox(planetList, textField, placeholder);
                }
                return method.getDeclaringClass() == Object.class ? method.invoke(this, args) : null;
            }
        }

        final TextFieldAPI textField = Global.getSettings().createTextField("", Fonts.DEFAULT_SMALL);
        textField.setUndoOnEscape(false);

        if (acc_TextField == null) acc_TextField = new TextFieldAccess(textField.getClass());
        SearchBoxHandler handler = new SearchBoxHandler();
        Object listener = Proxy.newProxyInstance(SearchBoxHandler.class.getClassLoader(),
                new Class<?>[] { acc_TextField.textListenerType() }, handler);
        acc_TextField.setTextListener(textField, listener);

        String placeholderText = Global.getSettings().getString("planetSearch", "searchboxPlaceholder");
        if (placeholderText == null) placeholderText = "Search...";

        final LabelAPI placeholder = Global.getSettings().createLabel(placeholderText, Fonts.DEFAULT_SMALL);
        placeholder.setOpacity(PLACEHOLDER_OPACITY);
        ((UIPanelAPI) textField).addComponent((UIComponentAPI) placeholder).inLMid(2f * 2f - 1f); // Just above the cursor

        handler.textField = textField;
        handler.placeholder = placeholder;

        class SearchBoxFilterPlugin implements PlanetFilterPlugin {
            @Override
            public void afterSizeFirstChanged(UIPanelAPI panel, float width, float height) {
                List<UIComponentAPI> children = acc_UIPanel.getChildrenNonCopy(panel);
                UIComponentAPI last = children.get(children.size() - 1);
                panel.addComponent((UIComponentAPI) textField).setSize(width, 25f).belowLeft(last, 20f);
            }

            @Override
            public List<SectorEntityToken> getFilteredList(UIPanelAPI panel, List<SectorEntityToken> list) {
                String search = textField.getText().trim().toLowerCase();
                if (search.isEmpty()) return list;

                List<SectorEntityToken> filtered = new ArrayList<>(list.size());
                for (SectorEntityToken entity : list) {
                    if (entity.getName().toLowerCase().contains(search)) {
                        filtered.add(entity);
                    }
                }
                return filtered;
            }
        }

        UIPanelAPI filter;
        try {
            filter = (UIPanelAPI) expandablePlanetFilterCtor.invoke(planetsPanel,
                    Collections.singletonList(new SearchBoxFilterPlugin()));
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }

        PositionAPI oldPosition = oldFilter.getPosition();
        if (acc_Position == null) acc_Position = new PositionAccess(oldPosition.getClass());
        acc_Position.set(filter.getPosition(), oldPosition);

        acc_PlanetsPanel.setPlanetFilter(planetsPanel, filter);
        acc_SortablePlanetList.setPlanetFilter(planetList, filter);
        acc_UIPanel.remove(planetsPanel, oldFilter);
        planetsPanel.addComponent(filter);

        float opacity = acc_BaseUIComponent.getOpacity(oldFilter);
        acc_BaseUIComponent.setOpacity(filter, opacity);
    }

    private static void updateSearchBox(UIPanelAPI planetList, TextFieldAPI textBox, LabelAPI placeholder) {
        placeholder.setOpacity(textBox.getText().trim().isEmpty() ? PLACEHOLDER_OPACITY : 0f);
        acc_SortablePlanetList.recreateList(planetList, null);
    }

    /**
     * @see ExpandablePlanetFilter
     */
    private static Class<?> loadCustomFilterClass(
            String className,
            Class<?> planetFilterType,
            Class<?> planetsPanelType
    )
            throws IOException, IllegalClassFormatException, ReflectiveOperationException {
        String planetFilterFrom = "com/fs/starfarer/campaign/ui/intel/PlanetFilter",
                planetsPanelFrom = "com/fs/starfarer/campaign/ui/intel/PlanetsPanel";
        String planetFilterTo = planetFilterType.getName().replace('.', '/'),
                planetsPanelTo = planetsPanelType.getName().replace('.', '/');

        ClassLoader cl = PlanetsPanelInjector.class.getClassLoader();
        byte[] data = ClassConstantTransformer.readClassBuffer(cl, className);
        data = new ClassConstantTransformer(Arrays.asList(
                ClassConstantTransformer.newTransform(planetFilterFrom, planetFilterTo),
                ClassConstantTransformer.newTransform(planetsPanelFrom, planetsPanelTo)
        )).apply(data);

        // We can't just cast to CustomClassLoader because that class is loaded by another class loader
        MethodHandle m = MethodHandles.lookup().findVirtual(cl.getClass(), "defineMyClass", MethodType.methodType(
                Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class));
        try {
            return (Class<?>) m.invoke(cl,
                    className, data, 0, data.length, PlanetsPanelInjector.class.getProtectionDomain());
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    private PlanetsPanelInjector() {throw new AssertionError();}
}
