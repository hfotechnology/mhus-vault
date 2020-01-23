package de.mhus.cherry.vault.manager;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.cherry.vault.core.StaticAccess;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "cherry", name = "cvmgmt", description = "Cherry Vault Management")
@Service
public class AdminCmd extends AbstractCmd {

    @Argument(index=0, name="cmd", required=true, description="Command:\n"
            + " dbdelete - delete all from database\n"
            + " ", multiValued=false)
    String cmd;
    
    @Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        // CherryVaultApi api = M.l(CherryVaultApi.class);
        
        switch (cmd) {
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
