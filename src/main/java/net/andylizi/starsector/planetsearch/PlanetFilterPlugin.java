/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.List;

public interface PlanetFilterPlugin {
    void afterSizeFirstChanged(UIPanelAPI panel, float width, float height);
    List<SectorEntityToken> getFilteredList(UIPanelAPI panel, List<SectorEntityToken> list);
}
