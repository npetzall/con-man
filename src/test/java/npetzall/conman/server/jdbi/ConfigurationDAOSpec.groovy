package npetzall.conman.server.jdbi

import npetzall.conman.server.api.Configuration
import org.assertj.core.groups.FieldsOrPropertiesExtractor
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.extractor.Extractors.byName

class ConfigurationDAOSpec extends Specification {

    @ClassRule
    @Shared
    H2MemoryRule h2MemoryRule = new H2MemoryRule("ConfigurationDAOSpec");


    def "Can save and retrieve configuration"(){
        given:
        ConfigurationDAO configurationDAO = h2MemoryRule.getDbi().onDemand(ConfigurationDAO.class)
        Configuration expected = new Configuration("testDAOServiceSaveOne","testDAOKey", "testDAOValue")

        when:
        ConfigurationDAO.Event event = configurationDAO.createOrUpdate(expected.service,expected.key,expected.value)

        and:
        Configuration configuration = configurationDAO.fetchConfigurationForService(expected.service, expected.key)

        then:
        event == ConfigurationDAO.Event.CREATED
        with(configuration) {
            service == expected.service
            key == expected.key
            value == expected.value
        }
    }

    def "Save multiple and retrieve all configurations"() {
        given:
        ConfigurationDAO configurationDAO = h2MemoryRule.getDbi().onDemand(ConfigurationDAO.class)
        Configuration expected1 = new Configuration("testDAOServiceSaveMany", "testKeyOne", "testDAOValueOne")
        Configuration expected2 = new Configuration("testDAOServiceSaveMany", "testKeyTwo", "testDAOValueTwo")

        when:
        ConfigurationDAO.Event event1 = configurationDAO.createOrUpdate(expected1.service, expected1.key, expected1.value)
        ConfigurationDAO.Event event2 = configurationDAO.createOrUpdate(expected2.service, expected2.key, expected2.value)

        and:
        List<Configuration> configurations = configurationDAO.fetchAllForService(expected1.service)

        then:
        event1 == ConfigurationDAO.Event.CREATED
        event2 == ConfigurationDAO.Event.CREATED
        assertThat(configurations).extracting("service","key","value").containsAll(FieldsOrPropertiesExtractor.extract([expected1,expected2], byName("service","key","value")))
    }

    def "Updating with same value will result in unmodified"() {
        given:
        ConfigurationDAO configurationDAO = h2MemoryRule.getDbi().onDemand(ConfigurationDAO.class)
        Configuration configuration = new Configuration("testServiceDAOUpdateUnmodified","testDAOKey", "testDAOValue")

        when:
        ConfigurationDAO.Event eventCreate = configurationDAO.createOrUpdate(configuration.service,configuration.key,configuration.value)
        ConfigurationDAO.Event eventUnmodified = configurationDAO.createOrUpdate(configuration.service,configuration.key,configuration.value)

        then:
        eventCreate == ConfigurationDAO.Event.CREATED
        eventUnmodified == ConfigurationDAO.Event.UNMODIFIED
    }

    def "Updating with new value will result in updated"() {
        given:
        ConfigurationDAO configurationDAO = h2MemoryRule.getDbi().onDemand(ConfigurationDAO.class)
        Configuration configuration = new Configuration("testDAOServiceUpdateUpdated","testDAOKey", "testDAOValue")

        when:
        ConfigurationDAO.Event eventCreate = configurationDAO.createOrUpdate(configuration.service,configuration.key,configuration.value)
        ConfigurationDAO.Event eventUpdated = configurationDAO.createOrUpdate(configuration.service,configuration.key, "newValue")

        then:
        eventCreate == ConfigurationDAO.Event.CREATED
        eventUpdated == ConfigurationDAO.Event.UPDATED
    }

}