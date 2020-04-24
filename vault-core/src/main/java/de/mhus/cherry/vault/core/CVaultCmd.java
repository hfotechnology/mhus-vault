/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "cherry", name = "vault", description = "Cherry Vault Control")
@Service
public class CVaultCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + " search [index0..4]                            - Search for entries (blank index will not be searched)\n"
                            + " entry <id> | <secretid> (-t <target>)         - return entries\n"
                            + " create <groupId> [index0..4]                  - create a new entry with given data\n"
                            + " updatecreate <secretId> [index0..4]           - update an existing secret\n"
                            + " import <groupId> <secret> [index0..4]         - import an existing secret into a new entry\n"
                            + " updateimport <secretId> <secret> [index0..4]  - import a new secret into an existing entry\n"
                            + " updateindex <secretId> [index0..4]            - modify index of an existing entry\n"
                            + " test <group> [key=value]*                     - test the creation of a group, use -exec to create a real entry (not saved)\n"
                            + " dbexport <public key id> <file> [group]       - Export data for the management tool\n"
                            + " dbimport <private key id> <file>              - Import a merged export, use -a to import targets and groups\n"
                            + " cleanup [group]                               - Remove expired entries of the group/all\n"
                            + " groups\n"
                            + " targets\n"
                            + " secret <secretId>\n"
                            + " ",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    @Option(name = "-t", description = "Target", required = false, multiValued = false)
    String target;

    @Option(name = "-g", description = "Group", required = false, multiValued = false)
    String group;

    @Option(name = "-fr", description = "Valid From", required = false, multiValued = false)
    String fromStr;

    @Option(name = "-to", description = "Valid To", required = false, multiValued = false)
    String toStr;

    @Option(name = "-p", description = "Properties", required = false, multiValued = true)
    String p[];

    @Option(name = "-a", description = "All", required = false, multiValued = false)
    boolean all = false;

    @Option(name = "-exec", description = "Execute Test", required = false, multiValued = false)
    boolean execute = false;

    @Override
    public Object execute2() throws Exception {

        CherryVaultApi api = M.l(CherryVaultApi.class);

        Date from = MCast.toDate(fromStr, null);
        Date to = MCast.toDate(toStr, null);

        MProperties prop = IProperties.explodeToMProperties(p);

        switch (cmd) {
            case "secret":
                {
                    List<VaultEntry> res = null;
                    if (target == null) res = api.getSecrets(parameters[0]);
                    else {
                        res = new LinkedList<>();
                        VaultEntry r = api.getSecret(parameters[0], target);
                        if (r != null) res.add(r);
                    }
                    ConsoleTable table = new ConsoleTable(tblOpt);
                    table.setHeaderValues("id", "Group", "Target", "From", "To");
                    for (VaultEntry item : res) {
                        table.addRowValues(
                                item.getId(),
                                item.getGroup(),
                                item.getTarget(),
                                item.getValidFrom(),
                                item.getValidTo());
                    }
                    table.print();
                }
                break;
            case "groups":
                {
                    AQuery<VaultGroup> query = Db.query(VaultGroup.class);
                    if (!all) query.eq("enabled", true);
                    query.asc("name");
                    ConsoleTable out = new ConsoleTable(tblOpt);
                    out.setHeaderValues("Name", "Targets", "Generator", "Config", "Id", "Modified");
                    for (VaultGroup item : StaticAccess.db.getManager().getByQualification(query)) {
                        out.addRowValues(
                                item.getName() + (item.isEnabled() ? "" : "\n[disabled]"),
                                item.getTargets(),
                                item.getSecretGeneratorName(),
                                item.getSecretGeneratorConfig(),
                                item.getId(),
                                item.getModifyDate());
                    }
                    out.print();
                }
                break;
            case "targets":
                {
                    List<String> filter = null;
                    AQuery<VaultTarget> query = Db.query(VaultTarget.class);
                    if (!all) query.eq("enabled", true);
                    if (group != null) {
                        VaultGroup g =
                                StaticAccess.db
                                        .getManager()
                                        .getObjectByQualification(
                                                Db.query(VaultGroup.class).eq("name", group));
                        if (g == null) return null;
                        filter = g.getTargets();
                    }

                    query.asc("name");
                    ConsoleTable out = new ConsoleTable(tblOpt);
                    out.setHeaderValues(
                            "Name", "Processor", "Config", "Condition", "Config", "Id", "Modified");
                    for (VaultTarget item :
                            StaticAccess.db.getManager().getByQualification(query)) {
                        if (filter == null || filter.contains(item.getName()))
                            out.addRowValues(
                                    item.getName() + (item.isEnabled() ? "" : "\n[disabled]"),
                                    item.getProcessorName(),
                                    item.getProcessorConfig(),
                                    item.getConditionNames(),
                                    item.getConditionConfig(null),
                                    item.getId(),
                                    item.getModifyDate());
                    }
                    out.print();
                }
                break;
            case "dbimport":
                {
                    de.mhus.lib.core.vault.VaultEntry key =
                            MVaultUtil.loadDefault().getEntry(UUID.fromString(parameters[0]));
                    if (key == null) {
                        System.out.println("Key not found");
                        return null;
                    }
                    new ImportUtil()
                            .importDb(key.getValue().value(), parameters[1], parameters[2], all);
                }
                break;
            case "dbexport":
                {
                    de.mhus.lib.core.vault.VaultEntry key =
                            MVaultUtil.loadDefault().getEntry(UUID.fromString(parameters[0]));
                    if (key == null) {
                        System.out.println("Key not found");
                        return null;
                    }
                    new ExportUtil()
                            .exportDb(
                                    key.getValue().value(),
                                    parameters[1],
                                    parameters.length > 2 ? parameters[2] : null);
                }
                break;
            case "cleanup":
                {
                    api.cleanup(parameters == null || parameters.length < 1 ? null : parameters[0]);
                }
                break;
            case "test":
                {
                    String[] p = MCollection.cropArray(parameters, 1, parameters.length);
                    String out =
                            api.testGroup(
                                    parameters[0], execute, IProperties.explodeToMProperties(p));
                    System.out.println(out);
                }
                break;
            case "create":
                {
                    String[] index = MCollection.cropArray(parameters, 1, parameters.length);
                    String id = api.createSecret(parameters[0], from, to, prop, index);
                    System.out.println(id);
                }
                break;
            case "search":
                {
                    ConsoleTable table = new ConsoleTable(tblOpt);
                    table.setHeaderValues("id", "SecretId", "Group", "Target", "From", "To");
                    for (VaultEntry item : VaultApiImpl.instance.search(group, target, parameters, 100, all, false)) {
                        table.addRowValues(
                                item.getId(),
                                item.getSecretId(),
                                item.getGroup(),
                                item.getTarget(),
                                item.getValidFrom(),
                                item.getValidTo());
                    }
                    table.print();
                }
                break;
            case "entry":
                {
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
                }
                break;
            case "updateindex":
                {
                    String secretId = parameters[0];
                    String[] index = MCollection.cropArray(parameters, 1, parameters.length);
                    api.indexUpdate(secretId, index);
                    System.out.println("OK");
                }
                break;
            case "updatecreate":
                {
                    String secretId = parameters[0];
                    String[] index = MCollection.cropArray(parameters, 1, parameters.length);
                    api.createUpdate(secretId, from, to, prop, index);
                    System.out.println("OK");
                }
                break;
            case "import":
                {
                    String[] index = MCollection.cropArray(parameters, 2, parameters.length);
                    String id =
                            api.importSecret(parameters[0], from, to, parameters[1], prop, index);
                    System.out.println(id);
                }
                break;
            case "updateimport":
                {
                    String[] index = MCollection.cropArray(parameters, 2, parameters.length);
                    api.importUpdate(parameters[0], from, to, parameters[1], prop, index);
                    System.out.println("OK");
                }
                break;
            default:
                System.out.println("Unknown command " + cmd);
        }

        return null;
    }
}
