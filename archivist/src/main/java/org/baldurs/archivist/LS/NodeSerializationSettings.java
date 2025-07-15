package org.baldurs.archivist.LS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baldurs.archivist.LS.Resources.LSF.LSFMetadataFormat;

/**
 * Node serialization settings class
 */
public class NodeSerializationSettings {
    public boolean defaultByteSwapGuids = true;
    public boolean byteSwapGuids = true;
    public LSFMetadataFormat lsfMetadata = LSFMetadataFormat.NONE;
    
    public void initFromMeta(String meta) {
        if (meta.isEmpty()) {
            // No metadata available, use defaults
            byteSwapGuids = defaultByteSwapGuids;
            lsfMetadata = LSFMetadataFormat.NONE;
        } else {
            String[] tags = meta.split(",");
            byteSwapGuids = Arrays.asList(tags).contains("bswap_guids");
            
            lsfMetadata = LSFMetadataFormat.NONE;
            if (Arrays.asList(tags).contains("lsf_adjacency")) {
                lsfMetadata = LSFMetadataFormat.NONE2;
            } else if (Arrays.asList(tags).contains("lsf_keys_adjacency")) {
                lsfMetadata = LSFMetadataFormat.KEYS_AND_ADJACENCY;
            }
        }
    }
    
    public String buildMeta() {
        List<String> tags = new ArrayList<>();
        tags.add("v1");
        
        if (byteSwapGuids) {
            tags.add("bswap_guids");
        }
        
        if (lsfMetadata == LSFMetadataFormat.NONE2) {
            tags.add("lsf_adjacency");
        }
        
        if (lsfMetadata == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
            tags.add("lsf_keys_adjacency");
        }
        
        return String.join(",", tags);
    }
} 