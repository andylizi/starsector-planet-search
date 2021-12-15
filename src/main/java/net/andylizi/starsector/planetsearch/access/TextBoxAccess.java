package net.andylizi.starsector.planetsearch.access;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import net.andylizi.starsector.planetsearch.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TextBoxAccess {
    private final Class<? extends UIComponentAPI> textBoxType;
    private final Class<?> textListenerType;
    private final Class<? extends LabelAPI> labelType;
    private final MethodHandle ctor;
    private final MethodHandle m_getText;
    private final MethodHandle m_setTextListener;

    @SuppressWarnings("unchecked")
    public TextBoxAccess() throws ReflectiveOperationException {
        Class<?> launcherType = Class.forName("com.fs.starfarer.launcher.opengl.GLLauncher");
        Class<?> textBoxType = null;
        for (Field field : launcherType.getDeclaredFields()) {
            Class<?> type = field.getType();
            if (type.isPrimitive()) continue;
            try {
                type.getDeclaredMethod("getMaxChars");
                type.getDeclaredMethod("getTextLabel");
                textBoxType = type;
                break;
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (textBoxType == null) throw new ClassNotFoundException("TextBox");
        this.textBoxType = (Class<? extends UIComponentAPI>) textBoxType;
        this.textListenerType = textBoxType.getMethod("getTextListener").getReturnType();
        this.labelType = (Class<? extends LabelAPI>) textBoxType.getMethod("getTextLabel").getReturnType();

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Constructor<?> ctor = ReflectionUtil.getFirstConstructorByParameterCount(textBoxType, 4);
        ReflectionUtil.trySetAccessible(ctor);
        this.ctor = lookup.unreflectConstructor(ctor);
        this.m_getText = lookup.findVirtual(textBoxType, "getText", MethodType.methodType(String.class));
        this.m_setTextListener = lookup.findVirtual(textBoxType, "setTextListener",
                MethodType.methodType(void.class, textListenerType));
    }

    public Class<? extends UIComponentAPI> textBoxType() {
        return textBoxType;
    }

    public Class<?> textListenerType() {
        return textListenerType;
    }

    public Class<? extends LabelAPI> labelType() {
        return labelType;
    }

    public UIComponentAPI newInstance(String text, String font, boolean unknown, @Nullable Object blurListener) {
        try {
            return (UIComponentAPI) this.ctor.invoke(text, font, unknown, blurListener);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public String getText(UIComponentAPI textBox) {
        try {
            return (String) this.m_getText.invoke(textBox);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    public void setTextListener(UIComponentAPI textBox, @Nullable Object listener) {
        try {
            this.m_setTextListener.invoke(textBox, listener);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }
}
