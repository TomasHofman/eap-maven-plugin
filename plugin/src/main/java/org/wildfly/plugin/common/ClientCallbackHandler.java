/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.plugin.common;

import java.io.Console;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;

/**
 * A CallbackHandler implementation to supply the username and password if required when
 * connecting to the server - if these are not available the user will be prompted to
 * supply them.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class ClientCallbackHandler implements CallbackHandler {

    private final Console console;
    private boolean promptShown = false;
    private String username;
    private char[] password;

    ClientCallbackHandler(final String username, final String password) {
        console = System.console();
        this.username = username;
        if (password != null) {
            this.password = password.toCharArray();
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        // Special case for anonymous authentication to avoid prompting user for their name.
        if (callbacks.length == 1 && callbacks[0] instanceof NameCallback) {
            ((NameCallback) callbacks[0]).setName("anonymous demo user");
            return;
        }

        for (Callback current : callbacks) {
            if (current instanceof RealmCallback) {
                final RealmCallback rcb = (RealmCallback) current;
                final String defaultText = rcb.getDefaultText();
                rcb.setText(defaultText); // For now just use the realm suggested.

                prompt(defaultText);
            } else if (current instanceof RealmChoiceCallback) {
                throw new UnsupportedCallbackException(current, "Realm choice not currently supported.");
            } else if (current instanceof NameCallback) {
                final NameCallback ncb = (NameCallback) current;
                final String userName = obtainUsername("Username:");

                ncb.setName(userName);
            } else if (current instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) current;
                char[] password = obtainPassword("Password:");

                pcb.setPassword(password);
            } else {
                throw new UnsupportedCallbackException(current);
            }
        }
    }

    private void prompt(final String realm) {
        if (!promptShown) {
            promptShown = true;
        }
    }

    private String obtainUsername(final String prompt) {
        if (username == null) {
            checkConsole();
            username = console.readLine(prompt);
        }
        return username;
    }

    private char[] obtainPassword(final String prompt) {
        if (password == null) {
            checkConsole();
            password = console.readPassword(prompt);
        }

        return password;
    }

    private void checkConsole() {
        if (console == null) {
            throw new IllegalStateException(
                    "The environment does not have a usable console. Cannot prompt for user name and password");
        }
    }

}
