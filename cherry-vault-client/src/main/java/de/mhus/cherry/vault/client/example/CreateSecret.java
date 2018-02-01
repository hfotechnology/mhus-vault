package de.mhus.cherry.vault.client.example;

import java.io.IOException;

import de.mhus.cherry.vault.client.VaultClientConnection;
import de.mhus.lib.errors.MException;

public class CreateSecret {

	public static void main(String[] args) throws IOException, MException {
		VaultClientConnection con = new VaultClientConnection("http://localhost:8181", "admin", "123");
		String secretId = con.createSecret("test", null);
		System.out.println("SecretId: " + secretId);
	}
}
