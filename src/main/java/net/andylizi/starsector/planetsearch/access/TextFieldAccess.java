package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TextFieldAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TextFieldAccess {
    private final Class<? extends TextFieldAPI> textFieldType;
    private final Class<?> textListenerType;
    private final MethodHandle m_setTextListener;

    public TextFieldAccess(Class<? extends TextFieldAPI> textFieldType) throws ReflectiveOperationException {
        this.textFieldType = textFieldType;
        this.textListenerType = textFieldType.getMethod("getTextListener").getReturnType();
        this.m_setTextListener = MethodHandles.publicLookup().findVirtual(textFieldType, "setTextListener",
                MethodType.methodType(void.class, textListenerType));
    }

    public Class<? extends TextFieldAPI> textFieldType() {
        return textFieldType;
    }

    public Class<?> textListenerType() {
        return textListenerType;
    }

    public void setTextListener(TextFieldAPI textBox, @Nullable Object listener) {
        try {
            this.m_setTextListener.invoke(textBox, listener);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
