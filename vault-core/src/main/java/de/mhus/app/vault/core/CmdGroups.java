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
package de.mhus.app.vault.core;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.model.VaultGroup;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "groups", description = "Print all groups")
@Service
public class CmdGroups extends AbstractCmd {


    @Option(name = "-a", description = "Print disabled", required = false, multiValued = false)
    boolean all = false;

    @Override
    public Object execute2() throws Exception {

        AQuery<VaultGroup> query = Db.query(VaultGroup.class);
        if (!all) query.eq("enabled", true);
        query.asc("name");
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Name", "Targets", "Generator", "Config", "Id", "Modified");
        for (VaultGroup item : StaticAccess.db.getManager().getByQualification(query)) {
            out.addRowValues(
                    item.getName() + (item.isEnabled() ? "" : "\n[disabled]"),
                    item.getTargets(),
                    item.getSecretGeneratorName(),
                    item.getSecretGeneratorConfig(),
                    item.getId(),
                    item.getModifyDate());
        }
        out.print();

        return null;
    }
}
