/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.app.vault.manager;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.model.VaultEntry;
import de.mhus.app.vault.api.model.VaultGroup;
import de.mhus.app.vault.api.model.VaultKey;
import de.mhus.app.vault.api.model.VaultTarget;
import de.mhus.app.vault.core.StaticAccess;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "dbdelete", description = "delete everithing from database")
@Service
public class CmdVaultDbDelete extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        for (VaultEntry entry : StaticAccess.db.getManager().getAll(VaultEntry.class))
            try {
                System.out.println("Delete " + entry);
                entry.delete();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        for (VaultTarget entry : StaticAccess.db.getManager().getAll(VaultTarget.class))
            try {
                System.out.println("Delete " + entry);
                entry.delete();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        for (VaultGroup entry : StaticAccess.db.getManager().getAll(VaultGroup.class))
            try {
                System.out.println("Delete " + entry);
                entry.delete();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        for (VaultKey entry : StaticAccess.db.getManager().getAll(VaultKey.class))
            try {
                System.out.println("Delete " + entry);
                entry.delete();
            } catch (Throwable t) {
                t.printStackTrace();
            }

        System.out.println("OK");

        return null;
    }
}
