package net.andylizi.starsector.planetsearch;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PlanetsPanelInjector {
    private static final Logger logger = Logger.getLogger(CoreUIWatchScript.class);

    private static PlanetsPanelAccess acc_PlanetsPanel;
    private static SortablePlanetListAccess acc_SortablePlanetList;
    private static UIPanelAccess acc_UIPanel;
    private static TextBoxAccess acc_TextBox;
    private static PositionAccess acc_Position;

    private static Class<?> expandablePlanetFilter;
    private static MethodHandle expandablePlanetFilterCtor;

    public static void inject(UIPanelAPI planetsPanel) throws ReflectiveOperationException {
        if (acc_PlanetsPanel == null) acc_PlanetsPanel = new PlanetsPanelAccess(planetsPanel.getClass());
        if (acc_SortablePlanetList == null) acc_SortablePlanetList = new SortablePlanetListAccess(
                acc_PlanetsPanel.sortablePlanetListType(), acc_PlanetsPanel.planetFilterType());
        if (acc_UIPanel == null) acc_UIPanel = new UIPanelAccess(planetsPanel.getClass());
        if (acc_TextBox == null) acc_TextBox = new TextBoxAccess();

        if (expandablePlanetFilter == null) {
            try {
                expandablePlanetFilter = loadCustomFilterClass(acc_PlanetsPanel.planetFilterType(),
                        acc_PlanetsPanel.planetsPanelType());
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

        final UIComponentAPI textBox = acc_TextBox.newInstance("", Fonts.DEFAULT_SMALL, false, null);
        final SortablePlanetListAccess access_PlanetList = acc_SortablePlanetList; // avoid synthetic accessors
        final UIPanelAccess access_UIPanel = acc_UIPanel;
        final TextBoxAccess access_TextBox = acc_TextBox;
        class SearchBoxHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if ("charTyped".equals(methodName) || "backspacePressed".equals(methodName)) {
                    access_PlanetList.recreateList(planetList, null);
                }
                return method.getDeclaringClass() == Object.class ? method.invoke(this, args) : null;
            }
        }

        acc_TextBox.setTextListener(textBox, Proxy.newProxyInstance(SearchBoxHandler.class.getClassLoader(),
                new Class<?>[] { acc_TextBox.textListenerType() }, new SearchBoxHandler()));

        class SearchBoxPlanetFilterPlugin implements PlanetFilterPlugin {
            @Override
            public void afterSizeFirstChanged(UIPanelAPI panel, float width, float height) {
                List<UIComponentAPI> children = access_UIPanel.getChildrenNonCopy(panel);
                UIComponentAPI last = children.get(children.size() - 1);
                panel.addComponent(textBox).setSize(width, 25f).belowLeft(last, 10f);
            }

            @Override
            public List<SectorEntityToken> getFilteredList(UIPanelAPI panel, List<SectorEntityToken> list) {
                String search = access_TextBox.getText(textBox).trim();
                if (search.isEmpty()) return list;

                List<SectorEntityToken> filtered = new ArrayList<>(list.size());
                for (SectorEntityToken entity : list) {
                    if (entity.getName().contains(search)) {
                        filtered.add(entity);
                    }
                }
                return filtered;
            }
        }

        UIPanelAPI filter;
        try {
            filter = (UIPanelAPI) expandablePlanetFilterCtor.invoke(planetsPanel,
                    Collections.singletonList(new SearchBoxPlanetFilterPlugin()));
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
    }

    private static Class<?> loadCustomFilterClass(Class<?> planetFilterType, Class<?> planetsPanelType)
            throws IOException, IllegalClassFormatException, ReflectiveOperationException {
        String planetFilterFrom = "com/fs/starfarer/campaign/ui/intel/PlanetFilter",
                planetsPanelFrom = "com/fs/starfarer/campaign/ui/intel/PlanetsPanel";
        String planetFilterTo = planetFilterType.getName().replace('.', '/'),
                planetsPanelTo = planetsPanelType.getName().replace('.', '/');

        ClassLoader cl = PlanetsPanelInjector.class.getClassLoader();
        String className = PlanetsPanelInjector.class.getPackage().getName().concat(".ExpandablePlanetFilter");
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
