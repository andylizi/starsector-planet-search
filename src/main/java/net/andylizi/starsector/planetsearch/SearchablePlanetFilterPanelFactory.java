/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import com.fs.starfarer.api.impl.PlanetSearchData;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public final class SearchablePlanetFilterPanelFactory {
    @SuppressWarnings("unchecked")
    public static <T extends UIPanelAPI> MethodHandle create(Class<T> baseType,
                                                             Class<? extends UIPanelAPI> planetListType,
                                                             Class<? extends PositionAPI> positionType) throws
            ReflectiveOperationException {
        final var siblingClass = SearchablePlanetFilterPanelFactory.class;
        final var objectType = Type.getType(Object.class);
        var classJavaName = siblingClass.getPackageName() + ".SearchablePlanetFilterPanel";
        var className = classJavaName.replace('.', '/');
        var baseName = Type.getInternalName(baseType);

        var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, baseName, new String[0]);

        var searchBoxDesc = Type.getDescriptor(TextFieldAPI.class);
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "searchBox", searchBoxDesc, null, null);

        {
            // public SearchablePlanetFilterPanel(float width, float height, PlanetListV2 list)
            //       throws ReflectiveOperationException {
            //   super(width, height, list);
            //   this.searchBox = PlanetsPanelInjector.__injectFilterPanel(this);
            //   this.setSize(width, height);
            //   this.updatePlanetList();
            // }
            var desc = Type.getMethodDescriptor(Type.VOID_TYPE,
                    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType(planetListType));
            var m = cw.visitMethod(ACC_PUBLIC, "<init>", desc, null,
                    new String[] { Type.getInternalName(ReflectiveOperationException.class) });
            m.visitCode();
            m.visitVarInsn(ALOAD, 0);
            m.visitVarInsn(FLOAD, 1); // width
            m.visitVarInsn(FLOAD, 2); // height
            m.visitVarInsn(ALOAD, 3); // planetsPanel
            m.visitMethodInsn(INVOKESPECIAL, baseName, "<init>", desc, false);

            m.visitVarInsn(ALOAD, 0);
            m.visitInsn(DUP);
            var inject = PlanetsPanelInjector.class.getDeclaredMethod("__injectFilterPanel", UIPanelAPI.class);
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(inject.getDeclaringClass()),
                    inject.getName(), Type.getMethodDescriptor(inject), false);
            m.visitFieldInsn(PUTFIELD, className, "searchBox", searchBoxDesc);

            // Reproduce the post-creation actions performed in PlanetsPanel.createUI()
            m.visitVarInsn(ALOAD, 0);
            m.visitVarInsn(FLOAD, 1); // width
            m.visitVarInsn(FLOAD, 2); // height
            m.visitMethodInsn(INVOKEVIRTUAL, className, "setSize",
                    Type.getMethodDescriptor(Type.getType(positionType), Type.FLOAT_TYPE, Type.FLOAT_TYPE), false);
            m.visitInsn(POP);
            m.visitVarInsn(ALOAD, 0);
            m.visitMethodInsn(INVOKEVIRTUAL, baseName, "updatePlanetList",
                    Type.getMethodDescriptor(Type.VOID_TYPE), false);

            m.visitInsn(RETURN);
            m.visitMaxs(0, 0);
            m.visitEnd();
        }

        {
            // public List<PlanetFilter> getAllFilters() {
            //   List filters = super.getAllFilters();
            //   filters.add(PlanetsPanelInjector.SEARCH_FILTER);
            //   return filters;
            // }
            var searchFilterField = PlanetsPanelInjector.class.getDeclaredField("SEARCH_FILTER");
            var desc = Type.getMethodDescriptor(Type.getType(List.class));
            var sig = new SignatureWriter();
            sig.visitReturnType();
            sig.visitClassType(Type.getInternalName(List.class));
            {
                var bound = sig.visitTypeArgument('=');
                bound.visitClassType(Type.getInternalName(PlanetSearchData.PlanetFilter.class));
                bound.visitEnd();
            }
            sig.visitEnd();
            var m = cw.visitMethod(ACC_PUBLIC, "getAllFilters", desc, sig.toString(), null);
            m.visitCode();
            m.visitVarInsn(ALOAD, 0);
            m.visitMethodInsn(INVOKESPECIAL, baseName, "getAllFilters", desc, false);
            m.visitInsn(DUP);
            m.visitFieldInsn(GETSTATIC, Type.getInternalName(searchFilterField.getDeclaringClass()),
                    searchFilterField.getName(), Type.getDescriptor(searchFilterField.getType()));
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(List.class), "add",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, objectType), true);
            m.visitInsn(POP);
            m.visitInsn(ARETURN);
            m.visitMaxs(0, 0);
            m.visitEnd();
        }

        {
            // public Map<String, String> getParams() {
            //   Map params = super.getParams();
            //   PlanetsPanelInjector.__hook_getParams(params, this.searchBox);
            //   return params;
            // }
            var desc = Type.getMethodDescriptor(Type.getType(Map.class));
            var sig = new SignatureWriter();
            {
                var ret = sig.visitReturnType();
                ret.visitClassType(Type.getInternalName(Map.class));
                {
                    var bound = sig.visitTypeArgument('=');
                    bound.visitClassType(Type.getInternalName(String.class));
                    bound.visitEnd();
                    bound.visitClassType(Type.getInternalName(String.class));
                    bound.visitEnd();
                }
                ret.visitEnd();
            }
            var m = cw.visitMethod(ACC_PUBLIC, "getParams", desc, sig.toString(), null);
            m.visitCode();
            m.visitVarInsn(ALOAD, 0);
            m.visitMethodInsn(INVOKESPECIAL, baseName, "getParams", desc, false);
            m.visitInsn(DUP);
            m.visitVarInsn(ALOAD, 0);
            m.visitFieldInsn(GETFIELD, className, "searchBox", searchBoxDesc);
            var hook = PlanetsPanelInjector.class.getDeclaredMethod("__hook_getParams", Map.class, TextFieldAPI.class);
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(hook.getDeclaringClass()),
                    hook.getName(), Type.getMethodDescriptor(hook), false);
            m.visitInsn(ARETURN);
            m.visitMaxs(0, 0);
            m.visitEnd();
        }

        {
            // public void syncWithParams(Map<String, String> params) {
            //   super.syncWithParams(params);
            //   PlanetsPanelInjector.__hook_syncWithParams(this.searchBox, params);
            // }
            var desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Map.class));
            var sig = new SignatureWriter();
            {
                var param = sig.visitParameterType();
                param.visitClassType(Type.getInternalName(Map.class));
                {
                    var bound = param.visitTypeArgument('=');
                    bound.visitClassType(Type.getInternalName(String.class));
                    bound.visitEnd();
                    bound.visitClassType(Type.getInternalName(String.class));
                    bound.visitEnd();
                }
                param.visitEnd();
            }
            sig.visitReturnType().visitBaseType('V');
            var m = cw.visitMethod(ACC_PUBLIC, "syncWithParams", desc, sig.toString(), null);
            m.visitCode();
            m.visitVarInsn(ALOAD, 0);
            m.visitInsn(DUP);
            m.visitVarInsn(ALOAD, 1);
            m.visitMethodInsn(INVOKESPECIAL, baseName, "syncWithParams", desc, false);
            m.visitFieldInsn(GETFIELD, className, "searchBox", searchBoxDesc);
            m.visitVarInsn(ALOAD, 1);
            var hook = PlanetsPanelInjector.class.getDeclaredMethod("__hook_syncWithParams",
                    TextFieldAPI.class, Map.class);
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(hook.getDeclaringClass()),
                    hook.getName(), Type.getMethodDescriptor(hook), false);
            m.visitInsn(RETURN);
            m.visitMaxs(0, 0);
            m.visitEnd();
        }

        cw.visitEnd();
        byte[] data = cw.toByteArray();
//        try {
//            Files.write(Path.of("dump.class"), data);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }

        var lookup = MethodHandles.lookup();
        ClassLoader cl = siblingClass.getClassLoader();
        // We can't directly cast to CustomClassLoader because that class is loaded by another class loader
        MethodHandle defineClass = lookup.findVirtual(cl.getClass(), "defineMyClass",
                methodType(Class.class, String.class, byte[].class, int.class, int.class,
                        ProtectionDomain.class));
        try {
            var cls = (Class<? extends T>) defineClass.invoke(cl, classJavaName, data, 0, data.length,
                    siblingClass.getProtectionDomain());
            return lookup.findConstructor(cls, methodType(void.class,
                    float.class, float.class, planetListType));
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
    }

    private SearchablePlanetFilterPanelFactory() {throw new AssertionError();}
}
