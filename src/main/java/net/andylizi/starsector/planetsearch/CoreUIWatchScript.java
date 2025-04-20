/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import net.andylizi.starsector.planetsearch.access.CampaignStateAccess;
import net.andylizi.starsector.planetsearch.access.InteractionDialogAccess;
import org.apache.log4j.Logger;

public class CoreUIWatchScript implements EveryFrameScriptWithCleanup {
    private static final Logger logger = Logger.getLogger(CoreUIWatchScript.class);

    private static InteractionDialogAccess acc_InteractionDialog;
    private static CampaignStateAccess acc_CampaignState;

    private boolean done = false;
    private CoreUIAPI current = null;

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        try {
            CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
            InteractionDialogAPI dialog = campaignUI.getCurrentInteractionDialog();
            CoreUIAPI core = null;

            if (dialog != null) {
                if (acc_InteractionDialog == null) {
                    Class<? extends InteractionDialogAPI> dialogType = dialog.getClass();
                    // Guard against other implementations of InteractionDialogAPI
                    if (dialogType.getName().startsWith("com.fs.starfarer.ui.newui.")) {
                        acc_InteractionDialog = new InteractionDialogAccess(dialogType);
                    }
                }

                if (acc_InteractionDialog != null) core = acc_InteractionDialog.getCoreUI(dialog);
            }

            if (core == null) {
                if (acc_CampaignState == null) acc_CampaignState = new CampaignStateAccess();
                core = acc_CampaignState.getCore(campaignUI);
            }

            if (core != current) {
                if (core != null) CoreUIInjector.inject(core);
                current = core;
            }
        } catch (Throwable t) {
            this.done = true;
            logger.error("Injection failed", t);
        }
    }

    @Override
    public void cleanup() {
        if (current != null) {
            CoreUIInjector.uninject(current);
        }
    }
}
