package org.baldurs.archivist.LS;

import java.util.HashMap;
import java.util.Map;

import org.baldurs.archivist.LS.Resources.LSF.LSFMetadataFormat;

/**
 * Resource class and related structures ported from C# Resource.cs
 */
public class Resource {
    public LSMetadata metadata;
    public LSFMetadataFormat metadataFormat = null;
    public Map<String, Region> regions = new HashMap<>();

    public Resource() {
        metadata = new LSMetadata();
        metadata.majorVersion = 3;
    }
} 