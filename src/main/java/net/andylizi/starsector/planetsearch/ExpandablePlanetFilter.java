package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.campaign.ui.intel.PlanetFilter;
import com.fs.starfarer.campaign.ui.intel.PlanetsPanel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An expandable planet filter that supports {@link PlanetFilterPlugin filter plugins}.
 * <p>
 * Note: <code>com.fs.starfarer.campaign.ui.intel.{PlanetFilter,PlanetsPanel}</code> are placeholder types.
 * They don't exist at runtime because it's obfuscated. When loading this class,
 * we need to replace all references to them with actual ones. The is done in {@link ClassConstantTransformer}.
 */
public class ExpandablePlanetFilter extends PlanetFilter {
    private final List<PlanetFilterPlugin> plugins;

    public ExpandablePlanetFilter(PlanetsPanel parent) {
        this(parent, null);
    }

    public ExpandablePlanetFilter(PlanetsPanel parent, @Nullable List<PlanetFilterPlugin> plugins) {
        super(parent);
        this.plugins = new ArrayList<>(1);
        if (plugins != null) this.plugins.addAll(plugins);
    }

    public List<PlanetFilterPlugin> getPlugins() {
        return Collections.unmodifiableList(this.plugins);
    }

    public void addPlugin(PlanetFilterPlugin plugin) {
        this.plugins.add(Objects.requireNonNull(plugin));
    }

    public boolean removePlugin(PlanetFilterPlugin plugin) {
        return this.plugins.remove(Objects.requireNonNull(plugin));
    }

    @Override
    protected void afterSizeFirstChanged(float width, float height) {
        super.afterSizeFirstChanged(width, height);
        for (PlanetFilterPlugin plugin : this.plugins) {
            plugin.afterSizeFirstChanged(this, width, height);
        }
    }

    @Override
    public List<SectorEntityToken> getFilteredList(List<SectorEntityToken> list) {
        list = super.getFilteredList(list);
        for (PlanetFilterPlugin plugin : this.plugins) {
            list = plugin.getFilteredList(this, list);
        }
        return list;
    }
}
