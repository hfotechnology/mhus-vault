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
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "dbexport", description = "Export data for the management tool")
@Service
public class CmdDbExport extends AbstractCmd {

    @Argument(
            index = 0,
            name = "publicKey",
            required = true,
            description = "Public key id in keychain",
            multiValued = false)
    String publicKeyId;

    @Argument(
            index = 1,
            name = "file",
            required = true,
            description = "File where to export",
            multiValued = false)
    String fileName;

    @Argument(
            index = 2,
            name = "group",
            required = false,
            description = "Name of the group or all if not set",
            multiValued = false)
    String groupName;

    @Override
    public Object execute2() throws Exception {

        de.mhus.lib.core.keychain.KeyEntry key =
                MKeychainUtil.loadDefault().getEntry(UUID.fromString(publicKeyId));
        if (key == null) {
            System.out.println("Key not found");
            return null;
        }
        new ExportUtil().exportDb(key.getValue().value(), fileName, groupName);

        return null;
    }
}
