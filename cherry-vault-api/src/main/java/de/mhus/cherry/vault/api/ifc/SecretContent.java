package de.mhus.cherry.vault.api.ifc;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.SecureString;

public class SecretContent {
	
	private SecureString content;
	private MProperties properties;
	
	public SecretContent() {}
	
	public SecretContent(SecureString content, MProperties properties) {
		this.content = content;
		this.properties = properties;
	}

	
	
	public SecureString getContent() {
		return content;
	}

	public IReadProperties getProperties() {
		return properties;
	}

}
