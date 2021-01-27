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
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.app.vault.api.CherryVaultApi;
import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "vault-cleanup", description = "Remove expired entries")
@Service
public class CmdVaultCleanup extends AbstractCmd {
    
    @Argument(
            index = 0,
            name = "group",
            required = false,
            description = "Name of the group to cleanup or all",
            multiValued = false)
    String groupName;

    @Override
    public Object execute2() throws Exception {

        CherryVaultApi api = M.l(CherryVaultApi.class);

        api.cleanup(groupName);

        return null;
    }
}
