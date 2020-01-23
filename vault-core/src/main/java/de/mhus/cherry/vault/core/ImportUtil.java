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
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
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

    public void importDb(String privateKey, String passphrase, String file, boolean all) throws MException, IOException {
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
        
        if (all) {
            importGroups();
            
            importTargets();
            
            importVault();
        }
        
        zip.close();
    }

    private void importVault() {
        XdbService db = StaticAccess.db.getManager();

        for (ZipEntry zipEntry : new EnumerationIterator<ZipEntry>(zip.entries())) {
            try {
                if (zipEntry.getName().startsWith("key/")) {
                    UUID id = UUID.fromString(zipEntry.getName().substring(4));
                    VaultKey key = db.getObject(VaultKey.class, id);
                    if (key == null) {
                        key = db.inject(new VaultKey());
                        System.out.println(">>> Create Key: "+ id);
                    } else {
                        System.out.println(">>> Update Key: "+ id);
                    }
                    
                    JsonNode json = MJson.load(load(zipEntry.getName()));
                    MPojo.jsonToPojo(json, key);

                    key.save();

                }
            } catch (Throwable t) {
                log().e(zipEntry.getName(),t);
            }
        }
    }

    private void importTargets() throws MException {
        XdbService db = StaticAccess.db.getManager();
        
        for (VaultTarget target : db.getAll(VaultTarget.class)) {
            if (target.isEnabled()) {
                System.out.println(">>> Disable Target: " + target.getName() + " " + target.getId());
                target.setEnabled(false);
                target.save();
            }
        }
        
        for (ZipEntry zipEntry : new EnumerationIterator<ZipEntry>(zip.entries())) {
            try {
                if (zipEntry.getName().startsWith("target/")) {
                    UUID id = UUID.fromString(zipEntry.getName().substring(7));
                    VaultTarget target = db.getObject(VaultTarget.class, id);
                    if (target == null) {
                        target = db.inject(new VaultTarget());
                        System.out.println(">>> Create Target: "+ id);
                    } else {
                        System.out.println(">>> Update Target: "+ id);
                    }
                    
                    JsonNode json = MJson.load(load(zipEntry.getName()));
                    MPojo.jsonToPojo(json, target);
                    
                    target.save();

                }
            } catch (Throwable t) {
                log().e(zipEntry.getName(),t);
            }
        }
    }

    private void importGroups() throws MException {
        XdbService db = StaticAccess.db.getManager();
        
        for (VaultGroup group : db.getAll(VaultGroup.class)) {
            if (group.isEnabled()) {
                System.out.println(">>> Disable Group: " + group.getName() + " " + group.getId());
                group.setEnabled(false);
                group.save();
            }
        }
        
        for (ZipEntry zipEntry : new EnumerationIterator<ZipEntry>(zip.entries())) {
            try {
                if (zipEntry.getName().startsWith("group/")) {
                    UUID id = UUID.fromString(zipEntry.getName().substring(6));
                    VaultGroup group = db.getObject(VaultGroup.class, id);
                    if (group == null) {
                        group = db.inject(new VaultGroup());
                        System.out.println(">>> Create Group: "+ id);
                    } else {
                        System.out.println(">>> Update Group: "+ id);
                    }
                    
                    JsonNode json = MJson.load(load(zipEntry.getName()));
                    MPojo.jsonToPojo(json, group);
                    
                    group.save();
                }
            } catch (Throwable t) {
                log().e(zipEntry.getName(),t);
            }
        }
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
                        System.out.println(">>> Create Entry " + id);
                    } else {
                        System.out.println(">>> Update Entry " + id);
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
