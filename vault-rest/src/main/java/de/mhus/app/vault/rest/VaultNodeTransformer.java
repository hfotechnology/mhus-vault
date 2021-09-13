package de.mhus.app.vault.rest;

import de.mhus.app.vault.api.CherryVaultApi;
import de.mhus.lib.core.M;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.rest.core.transform.PojoTransformer;

public class VaultNodeTransformer extends PojoTransformer {

    @Override
    protected PojoModelFactory getPojoModelFactory() {
        return M.l(CherryVaultApi.class).getManager().getPojoModelFactory();
    }

}
