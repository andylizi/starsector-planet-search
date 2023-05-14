package com.fs.starfarer.campaign.ui.intel;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.List;

/**
 * Dummy class to compile against.
 * References to this class will be replaced with the actual obfuscated name at runtime.
 */
@SuppressWarnings("unused")
public class PlanetFilter implements UIPanelAPI {
    public PlanetFilter(PlanetsPanel parent) {
        throw new UnsupportedOperationException("unimplemented");
    }

    protected void afterSizeFirstChanged(float width, float height) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public List<SectorEntityToken> getFilteredList(List<SectorEntityToken> list) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public PositionAPI addComponent(UIComponentAPI custom) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public void removeComponent(UIComponentAPI component) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public void bringComponentToTop(UIComponentAPI c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public PositionAPI getPosition() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public void render(float alphaMult) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public void advance(float amount) {
        throw new UnsupportedOperationException("unimplemented");
    }
}
