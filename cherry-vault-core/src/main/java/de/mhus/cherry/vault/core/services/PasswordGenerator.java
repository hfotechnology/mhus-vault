package de.mhus.cherry.vault.core.services;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.SecretGenerator;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.util.SecureString;

@Component(properties="name=password")
public class PasswordGenerator implements SecretGenerator {

	@Override
	public SecretContent generateSecret(VaultGroup group, IProperties param) {
		int min          = param.getInt("password.min", 6);
		int max          = param.getInt("password.max", 20);
		boolean upper    = param.getBoolean("password.upper", true);
		boolean numbers  = param.getBoolean("password.numbers", true);
		boolean specials = param.getBoolean("password.specials", true);
		SecureString pw = new SecureString( MPassword.generate(min, max, upper, numbers, specials) );
		return new SecretContent(pw,null);
	}

}
