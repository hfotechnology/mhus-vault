package de.mhus.cherry.vault.manager;

import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.cherry.vault.core.StaticAccess;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "cherry", name = "mgmt", description = "Cherry Vault Management")
@Service
public class VaultMgmtCmd extends AbstractCmd {

    @Argument(index=0, name="cmd", required=true, description="Command:\n"
            + " dbdelete - delete all from database\n"
            + " recreate <target> <recovery target> <recovery priv key> <recovery priv pass> [filter]\n"
            + " expire <taget> [filter]\n"
            + " ", multiValued=false)
    String cmd;
    
    @Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        // CherryVaultApi api = M.l(CherryVaultApi.class);
        
        switch (cmd) {
        case "recreate": {
            String target = parameters[0];
            String recTarget = parameters[1];
            String recPrivKey = parameters[2];
            String recPassphrase = parameters[3];
            String filter = parameters.length > 4 ? parameters[4] : null;
            
            de.mhus.lib.core.vault.VaultEntry recPrivEntry = MVaultUtil.loadDefault().getEntry(UUID.fromString(recPrivKey));
            if (recPrivEntry == null) throw new MException("key not found");
            
            new RecreateUtil().recreate(target, recTarget, recPrivEntry.getValue().value(), recPassphrase, filter);
        } break;
        case "expire": {
            String target = parameters[0];
            String filter = parameters.length > 1 ? parameters[1] : null;
            new ExpireUtil().expire(target, filter);
        } break;
        case "dbdelete": {
            for (VaultEntry entry : StaticAccess.db.getManager().getAll(VaultEntry.class))
                try {
                    System.out.println("Delete " + entry);
                    entry.delete();
                } catch (Throwable t) {t.printStackTrace();}
            for (VaultTarget entry : StaticAccess.db.getManager().getAll(VaultTarget.class))
                try {
                    System.out.println("Delete " + entry);
                    entry.delete();
                } catch (Throwable t) {t.printStackTrace();}
            for (VaultGroup entry : StaticAccess.db.getManager().getAll(VaultGroup.class))
                try {
                    System.out.println("Delete " + entry);
                    entry.delete();
                } catch (Throwable t) {t.printStackTrace();}
            for (VaultKey entry : StaticAccess.db.getManager().getAll(VaultKey.class))
                try {
                    System.out.println("Delete " + entry);
                    entry.delete();
                } catch (Throwable t) {t.printStackTrace();}
        } break;
        }
        return null;
    }

}
