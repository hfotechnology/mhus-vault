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

import java.util.Date;
import java.util.LinkedList;

import de.mhus.app.vault.api.ifc.SecretContent;
import de.mhus.app.vault.api.model.VaultEntry;
import de.mhus.app.vault.api.model.VaultGroup;
import de.mhus.app.vault.core.StaticAccess;
import de.mhus.app.vault.core.VaultApiImpl;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.crypt.api.util.CryptUtil;

public class RecreateUtil {

    public void recreate(
            String target,
            String recTarget,
            String recPrivateKey,
            String recPassphrase,
            String filter)
            throws MException {
        XdbService db = StaticAccess.db.getManager();

        Date now = new Date();
        System.out.println("Now: " + now.getTime() + " " + now);
        for (VaultEntry entry :
                db.getByQualification(
                        Db.query(VaultEntry.class).eq("target", target).gt("validto", now))) {
            if (filter != null && !entry.getSecret().contains(filter)) continue;

            System.out.println(">>> Recreate " + entry);
            try {
                VaultEntry recEntry =
                        db.getObjectByQualification(
                                Db.query(VaultEntry.class)
                                        .eq("secretid", entry.getSecretId())
                                        .eq("target", recTarget));
                if (recEntry == null) {
                    System.out.println("*** Recovery Entry not found");
                    continue;
                }

                Date actualValidTo = entry.getValidTo();

                String recValue = recEntry.getSecret();
                String secret = CryptUtil.decrypt(recPrivateKey, recPassphrase, recValue);
                VaultGroup group = VaultApiImpl.instance.getGroup(entry.getGroup());

                LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
                SecretContent secretContent =
                        new SecretContent(
                                new SecureString(secret), new MProperties(entry.getProperties()));
                VaultApiImpl.instance.processTarget(
                        group,
                        target,
                        (MProperties) secretContent.getProperties(),
                        entry.getSecretId(),
                        secretContent,
                        entriesToSave);

                entry.setValidTo(now);
                entry.save();

                VaultApiImpl.instance.updateIndexes(
                        entriesToSave,
                        new String[] {
                            entry.getIndex0(),
                            entry.getIndex1(),
                            entry.getIndex2(),
                            entry.getIndex3(),
                            entry.getIndex4()
                        },
                        null);

                // save entries
                VaultApiImpl.instance.saveEntries(
                        group.getName(), entriesToSave, now, actualValidTo);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
