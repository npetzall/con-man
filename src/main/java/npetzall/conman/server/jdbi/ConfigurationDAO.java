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
    @SqlQuery("select service, key, value from configuration where service = :service")
    public abstract List<Configuration> fetchAllForService(
            @Bind("service") String service
    );

    @Mapper(ConfigurationMapper.class)
    @SqlQuery("select service, key, value from configuration where service = :service and key = :key")
    public abstract Configuration fetchConfigurationForService(
            @Bind("service") String service,
            @Bind("key") String key
    );

    public Event createOrUpdate(String service, String key, String value) {
        Configuration config = fetchConfigurationForService(service, key);
        if (config == null) {
            if (addConfiguration(service, key, value) == 1) {
                return Event.CREATED;
            } else {
                return Event.UNMODIFIED;
            }
        } else if (config.getValue().equals(value)) {
            return Event.UNMODIFIED;
        } else {
            if (updateValue(service, key, value) == 1) {
                return Event.UPDATED;
            } else {
                return Event.UNMODIFIED;
            }
        }
    }

    @SqlUpdate("insert into configuration (service,key,value) values (:service,:key,:value)")
    protected abstract int addConfiguration(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("value")String value
    );

    @SqlUpdate("update configuration set value = :value where service = :service and key = :key")
    protected abstract int updateValue(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("value")String value
    );
}
