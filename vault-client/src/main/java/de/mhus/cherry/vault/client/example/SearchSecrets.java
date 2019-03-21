package de.mhus.cherry.vault.client.example;

import java.io.IOException;
import java.util.List;

import de.mhus.cherry.vault.client.SecretEntry;
import de.mhus.cherry.vault.client.VaultClientConnection;
import de.mhus.lib.errors.MException;

public class SearchSecrets {

    public static void main(String[] args) throws IOException, MException {
        VaultClientConnection con = new VaultClientConnection("http://localhost:8181", "admin", "123");
        List<SecretEntry> list = con.search("test", "alf");
        for (SecretEntry item : list)
            System.out.println(item);
    }

}
