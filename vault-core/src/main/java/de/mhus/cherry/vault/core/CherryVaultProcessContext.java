package de.mhus.cherry.vault.core;

import java.util.UUID;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.util.SimplePemProcessContext;
import de.mhus.osgi.sop.api.aaa.AaaContext;

public class CherryVaultProcessContext extends SimplePemProcessContext {

	private IReadProperties properties;
	private AaaContext ac;
	
	public CherryVaultProcessContext(AaaContext ac, IReadProperties properties) {
		this.ac = ac;
		this.properties = properties;
	}
	
	@Override
	public PemPriv getPrivateKey(String privId) throws MException {
		PemPriv privKey = super.getPrivateKey(privId);
		if (privKey != null) return privKey;
		
		if (!MCollection.contains( ac.getAccount().getAttributes().getString("privateKey", ""), ',', privId))
			throw new AccessDeniedException("The private key is not owned by the current user",ac,privId);
		de.mhus.lib.core.vault.VaultEntry privKeyObj = MVaultUtil.loadDefault().getEntry(UUID.fromString(privId ) );
		if (privKeyObj == null) throw new NotFoundException("Private key not found",privId);

		privKey = PemUtil.toKey(privKeyObj.getValue().value());
		
		addPassphrase(privId, new SecureString(properties.getString("passphrase", null)));
		
		return privKey;
	}

}
