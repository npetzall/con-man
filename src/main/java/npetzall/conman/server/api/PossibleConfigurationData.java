package npetzall.conman.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PossibleConfigurationData {
    private String description;
    private String valueRestriction;
    private String valueRestrictionType;

    public PossibleConfigurationData() { /* json deserializer */ }

    public PossibleConfigurationData(String description, String valueRestriction, String valueRestrictionType) {
        this.description = description;
        this.valueRestriction = valueRestriction;
        this.valueRestrictionType = valueRestrictionType;
    }

    public PossibleConfigurationData(PossibleConfigurationData possibleConfigurationData) {
        this.description = possibleConfigurationData.getDescription();
        this.valueRestriction = possibleConfigurationData.getValueRestriction();
        this.valueRestrictionType = possibleConfigurationData.getValueRestrictionType();
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public String getValueRestriction() {
        return valueRestriction;
    }

    @JsonProperty
    public String getValueRestrictionType() {
        return valueRestrictionType;
    }
}
