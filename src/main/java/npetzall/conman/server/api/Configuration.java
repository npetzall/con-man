package npetzall.conman.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {

    private String service;
    private String key;
    private String value;

    public Configuration() { /* json deserializer */ }

    public Configuration(String service, String key, String value) {
        this.service = service;
        this.key = key;
        this.value = value;
    }

    @JsonProperty
    public String getService() {
        return service;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }

}
