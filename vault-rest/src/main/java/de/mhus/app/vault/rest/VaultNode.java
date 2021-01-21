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
package de.mhus.app.vault.rest;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.app.vault.api.CherryVaultApi;
import de.mhus.app.vault.api.model.VaultEntry;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.UsageException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestAction;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.ObjectListNode;
import de.mhus.rest.core.result.JsonResult;

@Component(immediate = true, service = RestNodeService.class)
@RestNode(name = "vault", parent = Node.ROOT_PARENT)
public class VaultNode extends ObjectListNode<VaultEntry, VaultEntry> {

    @Override
    protected List<VaultEntry> getObjectList(CallContext callContext) throws MException {
        // we will not support browsing - but searching

        String group = callContext.getParameter("target");
        if (group == null) return new EmptyList<VaultEntry>();

        String target = callContext.getParameter("target");

        String[] index = new String[5];
        for (int i = 0; i < index.length; i++) index[i] = callContext.getParameter("index" + i);

        CherryVaultApi api = M.l(CherryVaultApi.class);

        return api.search(group, target, index, 100, false);
    }

    //	@Override
    //	public Class<VaultEntry> getManagedClass() {
    //		return VaultEntry.class;
    //	}

    @Override
    protected VaultEntry getObjectForId(CallContext context, String id) throws Exception {
        int p = id.indexOf(':');
        if (p <= 0) return null;
        String target = id.substring(p + 1);
        id = id.substring(0, p);
        CherryVaultApi api = M.l(CherryVaultApi.class);
        return api.getSecret(id, target);
    }

    @Override
    public void doUpdate(JsonResult result, CallContext callContext) throws Exception {
        String secret = callContext.getParameter("_secret");
        String secretId = callContext.getParameter("_secretId");
        if (secretId == null) {
            secretId = getIdFromContext(callContext);
            int p = secretId.indexOf(':');
            if (p >= 0) secretId = secretId.substring(0, p); // ignore target
        }
        if (secretId == null) throw new UsageException("secret id not found");
        Date validFrom = MDate.toDate(callContext.getParameter("_validFrom"), null);
        Date validTo = MDate.toDate(callContext.getParameter("_validTo"), null);

        MProperties properties = new MProperties();
        for (String name : callContext.getParameterNames())
            if (!name.startsWith("_")) properties.put(name, callContext.getParameter(name));
            else if (name.startsWith("__"))
                properties.put(name.substring(1), callContext.getParameter(name));

        String[] index = new String[5];
        for (int i = 0; i < index.length; i++) index[i] = callContext.getParameter("_index" + i);

        CherryVaultApi api = M.l(CherryVaultApi.class);

        if (secret != null) {
            api.importUpdate(secretId, validFrom, validTo, secret, properties, index);
        } else {
            api.createUpdate(secretId, validFrom, validTo, properties, index);
        }
    }

    @Override
    public void doCreate(JsonResult result, CallContext callContext) throws Exception {
        String groupName = callContext.getParameter("_group");
        String secret = callContext.getParameter("_secret");
        Date validFrom = MDate.toDate(callContext.getParameter("_validFrom"), null);
        Date validTo = MDate.toDate(callContext.getParameter("_validTo"), null);

        MProperties properties = new MProperties();
        for (String name : callContext.getParameterNames())
            if (!name.startsWith("_")) properties.put(name, callContext.getParameter(name));
            else if (name.startsWith("__"))
                properties.put(name.substring(1), callContext.getParameter(name));

        CherryVaultApi api = M.l(CherryVaultApi.class);

        String[] index = new String[5];
        for (int i = 0; i < index.length; i++) index[i] = callContext.getParameter("_index" + i);

        if (secret != null) {
            String secretId =
                    api.importSecret(groupName, validFrom, validTo, secret, properties, index);
            ObjectNode res = result.createObjectNode();
            res.put("secretId", secretId);
        } else {
            String secretId = api.createSecret(groupName, validFrom, validTo, properties, index);
            ObjectNode res = result.createObjectNode();
            res.put("secretId", secretId);
        }
    }

    @Override
    public void doDelete(JsonResult result, CallContext callContext) throws Exception {
        String secretId = callContext.getParameter("_secretId");
        if (secretId == null) {
            secretId = getIdFromContext(callContext);
            int p = secretId.indexOf(':');
            if (p >= 0) secretId = secretId.substring(0, p); // ignore target
        }
        if (secretId == null) throw new UsageException("secret id not found");

        CherryVaultApi api = M.l(CherryVaultApi.class);

        api.deleteSecret(secretId);
    }

    @Override
    protected PojoModelFactory getPojoModelFactory() {
        return M.l(CherryVaultApi.class).getManager().getPojoModelFactory();
    }

    @RestAction(name = "indexes")
    public void onIndexes(JsonResult result, CallContext callContext) throws Exception {

        String secretId = callContext.getParameter("_secretId");
        if (secretId == null) {
            secretId = getIdFromContext(callContext);
            int p = secretId.indexOf(':');
            if (p >= 0) secretId = secretId.substring(0, p); // ignore target
        }
        if (secretId == null) throw new UsageException("secret id not found");

        String[] index = new String[5];
        for (int i = 0; i < index.length; i++) index[i] = callContext.getParameter("_index" + i);

        CherryVaultApi api = M.l(CherryVaultApi.class);

        api.indexUpdate(secretId, index);

        result.createObjectNode().put("secretId", secretId);
    }
}
