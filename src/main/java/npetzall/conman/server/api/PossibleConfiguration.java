package npetzall.conman.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PossibleConfiguration extends PossibleConfigurationData {
    private String service;
    private String key;

    public PossibleConfiguration() { /* json deserializer */ }

    public PossibleConfiguration(String service, String key, String description, String valueRestriction, String valueRestrictionType) {
        super(description, valueRestriction, valueRestrictionType);
        this.service = service;
        this.key = key;
    }

    @JsonProperty
    public String getService() {
        return service;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

}
