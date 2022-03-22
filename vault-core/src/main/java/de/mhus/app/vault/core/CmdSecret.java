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

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.CherryVaultApi;
import de.mhus.app.vault.api.model.VaultEntry;
import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "secret", description = "Print all entries for a secret id")
@Service
public class CmdSecret extends AbstractCmd {

    @Argument(
            index = 0,
            name = "secret",
            required = true,
            description = "Id of the secret",
            multiValued = false)
    String secretId;

    @Option(name = "-t", description = "Target", required = false, multiValued = false)
    String target;

    //    @Option(name = "-a", description = "All", required = false, multiValued = false)
    //    boolean all = false;

    @Override
    public Object execute2() throws Exception {

        CherryVaultApi api = M.l(CherryVaultApi.class);

        List<VaultEntry> res = null;
        if (target == null) res = api.getSecrets(secretId);
        else {
            res = new LinkedList<>();
            VaultEntry r = api.getSecret(secretId, target);
            if (r != null) {
                System.out.println("Creator   : " + r.getCreator());
                System.out.println("Group     : " + r.getGroup());
                System.out.println("Target    : " + r.getTarget());
                System.out.println("ID        : " + r.getId());
                System.out.println("Created   : " + r.getCreationDate());
                System.out.println("Modified  : " + r.getModifyDate());
                System.out.println("Valid from: " + r.getValidFrom());
                System.out.println("Valid to  : " + r.getValidTo());
                System.out.println("SecretId  : " + r.getSecretId());
                System.out.println("Secret    : " + r.getSecret());
            }
            return null;
        }
        ConsoleTable table = new ConsoleTable(tblOpt);
        table.setHeaderValues("id", "Group", "Target", "From", "To");
        for (VaultEntry item : res) {
            table.addRowValues(
                    item.getId(),
                    item.getGroup(),
                    item.getTarget(),
                    item.getValidFrom(),
                    item.getValidTo());
        }
        table.print();

        return null;
    }
}
