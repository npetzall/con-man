package npetzall.conman.server.jdbi;

import npetzall.conman.server.api.Configuration;
import npetzall.conman.server.jdbi.mapper.ConfigurationMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public abstract class ConfigurationDAO {

    public enum Event {
        CREATED, UPDATED, UNMODIFIED
    }

    @Mapper(ConfigurationMapper.class)
    @SqlQuery("select service, key, env, value from configuration where service = :service and env = :env")
    public abstract List<Configuration> fetchAllForService(
            @Bind("service") String service,
            @Bind("env") String env
    );

    @Mapper(ConfigurationMapper.class)
    @SqlQuery("select service, key, env, value from configuration where service = :service and key = :key and env = :env")
    public abstract Configuration fetchConfigurationForService(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("env") String env
    );

    public Event createOrUpdate(String service, String key, String env, String value) {
        Configuration config = fetchConfigurationForService(service, key, env);
        if (config == null) {
            if (addConfiguration(service, key, env, value) == 1) {
                return Event.CREATED;
            } else {
                return Event.UNMODIFIED;
            }
        } else if (config.getValue().equals(value)) {
            return Event.UNMODIFIED;
        } else {
            if (updateValue(service, key, env, value) == 1) {
                return Event.UPDATED;
            } else {
                return Event.UNMODIFIED;
            }
        }
    }

    @SqlUpdate("insert into configuration (service,key, env, value) values (:service,:key,:env,:value)")
    protected abstract int addConfiguration(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("env") String env,
            @Bind("value")String value
    );

    @SqlUpdate("update configuration set value = :value where service = :service and key = :key and env = :env")
    protected abstract int updateValue(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("env") String env,
            @Bind("value")String value
    );
}
