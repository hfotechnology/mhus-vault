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

import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "cherry", name = "mgmt", description = "Cherry Vault Management")
@Service
public class CmdVaultRecreate extends AbstractCmd {

    @Argument(
            index = 0,
            name = "target",
            required = true,
            description = "target",
            multiValued = false)
    String target;
    
    @Argument(
            index = 1,
            name = "recoveryTarget",
            required = true,
            description = "Recovery target",
            multiValued = false)
    String recTarget;
    
    @Argument(
            index = 2,
            name = "recoveryPrivKey",
            required = true,
            description = "recoveryPrivKey",
            multiValued = false)
    String recPrivKey;
    
    @Argument(
            index = 3,
            name = "recoveryPrivPass",
            required = true,
            description = "recoveryPrivPass",
            multiValued = false)
    String recPassphrase;
    
    @Argument(
            index = 4,
            name = "filter",
            required = false,
            description = "Filter",
            multiValued = false)
    String filter;

    @Override
    public Object execute2() throws Exception {
        
        de.mhus.lib.core.keychain.KeyEntry recPrivEntry =
                MKeychainUtil.loadDefault().getEntry(UUID.fromString(recPrivKey));
        if (recPrivEntry == null) throw new MException("key not found");

        new RecreateUtil()
                .recreate(
                        target,
                        recTarget,
                        recPrivEntry.getValue().value(),
                        recPassphrase,
                        filter);
        System.out.println("OK");

        return null;
    }
}
