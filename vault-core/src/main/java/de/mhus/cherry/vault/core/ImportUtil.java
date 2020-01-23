package de.mhus.cherry.vault.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.codehaus.jackson.JsonNode;

import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.core.impl.StaticAccess;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.util.EnumerationIterator;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.crypt.api.util.CryptUtil;

public class ImportUtil extends MLog {

    private String privateKey;
    @SuppressWarnings("unused")
    private File file;
    private ZipFile zip;
    private String passphrase;

    public void importDb(String privateKey, String passphrase, String file) throws MException, IOException {
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.file = new File(file);

        if (CryptUtil.getCipher(privateKey) == null) throw new MException("cipher not found");

        zip = new ZipFile(file);
        
        // load meta data
        MProperties meta = MProperties.loadFromString(loadPlain("meta.properties"));
        if (!"export".equals(meta.getString("type")))
                throw new MException("file is not an export");
        
        // import entries
        importEntries();
        
        zip.close();
    }

    private void importEntries() {
        XdbService db = StaticAccess.db.getManager();
        for (ZipEntry zipEntry : new EnumerationIterator<ZipEntry>(zip.entries())) {
            try {
                if (zipEntry.getName().startsWith("entry/")) {
                    UUID id = UUID.fromString(zipEntry.getName().substring(6));
                    VaultEntry entry = db.getObject(VaultEntry.class, id);
                    if (entry == null) {
                        entry = db.inject(new VaultEntry());
                        System.out.println(">>> Create " + entry.getGroup() + " " + entry.getTarget() + " " + entry.getId());
                    } else {
                        System.out.println(">>> Update " + entry.getGroup() + " " + entry.getTarget() + " " + entry.getId());
                    }
                    
                    JsonNode json = MJson.load(load(zipEntry.getName()));
                    MPojo.jsonToPojo(json, entry);
                    
                    entry.save();
                    
                }
            } catch (Throwable t) {
                log().e(zipEntry.getName(),t);
            }
        }
        
    }

    private String loadPlain(String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        InputStream is = zip.getInputStream(entry);
        return MFile.readFile(is);
    }
    
    private String load(String name) throws MException, IOException {
        String content = loadPlain(name);
        content = CryptUtil.decrypt(privateKey, passphrase, content);
        return content;
    }
    
}
