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
