package de.mhus.cherry.vault.rest;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.UsageException;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.rest.AbstractObjectListNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true,provide=RestNodeService.class)
public class VaultNode extends AbstractObjectListNode<VaultEntry>{

	private PojoModelFactory pojoModelFactory = new PojoModelFactory() {

		@Override
		public PojoModel createPojoModel(Class<?> pojoClass) {
			if (pojoClass == VaultEntry.class) {
				try {
					return MApi.lookup(CherryVaultApi.class).getEntryPojoModel();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			return null;
		}
		
	};

	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID};
	}

	@Override
	public String getNodeId() {
		return "vault";
	}

	@Override
	protected List<VaultEntry> getObjectList(CallContext callContext) throws MException {
		// we will not support browsing
		return new EmptyList<>();
	}

	@Override
	public Class<VaultEntry> getManagedClass() {
		return VaultEntry.class;
	}

	@Override
	protected VaultEntry getObjectForId(CallContext context, String id) throws Exception {
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		return api.getSecret(id, context.getParameter("target"));
	}

	@Override
	public void doUpdate(JsonResult result, CallContext callContext) throws Exception {
		String secret = callContext.getParameter("_secret");
		String secredId = callContext.getParameter("_secretId");
		if (secredId == null)
			secredId = getIdFromContext(callContext);
		if (secredId == null)
			throw new UsageException("secred id not found");
		Date validFrom = MDate.toDate(callContext.getParameter("_validFrom"), null);
		Date validTo = MDate.toDate(callContext.getParameter("_validTo"), null);
		
		MProperties properties = new MProperties();
		for (String name : callContext.getParameterNames())
			if (!name.startsWith("_"))
				properties.put(name, callContext.getParameter(name));
			else
			if (name.startsWith("__"))
				properties.put(name.substring(1), callContext.getParameter(name));
		
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		
		if (secret != null) {
			api.importUpdate(secredId, validFrom, validTo, secret, properties);
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
			if (!name.startsWith("_"))
				properties.put(name, callContext.getParameter(name));
			else
			if (name.startsWith("__"))
				properties.put(name.substring(1), callContext.getParameter(name));
		
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		
		if (secret != null) {
			String secretId = api.importSecret(groupName, validFrom, validTo, secret, properties);
			ObjectNode res = result.createObjectNode();
			res.put("secredId", secretId);
		}
		
	}
	@Override
	public void doDelete(JsonResult result, CallContext callContext) throws Exception {
		// TODO rollback ?
	}
	
	protected PojoModelFactory getPojoModelFactory() {
		return pojoModelFactory;
	}
	
}
