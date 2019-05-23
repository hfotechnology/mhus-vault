package de.mhus.cherry.vault.core.services;

import java.io.PrintStream;
import java.util.UUID;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptApi;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

public class SignerUtil {

    public static void test(PrintStream out, IProperties properties, IReadProperties processorConfig) throws Exception {
        if (processorConfig.isProperty("signId")) {
            UUID signId = UUID.fromString(processorConfig.getString("signId"));
            out.println("Signature: " + signId);
            MVault vault = MVaultUtil.loadDefault();
            de.mhus.lib.core.vault.VaultEntry signKeyValue = vault.getEntry(signId);
            out.println("Key: " + signKeyValue);
            CryptApi api = M.l(CryptApi.class);
            SignerProvider signer = api.getSigner(processorConfig.getString("signService", "DSA-1"));
            out.println("Signer: " +  signer);
        }
    }

    public static void sign(PemBlockList result, IReadProperties processorConfig, String cs) throws MException {
        if (processorConfig.isProperty("signId")) {
            MVault vault = MVaultUtil.loadDefault();
            CryptApi api = M.l(CryptApi.class);
            UUID signId = UUID.fromString(processorConfig.getString("signId"));
            SignerProvider signer = api.getSigner(processorConfig.getString("signService", "DSA-1"));
            de.mhus.lib.core.vault.VaultEntry signKeyValue = vault.getEntry(signId);
            if (signKeyValue == null) throw new NotFoundException("sign key not found",signId);
            PemPriv signKey = PemUtil.toKey(signKeyValue.getValue().value());
            PemBlock signed = signer.sign(signKey, cs, processorConfig.getString("signPassphrase", null));
            result.add(signed);
        }
    }

}
