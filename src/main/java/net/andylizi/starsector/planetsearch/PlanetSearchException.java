/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.andylizi.starsector.planetsearch;

public class PlanetSearchException extends RuntimeException {
    public PlanetSearchException(String message, Throwable cause) {
        super("PlanetSearch mod failed to load: " + message, cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        // Don't record stacktrace as this exception is purely a wrapper
        return this;
    }
}
