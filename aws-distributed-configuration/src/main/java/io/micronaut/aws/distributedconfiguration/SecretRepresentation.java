package io.micronaut.aws.distributedconfiguration;

/**
 * Representation of Secret mappings that will be used to create a AWS SDK request.
 * @author Matej Nedic
 * @since ?
 */
public class SecretRepresentation {

    private String key;
    private String versionId;

    public SecretRepresentation(String key, String versionId) {
        this.key = key;
        this.versionId = versionId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
}
