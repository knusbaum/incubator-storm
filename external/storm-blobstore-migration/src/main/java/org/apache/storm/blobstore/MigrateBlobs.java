package org.apache.storm.blobstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.storm.Config;
import org.apache.storm.blobstore.BlobStore;
import org.apache.storm.hdfs.blobstore.HdfsBlobStore;
import org.apache.storm.nimbus.NimbusInfo;
import org.apache.storm.utils.Utils;
import org.apache.storm.blobstore.LocalFsBlobStore;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.KeyAlreadyExistsException;
import org.apache.storm.generated.KeyNotFoundException;
import org.apache.storm.generated.ReadableBlobMeta;
import org.apache.storm.generated.SettableBlobMeta;

public class MigrateBlobs {
    
    protected static void deleteAllBlobStoreKeys(BlobStore bs, Subject who) throws AuthorizationException, KeyNotFoundException {
        Iterable<String> hdfsKeys = () -> bs.listKeys();
        for(String key : hdfsKeys) {
            System.out.println(key);
            bs.deleteBlob(key, who);
        }
    }
    
    protected static void copyBlobStoreKeys(BlobStore bsFrom, Subject whoFrom, BlobStore bsTo, Subject whoTo) throws AuthorizationException, KeyAlreadyExistsException, IOException, KeyNotFoundException {
        Iterable<String> lfsKeys = () -> bsFrom.listKeys();
        for(String key : lfsKeys) {
            ReadableBlobMeta readable_meta = bsFrom.getBlobMeta(key, whoFrom);
            SettableBlobMeta meta = readable_meta.get_settable();
            InputStream in = bsFrom.getBlob(key, whoFrom);
            System.out.println("COPYING BLOB " + key + " FROM " + bsFrom + " TO " + bsTo);
            bsTo.createBlob(key, in, meta, whoTo);
            System.out.println("DONE CREATING BLOB " + key);
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        Map<String, Object> hdfsConf = Utils.readStormConfig();
        
        if (args.length < 2) {
            System.out.println("Need at least 2 arguments, but have " + Integer.toString(args.length));
            System.out.println("migrate <local_blobstore_dir> <hdfs_blobstore_path> <hdfs_principal> <keytab>");
            System.out.println("Migrates blobs from LocalFsBlobStore to HdfsBlobStore");
            System.out.println("Example: migrate '/srv/storm' 'hdfs://some-hdfs-namenode:8080/srv/storm/my-storm-blobstore' 'stormUser/my-nimbus-host.example.com@STORM.EXAMPLE.COM' '/srv/my-keytab/stormUser.kt'");
            System.exit(1);
        }
        
        String localBlobstoreDir = args[0];
        String hdfsBlobstorePath = args[1];
        
        hdfsConf.put(Config.BLOBSTORE_DIR, hdfsBlobstorePath);
        hdfsConf.put(Config.STORM_PRINCIPAL_TO_LOCAL_PLUGIN, "org.apache.storm.security.auth.DefaultPrincipalToLocal");
        if(args.length >= 3) {
        	System.out.println("SETTING HDFS PRINCIPAL!");
        	hdfsConf.put(Config.BLOBSTORE_HDFS_PRINCIPAL, args[2]);
        }
        if(args.length >= 4) {
        	System.out.println("SETTING HDFS KEYTAB!");
        	hdfsConf.put(Config.BLOBSTORE_HDFS_KEYTAB, args[3]);
        }
        hdfsConf.put(Config.STORM_BLOBSTORE_REPLICATION_FACTOR, 7);
        
        Map<String, Object> lfsConf = Utils.readStormConfig();
        lfsConf.put(Config.BLOBSTORE_DIR, localBlobstoreDir);
        lfsConf.put(Config.STORM_PRINCIPAL_TO_LOCAL_PLUGIN, "org.apache.storm.security.auth.DefaultPrincipalToLocal");
        
        
        /* CREATE THE BLOBSTORES */
        System.out.println("Creating Local Blobstore.");
        LocalFsBlobStore lfsBlobStore = new LocalFsBlobStore();
        lfsBlobStore.prepare(lfsConf, null, NimbusInfo.fromConf(lfsConf));
        System.out.print("Done.");
        
        System.out.println("Creating HDFS blobstore.");
        HdfsBlobStore hdfsBlobStore = new HdfsBlobStore();
        hdfsBlobStore.prepare(hdfsConf, null, null);
        System.out.print("Done.");
        
        
        /* LOOK AT LOCAL BLOBSTORE */
        System.out.println("Listing local blobstore keys.");
        MigratorMain.listBlobStoreKeys(lfsBlobStore, null);
        System.out.println("Done listing local blobstore keys.");
        
        /* LOOK AT HDFS BLOBSTORE */
        System.out.println("Listing HDFS blobstore keys.");
        MigratorMain.listBlobStoreKeys(hdfsBlobStore, null);
        System.out.println("Done listing HDFS blobstore keys.");
        
        
        System.out.println("Going to delete everything in HDFS, then copy all local blobs to HDFS. Continue? [Y/n]");
        String resp = System.console().readLine().toLowerCase().trim();
        if (!(resp.equals("y") || resp.equals(""))) {
            System.out.println("Not copying blobs. Exiting. [" + resp.toLowerCase().trim() + "]");
            System.exit(1);
        }
        
        /* DELETE EVERYTHING IN HDFS */
        System.out.println("Deleting blobs from HDFS.");
        deleteAllBlobStoreKeys(hdfsBlobStore, null);
        System.out.println("DONE deleting blobs from HDFS.");
        
        /* COPY EVERYTHING FROM LOCAL BLOBSTORE TO HDFS */
        System.out.println("Copying local blobstore keys.");
        copyBlobStoreKeys(lfsBlobStore, null, hdfsBlobStore, null);
        System.out.println("DONE Copying local blobstore keys.");
        
        /* LOOK AT HDFS BLOBSTORE AGAIN */
        System.out.println("Listing HDFS blobstore keys.");
        MigratorMain.listBlobStoreKeys(hdfsBlobStore, null);
        System.out.println("Done listing HDFS blobstore keys.");
        
        hdfsBlobStore.shutdown();
        System.out.println("Done.");
    }
}
