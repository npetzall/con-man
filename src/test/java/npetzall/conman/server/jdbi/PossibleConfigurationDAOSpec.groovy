package npetzall.conman.server.jdbi

import npetzall.conman.server.api.PossibleConfiguration
import org.assertj.core.groups.FieldsOrPropertiesExtractor
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.extractor.Extractors.byName

class PossibleConfigurationDAOSpec extends Specification {

    @ClassRule
    @Shared
    H2MemoryRule h2MemoryRule = new H2MemoryRule("PossibleConfigurationDAOSpec");

    def "Add/Retrieved possible configuration"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testService", "testKey", "This is a possible configuration description", "true,false", "csv")

        when:
        PossibleConfigurationDAO.Event event = possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        and:
        PossibleConfiguration retrieved = possibleConfigurationDAO.fetchPossibleConfigurationForService(possibleConfiguration.service, possibleConfiguration.key)

        then:
        event == PossibleConfigurationDAO.Event.CREATED
        with(retrieved) {
            service == possibleConfiguration.service
            key == possibleConfiguration.key
            description == possibleConfiguration.description
            valueRestriction == possibleConfiguration.valueRestriction
            valueRestrictionType == possibleConfiguration.valueRestrictionType
        }
    }

    def "Save multiple and retrieve all"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration expected1 = new PossibleConfiguration("testServiceMany", "keyOne", "Description One", "one,two", "csv")
        PossibleConfiguration expected2 = new PossibleConfiguration(expected1.service, "keyTwo", "Description One", "one,two", "csv")

        when:
        PossibleConfigurationDAO.Event event1 = possibleConfigurationDAO.createOrUpdate(expected1.service, expected1.key, expected1.description, expected1.valueRestriction, expected1.valueRestrictionType)
        PossibleConfigurationDAO.Event event2 = possibleConfigurationDAO.createOrUpdate(expected2.service, expected2.key, expected2.description, expected2.valueRestriction, expected2.valueRestrictionType)

        and:
        List<PossibleConfiguration> possibleConfigurations = possibleConfigurationDAO.fetchAllForService(expected1.service)

        then:
        event1 == PossibleConfigurationDAO.Event.CREATED
        event2 == PossibleConfigurationDAO.Event.CREATED
        assertThat(possibleConfigurations)
                .extracting("service","key","description","valueRestriction","valueRestrictionType")
                .containsAll(FieldsOrPropertiesExtractor
                    .extract(
                        [expected1, expected2],
                        byName("service","key","description","valueRestriction","valueRestrictionType")))
    }

    def "Updating existing with same data will return unmodified"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceUpdateUnmodified", "testKey", "This is a possible configuration description", "true,false", "csv")

        when:
        PossibleConfigurationDAO.Event eventCreated = possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        and:
        PossibleConfigurationDAO.Event eventUnmodified = possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        then:
        eventCreated == PossibleConfigurationDAO.Event.CREATED
        eventUnmodified == PossibleConfigurationDAO.Event.UNMODIFIED
    }

    def "Updating with new data will return updated"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceUpdateUpdated", "testKey", "This is a possible configuration description", "true,false", "csv")

        when:
        PossibleConfigurationDAO.Event eventCreated = possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        and:
        PossibleConfigurationDAO.Event eventUnmodified = possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, "This is the possible configuration description" , possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        then:
        eventCreated == PossibleConfigurationDAO.Event.CREATED
        eventUnmodified == PossibleConfigurationDAO.Event.UPDATED
    }

    def "Update description only"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceSingleValueUpdate", "testKeyDescription", "DescriptionOld", "true,false", "csv")
        possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)
        String newDescription = "DescriptionNew"

        when:
        int updateCount = possibleConfigurationDAO.updateDescription(possibleConfiguration.service, possibleConfiguration.key, newDescription)
        and:
        PossibleConfiguration possibleConfigurationRetrieved = possibleConfigurationDAO.fetchPossibleConfigurationForService(possibleConfiguration.service, possibleConfiguration.key)

        then:
        updateCount == 1
        with(possibleConfigurationRetrieved) {
            service == possibleConfiguration.service
            key == possibleConfiguration.key
            description == newDescription
            valueRestriction == possibleConfiguration.valueRestriction
            valueRestrictionType == possibleConfiguration.valueRestrictionType
        }
    }

    def "Update valueRestriction only"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceSingleValueUpdate", "testKeyValueRestriction", "DescriptionOld", "true,false", "csv")
        possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)
        String newValueRestriction = "y,n"

        when:
        int updateCount = possibleConfigurationDAO.updateValueRestriction(possibleConfiguration.service, possibleConfiguration.key, newValueRestriction)
        and:
        PossibleConfiguration possibleConfigurationRetrieved = possibleConfigurationDAO.fetchPossibleConfigurationForService(possibleConfiguration.service, possibleConfiguration.key)

        then:
        updateCount == 1
        with(possibleConfigurationRetrieved) {
            service == possibleConfiguration.service
            key == possibleConfiguration.key
            description == possibleConfiguration.description
            valueRestriction == newValueRestriction
            valueRestrictionType == possibleConfiguration.valueRestrictionType
        }
    }

    def "Update valueRestrictionType only"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceSingleValueUpdate", "testKeyValueRestrictionType", "DescriptionOld", "true,false", "csv")
        possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)
        String newValueRestrictionType = "boolean"

        when:
        int updateCount = possibleConfigurationDAO.updateValueRestrictionType(possibleConfiguration.service, possibleConfiguration.key, newValueRestrictionType)
        and:
        PossibleConfiguration possibleConfigurationRetrieved = possibleConfigurationDAO.fetchPossibleConfigurationForService(possibleConfiguration.service, possibleConfiguration.key)

        then:
        updateCount == 1
        with(possibleConfigurationRetrieved) {
            service == possibleConfiguration.service
            key == possibleConfiguration.key
            description == possibleConfiguration.description
            valueRestriction == possibleConfiguration.valueRestriction
            valueRestrictionType == newValueRestrictionType
        }
    }

    def "Exists"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("testServiceSingleValueUpdate", "testKeyValueRestrictionType", "DescriptionOld", "true,false", "csv")
        possibleConfigurationDAO.createOrUpdate(possibleConfiguration.service, possibleConfiguration.key, possibleConfiguration.description, possibleConfiguration.valueRestriction, possibleConfiguration.valueRestrictionType)

        when:
        boolean exists = possibleConfigurationDAO.exists(possibleConfiguration.service, possibleConfiguration.key)

        then:
        exists == true
    }

    def "Doesn't exist"() {
        setup:
        PossibleConfigurationDAO possibleConfigurationDAO = h2MemoryRule.dbi.onDemand(PossibleConfigurationDAO.class)

        when:
        boolean exists = possibleConfigurationDAO.exists("aService", "aKey")

        then:
        exists == false
    }
}
