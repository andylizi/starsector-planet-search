/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class CampaignStateAccess {
    private final Class<? extends CampaignUIAPI> campaignStateType;
    private final MethodHandle m_getCore;

    @SuppressWarnings("unchecked")
    public CampaignStateAccess() throws ReflectiveOperationException {
        this.campaignStateType = (Class<? extends CampaignUIAPI>) Class.forName("com.fs.starfarer.campaign.CampaignState");

        Method method = campaignStateType.getMethod("getCore");
        method.trySetAccessible();
        this.m_getCore = MethodHandles.publicLookup().unreflect(method);
    }

    public Class<? extends CampaignUIAPI> campaignStateType() {
        return campaignStateType;
    }

    @Nullable
    public CoreUIAPI getCore(CampaignUIAPI campaignUI) {
        try {
            return (CoreUIAPI) this.m_getCore.invoke(campaignUI);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
