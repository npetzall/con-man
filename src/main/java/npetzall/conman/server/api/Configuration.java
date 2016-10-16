package npetzall.conman.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {

    public static final String DEFAULT_ENV = "default";

    private String service;
    private String key;
    private String env;
    private String value;

    public Configuration() { /* json deserializer */ }

    public Configuration(String service, String key, String value) {
        this(service,key,DEFAULT_ENV, value);
    }

    public Configuration(String service, String key, String env, String value) {
        this.service = service;
        this.key = key;
        this.env = env;
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
    public String getEnv() {
        return env;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }

}
