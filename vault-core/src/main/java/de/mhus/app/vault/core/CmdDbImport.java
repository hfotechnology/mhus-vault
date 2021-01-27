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

import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "dbimport", description = "Import a exported db")
@Service
public class CmdDbImport extends AbstractCmd {

    @Argument(
            index = 0,
            name = "privateKey",
            required = true,
            description = "Private key id in keychain",
            multiValued = false)
    String privateKeyId;

    @Argument(
            index = 1,
            name = "file",
            required = true,
            description = "File from where to import",
            multiValued = false)
    String fileName;
    
    @Argument(
            index = 2,
            name = "passphrase",
            required = false,
            description = "Passphrase for private key, will be asked if not set",
            multiValued = false)
    String passphrase;
    
    @Option(name = "-a", description = "import targets and groups", required = false, multiValued = false)
    boolean all = false;

    @Override
    public Object execute2() throws Exception {

        de.mhus.lib.core.keychain.KeyEntry key =
                MKeychainUtil.loadDefault().getEntry(UUID.fromString(privateKeyId));
        if (key == null) {
            System.out.println("Key not found");
            return null;
        }

        if (passphrase == null)
            passphrase = Console.get().readPassword();

        new ImportUtil()
                .importDb(key.getValue().value(), passphrase, fileName, all);

        return null;
    }
}
