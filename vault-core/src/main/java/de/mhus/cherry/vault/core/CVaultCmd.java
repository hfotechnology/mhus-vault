package de.mhus.cherry.vault.core;

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCollection;

@Command(scope = "cherry", name = "cvc", description = "Cherry Vault Control")
@Service
public class CVaultCmd implements Action {

    @Argument(index=0, name="cmd", required=true, description="Command:\n"
            + " search [index0] [index1] [index2] [index3] [index4]\n"
            + " update <secretId> [index0] [index1] [index2] [index3] [index4]\n"
            + " ", multiValued=false)
    String cmd;
    
    @Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Override
    public Object execute() throws Exception {

        CherryVaultApi api = MApi.lookup(CherryVaultApi.class);

        switch (cmd) {
        case "search": {
            for (VaultEntry item : api.search(parameters, 100)) {
                System.out.println(item);
            }
        } break;
        case "update": {
            String secretId = parameters[0];
            String[] index = MCollection.cropArray(parameters, 1, parameters.length);
            api.update(secretId, index);
            System.out.println("OK");
        } break;
        }

        return null;
    }

}
