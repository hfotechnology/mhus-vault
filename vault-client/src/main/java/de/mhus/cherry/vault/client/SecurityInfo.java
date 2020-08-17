/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.client;

import java.security.Provider;
import java.security.Security;

import de.mhus.lib.core.console.ConsoleTable;

public class SecurityInfo {

    public static void main(String[] args) {

        try {
            EccSigner.init();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        ConsoleTable out = new ConsoleTable();
        out.setHeaderValues("Name", "Version", "Info");
        for (Provider provider : Security.getProviders()) {
            out.addRowValues(provider.getName(), provider.getVersionStr(), provider.getInfo());
        }
        out.print(System.out);

        out = new ConsoleTable();
        out.setHeaderValues("Provider", "Algorithm", "Type");

        for (Provider provider : Security.getProviders()) {
            for (java.security.Provider.Service service : provider.getServices()) {
                out.addRowValues(provider.getName(), service.getAlgorithm(), service.getType());
            }
        }
        out.print(System.out);
    }
}
