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

import java.util.List;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.model.VaultGroup;
import de.mhus.app.vault.api.model.VaultTarget;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "targets", description = "Print all tagets")
@Service
public class CmdTargets extends AbstractCmd {

    @Option(name = "-g", description = "Group", required = false, multiValued = false)
    String group;
    
    @Option(name = "-a", description = "All", required = false, multiValued = false)
    boolean all = false;

    @Override
    public Object execute2() throws Exception {

        List<String> filter = null;
        AQuery<VaultTarget> query = Db.query(VaultTarget.class);
        if (!all) query.eq("enabled", true);
        if (group != null) {
            VaultGroup g =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(
                                    Db.query(VaultGroup.class).eq("name", group));
            if (g == null) return null;
            filter = g.getTargets();
        }

        query.asc("name");
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues(
                "Name",
                "Processor",
                "Config",
                "Condition",
                "Config",
                "Id",
                "Modified",
                "Description");
        for (VaultTarget item :
                StaticAccess.db.getManager().getByQualification(query)) {
            if (filter == null || filter.contains(item.getName()))
                out.addRowValues(
                        item.getName() + (item.isEnabled() ? "" : "\n[disabled]"),
                        item.getProcessorName(),
                        item.getProcessorConfig(),
                        item.getConditionNames(),
                        item.getConditionConfig(null),
                        item.getId(),
                        item.getModifyDate(),
                        item.getDescription());
        }
        out.print();

        return null;
    }
}
