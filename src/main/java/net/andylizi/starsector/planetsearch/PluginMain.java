/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;

public final class PluginMain extends BaseModPlugin {
    private static final MethodHandle scriptCtor;

    static {
        MethodHandle ctor;
        try {
            // Bypass reflection restriction
            // https://fractalsoftworks.com/forum/index.php?topic=23229.msg354196#msg354196
            ClassLoader cl = PluginMain.class.getClassLoader();
            while (cl != null && !(cl instanceof URLClassLoader)) cl = cl.getParent();
            if (cl == null) throw new RuntimeException("Unable to find URLClassLoader");
            URL[] urls = ((URLClassLoader) cl).getURLs();

            @SuppressWarnings("resource")
            Class<?> cls = new CustomClassLoader(urls, ClassLoader.getSystemClassLoader())
                    .loadClass(PluginMain.class.getPackage().getName() + ".CoreUIWatchScript");

            ctor = MethodHandles.lookup().findConstructor(cls, MethodType.methodType(void.class));
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
        scriptCtor = ctor;
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (scriptCtor != null) {
            try {
                EveryFrameScript script = (EveryFrameScript) scriptCtor.invoke();
                Global.getSector().addTransientScript(script);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new AssertionError("unreachable", t);
            }
        }
    }
}
