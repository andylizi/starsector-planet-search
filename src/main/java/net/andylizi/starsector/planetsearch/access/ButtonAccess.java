/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.ButtonAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class ButtonAccess {
    private final Class<? extends ButtonAPI> buttonType;
    private final Class<?> actionListenerType;
    private final MethodHandle m_getListener;
    private final MethodHandle m_setListener;

    public ButtonAccess(Class<? extends ButtonAPI> buttonType) throws ReflectiveOperationException {
        this.buttonType = buttonType;

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Method method = buttonType.getMethod("getListener");
        method.trySetAccessible();
        this.actionListenerType = method.getReturnType();
        this.m_getListener = lookup.unreflect(method);

        if (!this.actionListenerType.isInterface())
            throw new ClassNotFoundException(buttonType + ".getListener() return type is not an interface");

        method = buttonType.getMethod("setListener", actionListenerType);
        method.trySetAccessible();
        this.m_setListener = lookup.unreflect(method);
    }

    public Class<? extends ButtonAPI> buttonType() {
        return buttonType;
    }

    public Class<?> actionListenerType() {
        return actionListenerType;
    }

    public Object getListener(ButtonAPI button) {
        try {
            return this.m_getListener.invoke(button);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setListener(ButtonAPI button, @Nullable Object listener) {
        try {
            this.m_setListener.invoke(button, listener);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
