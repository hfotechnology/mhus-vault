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

import java.util.Date;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.CherryVaultApi;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "vault-update-create", description = "update an existing secret")
@Service
public class CmdVaultUpdateCreate extends AbstractCmd {

    @Argument(
            index = 0,
            name = "secretId",
            required = false,
            description = "secretId of the entry to update",
            multiValued = false)
    String secretId;

    @Argument(
            index = 1,
            name = "index",
            required = false,
            description = "Index values",
            multiValued = true)
    String[] indexValues;

    @Option(
            name = "-f",
            aliases = "--from",
            description = "Valid From",
            required = false,
            multiValued = false)
    String fromStr;

    @Option(
            name = "-t",
            aliases = "--to",
            description = "Valid To",
            required = false,
            multiValued = false)
    String toStr;

    @Option(name = "-p", description = "Properties", required = false, multiValued = true)
    String p[];

    @Override
    public Object execute2() throws Exception {

        CherryVaultApi api = M.l(CherryVaultApi.class);

        Date from = MCast.toDate(fromStr, null);
        Date to = MCast.toDate(toStr, null);

        MProperties prop = IProperties.explodeToMProperties(p);

        api.createUpdate(secretId, from, to, prop, indexValues);
        System.out.println("OK");

        return null;
    }
}
