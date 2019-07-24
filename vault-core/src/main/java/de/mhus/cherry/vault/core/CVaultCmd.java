/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core;

import java.util.Date;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.core.impl.StaticAccess;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "cherry", name = "cvc", description = "Cherry Vault Control")
@Service
public class CVaultCmd extends AbstractCmd {

    @Argument(index=0, name="cmd", required=true, description="Command:\n"
            + " search [index0] [index1] [index2] [index3] [index4]\n"
            + " entry <id> | <secretid> (-t <target>)\n"
            + " create <groupId> [index0] [index1] [index2] [index3] [index4]\n"
            + " updatecreate <secretId> [index0] [index1] [index2] [index3] [index4]\n"
            + " import <groupId> <secret> [index0] [index1] [index2] [index3] [index4]\n"
            + " updateimport <secretId> <secret> [index0] [index1] [index2] [index3] [index4]\n"
            + " updateindex <secretId> [index0] [index1] [index2] [index3] [index4]\n"
            + " test <group> [key=value]*\n"
            + " ", multiValued=false)
    String cmd;
    
    @Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Option(name="-t", description="Target",required=false, multiValued=false)
    String target;
    
    @Option(name="-g", description="Group",required=false, multiValued=false)
    String group;
    
    @Option(name="-fr", description="Valid From",required=false, multiValued=false)
    String fromStr;
    
    @Option(name="-to", description="Valid To",required=false, multiValued=false)
    String toStr;
    
    @Option(name="-p", description="Properties",required=false, multiValued=true)
    String p[];
    
    @Option(name="-a", description="All",required=false, multiValued=false)
    boolean all = false;
    
    @Override
    public Object execute2() throws Exception {

        CherryVaultApi api = M.l(CherryVaultApi.class);

        Date from = MCast.toDate(fromStr, null);
        Date to = MCast.toDate(toStr, null);
        
        MProperties prop = MProperties.explodeToMProperties(p);
        
        switch (cmd) {
        case "test": {
            String[] p = MCollection.cropArray(parameters, 1, parameters.length);
            String out = api.testGroup(parameters[0], MProperties.explodeToMProperties(p));
            System.out.println(out);
        } break;
        case "create": {
            String[] index = MCollection.cropArray(parameters, 1, parameters.length);
            String id = api.createSecret(parameters[0], from, to, prop, index);
            System.out.println(id);
        } break;
        case "search": {
            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues("id","SecretId","Group","Target","From","To");
            for (VaultEntry item : api.search(group, target, parameters, 100, all)) {
                table.addRowValues(item.getId(),item.getSecretId(),item.getGroup(),item.getTarget(),item.getValidFrom(),item.getValidTo());
            }
            table.print();
        } break;
        case "entry": {
            String secretId = parameters[0];
            VaultEntry res = null;
            if (target == null) {
                res = StaticAccess.db.getManager().getObject(VaultEntry.class, secretId);
            } else {
                res = api.getSecret(secretId, target);
            }
            if (res == null) {
                System.out.println("Secret not found");
            } else {
                System.out.println(res);
                System.out.println(res.getSecret());
            }
        } break;
        case "updateindex": {
            String secretId = parameters[0];
            String[] index = MCollection.cropArray(parameters, 1, parameters.length);
            api.indexUpdate(secretId, index);
            System.out.println("OK");
        } break;
        case "updatecreate": {
            String secretId = parameters[0];
            String[] index = MCollection.cropArray(parameters, 1, parameters.length);
            api.createUpdate(secretId, from, to, prop, index);
            System.out.println("OK");
        } break;
        case "import": {
            String[] index = MCollection.cropArray(parameters, 2, parameters.length);
            String id = api.importSecret(parameters[0], from, to, parameters[1], prop, index);
            System.out.println(id);
        } break;
        case "updateimport": {
            String[] index = MCollection.cropArray(parameters, 2, parameters.length);
            api.importUpdate(parameters[0], from, to, parameters[1], prop, index);
            System.out.println("OK");
        } break;
        default:
            System.out.println("Unknown command " + cmd);
        }

        return null;
    }

}
