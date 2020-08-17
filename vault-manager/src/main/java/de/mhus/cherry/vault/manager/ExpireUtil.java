/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package de.mhus.cherry.vault.manager;

import java.util.Date;

import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.core.StaticAccess;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;

public class ExpireUtil {

    public void expire(String target, String filter) throws MException {
        XdbService db = StaticAccess.db.getManager();

        Date now = new Date();
        System.out.println("Now: " + now.getTime() + " " + now);
        for (VaultEntry entry :
                db.getByQualification(
                        Db.query(VaultEntry.class).eq("target", target).gt("validto", now))) {
            if (filter != null && !entry.getSecret().contains(filter)) continue;

            System.out.println(">>> Expire " + entry);
            try {
                entry.setValidTo(now);
                entry.save();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
