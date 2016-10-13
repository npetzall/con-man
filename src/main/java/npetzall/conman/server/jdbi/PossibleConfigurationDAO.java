package npetzall.conman.server.jdbi;

import npetzall.conman.server.api.PossibleConfiguration;
import npetzall.conman.server.jdbi.mapper.ExistsMapper;
import npetzall.conman.server.jdbi.mapper.PossibleConfigurationMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public abstract class PossibleConfigurationDAO {

    public enum Event {
        CREATED, UPDATED, UNMODIFIED
    }

    @Mapper(PossibleConfigurationMapper.class)
    @SqlQuery("select service, key, description, valueRestriction, valueRestrictionType from possibleconfiguration where service = :service")
    public abstract List<PossibleConfiguration> fetchAllForService(
            @Bind("service") String service
    );

    @Mapper(PossibleConfigurationMapper.class)
    @SqlQuery("select service, key, description, valueRestriction, valueRestrictionType from possibleconfiguration where service = :service and key = :key")
    public abstract PossibleConfiguration fetchPossibleConfigurationForService(
            @Bind("service") String service,
            @Bind("key") String key);

    public Event createOrUpdate(String service, String key, String description, String valueRestriction, String valueRestrictionType) {
        PossibleConfiguration possibleConfiguration = fetchPossibleConfigurationForService(service, key);
        if (possibleConfiguration == null) {
            if (addPossibleConfiguration(service, key, description, valueRestriction, valueRestrictionType) == 1) {
                return Event.CREATED;
            }
        } else if (dataIsEqual(possibleConfiguration, description, valueRestriction, valueRestrictionType)) {
            return Event.UNMODIFIED;
        } else {
            if (updatePossibleConfiguration(service, key, description, valueRestriction, valueRestrictionType) == 1) {
                return Event.UPDATED;
            }
        }
        return Event.UNMODIFIED;
    }

    private static boolean dataIsEqual(PossibleConfiguration possibleConfiguration, String description, String valueRestriction, String valueRestrictionType) {
        return possibleConfiguration.getDescription().equals(description) && possibleConfiguration.getValueRestriction().equals(valueRestriction) && possibleConfiguration.getValueRestrictionType().equals(valueRestrictionType);
    }

    @SqlUpdate("insert into possibleconfiguration (service, key, description, valueRestriction, valueRestrictionType) values (:service, :key, :description, :valueRestriction, :valueRestrictionType)")
    protected abstract int addPossibleConfiguration(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("description") String description,
            @Bind("valueRestriction") String valueRestriction,
            @Bind("valueRestrictionType") String valueRestrictionType);

    @SqlUpdate("update possibleconfiguration set description = :description, valueRestriction = :valueRestriction, valueRestrictionType = :valueRestrictionType where service = :service and key = :key")
    public abstract int updatePossibleConfiguration(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("description") String description,
            @Bind("valueRestriction") String valueRestriction,
            @Bind("valueRestrictionType") String valueRestrictionType);

    @Mapper(ExistsMapper.class)
    @SqlQuery("select 1 from possibleconfiguration where service = :service and key = :key")
    public abstract boolean exists(
            @Bind("service") String service,
            @Bind("key") String key);

    @SqlUpdate("update possibleconfiguration set description = :description where service = :service and key = :key")
    public abstract int updateDescription(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("description") String description);

    @SqlUpdate("update possibleconfiguration set valueRestriction = :valueRestriction where service = :service and key = :key")
    public abstract int updateValueRestriction(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("valueRestriction") String valueRestriction);

    @SqlUpdate("update possibleconfiguration set valueRestrictionType = :valueRestrictionType where service = :service and key = :key")
    public abstract int updateValueRestrictionType(
            @Bind("service") String service,
            @Bind("key") String key,
            @Bind("valueRestrictionType") String valueRestrictionType);
}
