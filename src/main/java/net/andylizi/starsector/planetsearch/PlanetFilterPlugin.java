package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.List;

public interface PlanetFilterPlugin {
    void afterSizeFirstChanged(UIPanelAPI panel, float width, float height);
    List<SectorEntityToken> getFilteredList(UIPanelAPI panel, List<SectorEntityToken> list);
}
