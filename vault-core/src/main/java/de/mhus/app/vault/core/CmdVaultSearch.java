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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.model.VaultEntry;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "search", description = "Search vault entries")
@Service
public class CmdVaultSearch extends AbstractCmd {

    @Argument(
            index = 0,
            name = "index",
            required = false,
            description = "Index values",
            multiValued = true)
    String[] indexValues;

    @Option(name = "-t", description = "Target", required = false, multiValued = false)
    String target;

    @Option(name = "-g", description = "Group", required = false, multiValued = false)
    String group;

    @Option(name = "-a", description = "All", required = false, multiValued = false)
    boolean all = false;

    @Override
    public Object execute2() throws Exception {

        ConsoleTable table = new ConsoleTable(tblOpt);
        table.setHeaderValues("id", "SecretId", "Group", "Target", "From", "To");
        for (VaultEntry item :
                VaultApiImpl.instance.search(group, target, indexValues, 100, all, false)) {
            table.addRowValues(
                    item.getId(),
                    item.getSecretId(),
                    item.getGroup(),
                    item.getTarget(),
                    item.getValidFrom(),
                    item.getValidTo());
        }
        table.print();

        return null;
    }
}
