/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.TextFieldAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public class TextFieldAccess {
    private final Class<? extends TextFieldAPI> textFieldType;
    private final Class<?> textListenerType;
    private final MethodHandle m_setTextListener;
    private final MethodHandle m_setHint;

    public TextFieldAccess(Class<? extends TextFieldAPI> textFieldType) throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        this.textFieldType = textFieldType;
        this.textListenerType = textFieldType.getMethod("getTextListener").getReturnType();
        this.m_setTextListener = lookup.findVirtual(textFieldType, "setTextListener",
                methodType(void.class, textListenerType));
        this.m_setHint = lookup.findVirtual(textFieldType, "setHint", methodType(void.class, String.class));
    }

    public Class<? extends TextFieldAPI> textFieldType() {
        return textFieldType;
    }

    public Class<?> textListenerType() {
        return textListenerType;
    }

    public void setTextListener(TextFieldAPI self, @Nullable Object listener) {
        try {
            this.m_setTextListener.invoke(self, listener);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setHint(TextFieldAPI self, String hint) {
        try {
            this.m_setHint.invoke(self, hint);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
