package de.mhus.cherry.vault.client.example;

import java.io.IOException;
import java.util.UUID;

import de.mhus.cherry.vault.client.DsaSigner;
import de.mhus.cherry.vault.client.RsaCipher;
import de.mhus.cherry.vault.client.SecretEntry;
import de.mhus.cherry.vault.client.VaultClientConnection;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemKey;
import de.mhus.lib.errors.MException;

public class RequestSecret {

	
	public static void main(String[] args) throws IOException, MException {
		
		PemKey key = new PemKey(PemBlock.BLOCK_PRIV, 
				"MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK"+
				"PqyvWAgPIxW1hywKNOxqycKPfijbs/NPu0E2XmhWR43Z8tluvH"+
				"xn9BJVStfqq46BIWi5nBO8mlVFwrGcXhMmguf/5q9117Cq1JjE"+
				"ArtWPMTUfP6eXxgEwBoECY3veOvh4wH4m0TNhi1UQRy4QwgTcE"+
				"nk9XgaqP6d5M0riYIsofAgMBAAECgYBDQhUTVS4TspLjoPpQSA"+
				"OXUeAxjmFPgqjv2rzW5Ba52io9pUw25Nsa3hU+QI4n6QU4Xs3b"+
				"QgGRROhvhTPnEPdIQji04twamaHCpaD1cQR9y5knNw3rIQ3gyC"+
				"bwgpJNRdRmfy0HVbC/SJZ3MvG+R4T/eLU87JNU1GJN8PpGIc0E"+
				"iQJBAP48gPDXjotCKPdk3oS4MrDjqHsd0AxfFjNSSeEqRd+mUM"+
				"UjFfxY49ChRxRqydJynJ+aE7DwSfv1gw91gqwopOsCQQClDeRa"+
				"dgt8XrNMSKcjbKmK63nNQFwpan78M+RUbKiLKI3qI30DPsNZly"+
				"2h8vr1IDvr+T2y/5q/UQK4ikvAD3KdAkEA0gsouFJOyUzqzaH/"+
				"mYUZFD2yV61E0sPIcrQ8p5OmUOV2e7jGFEtYRGjKcdrcTUs1jU"+
				"Ldm+SaK1kmY6JBSHz36wJBAJCAOQt5jVS1FAQVFz7MQyJ8RYaM"+
				"ciNQORxT0fKXrncmahgyOaA9sokc8bZ3I363Wb6d1oZ/gNs98U"+
				"Pez7K104ECQEwZWd2DFmX/jUFVGQs1D2q5JiFbHUGCdHpp1tgS"+
				"kk/20Rm91Xd1PQvgZo6by9HjiQ+uyIGUaK0APrq085pcds0="
				, true);
		
		PemKey signKey = new PemKey(PemBlock.BLOCK_PUB,
				"MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC"+
				"7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbL"+
				"m1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1"+
				"fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3R"+
				"SAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd"+
				"7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCB"+
				"gLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbq"+
				"N/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1Ul"+
				"ZAFMO/7PSSoDgYQAAoGAZQ/du8+gqTGtYVyEf5R3gV0Ge21rsI"+
				"UofAL+TkG+ntDh35wI1y1wCKEE7BcWfVEXcpco9fQK9f1AKbUZ"+
				"55446yPykUFtrXhJLkCuPYyACVgbEJzcjmec7/EmJZRQDgggbv"+
				"IH7Pd7it7w084lPz9DTUl0nXF4084nqvhOmaKDfmw="
				, false);
		
		// load entry from server
		VaultClientConnection con = new VaultClientConnection("http://localhost:8181", "admin", "123");
		SecretEntry entry = con.getSecret(UUID.fromString("7d693d2c-6474-4f7f-a734-7a77e906e7df"), "test");
		System.out.println("Entry: " + entry);
		
		// encode with private key
		PemBlockList blockList = entry.getSecretBlock();
		PemBlock cipherBlock = blockList.find(PemBlock.BLOCK_CIPHER);
		String secret = RsaCipher.decode(key, cipherBlock, null);
		System.out.println("Secret: " + secret);
	
		// check sign
		PemBlock signBlock = blockList.find(PemBlock.BLOCK_SIGN);
		boolean valide = DsaSigner.validate(signKey, cipherBlock.toString(), signBlock);
		System.out.println("Valide: " + valide);
		
	}
}
