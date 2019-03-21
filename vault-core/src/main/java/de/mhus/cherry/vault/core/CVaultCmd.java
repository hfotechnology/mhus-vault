package de.mhus.cherry.vault.core;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.console.ConsoleTable;

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

    @Option(name="-t", description="Target",required=false, multiValued=false)
    String target;

    @Override
    public Object execute() throws Exception {

        CherryVaultApi api = MApi.lookup(CherryVaultApi.class);

        switch (cmd) {
        case "search": {
            ConsoleTable table = new ConsoleTable(false);
            table.setHeaderValues("id","SecretId","Group","Target","SecretKeyId","From","To");
            for (VaultEntry item : api.search(target, parameters, 100)) {
                table.addRowValues(item.getId(),item.getSecretId(),item.getGroup(),item.getTarget(),item.getSecretKeyId(),item.getValidFrom(),item.getValidTo());
            }
            table.print();
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
