package de.mhus.cherry.vault.api.model;

import de.mhus.lib.mongo.MoMetadata;

public class VaultKey extends MoMetadata {

	String algorithm;
	String privateKey;
	String publicKey;
	
}
