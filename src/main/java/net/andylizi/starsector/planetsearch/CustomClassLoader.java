/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class CustomClassLoader extends URLClassLoader  {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public CustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    // Have to be public for some reason
    public Class<?> defineMyClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) {
        return defineClass(name, b, off, len, protectionDomain);
    }
}
