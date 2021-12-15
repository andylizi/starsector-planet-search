package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.campaign.ui.intel.PlanetFilter;
import com.fs.starfarer.campaign.ui.intel.PlanetsPanel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
