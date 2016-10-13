package npetzall.conman.server.resources

import io.dropwizard.testing.junit.ResourceTestRule
import npetzall.conman.server.api.Configuration
import npetzall.conman.server.jdbi.ConfigurationDAO
import org.assertj.core.groups.FieldsOrPropertiesExtractor
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.extractor.Extractors.byName

class ConfigSpec extends Specification {

    @Shared
    Configuration configuration = new Configuration("testService","testKey", "testValue")
    @Shared
    Configuration configurationTwo = new Configuration("testService","testKey2", "testValue2")

    @Shared
    ConfigurationDAO configurationDAO = Stub(ConfigurationDAO) {
        fetchAllForService(_) >> [configuration,configurationTwo]
        fetchConfigurationForService(_,_) >> configuration
        createOrUpdate("testService","testKey","testValueCreated") >> ConfigurationDAO.Event.CREATED
        createOrUpdate("testService","testKey","testValueUnmodified") >> ConfigurationDAO.Event.UNMODIFIED
        createOrUpdate("testService","testKey","testValueUpdated") >> ConfigurationDAO.Event.UPDATED
    }

    @ClassRule
    @Shared
    ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new Config(configurationDAO))
            .build();

    def "Can retrieve a configuration"() {
        when:
        Configuration retrievedConfiguration = resources.client().target("/config/testService/testKey").request().get(Configuration.class)
        then:
        with(retrievedConfiguration) {
            service == configuration.service
            key == configuration.key
            value == configuration.value
        }
    }

    def "Can retrieve all configurations"() {
        given:
        List<Configuration> configurations = [configuration,configurationTwo]
        when:
        List<Configuration> retrievedConfigurations = resources.client().target("/config/testService")
                .request().get(new GenericType<List<Configuration>>(){})
        then:
        assertThat(retrievedConfigurations).extracting("service","key","value").containsAll(FieldsOrPropertiesExtractor.extract(configurations, byName("service","key","value")))
    }

    def "Can retrieve configuration value"() {
        when:
        String configurationValue = resources.client().target("/config/testService/testKey/value")
                .request().get(String.class)
        then:
        configurationValue == "testValue"
    }

    def "Add config"() {
        when:
        Response response = resources.client().target("/config/testServiceNew/testKeyNew/value")
                .request().put(Entity.text("testValueCreated"))
        then:
        response.status == Response.Status.CREATED.statusCode
    }

    def "Update config, unmodified"() {
        when:
        Response response = resources.client().target("/config/testService/testKey/value")
                .request().put(Entity.text("testValueUnmodified"))
        then:
        response.status == Response.Status.OK.statusCode
    }

    def "Update config, updated"() {
        when:
        Response response = resources.client().target("/config/testService/testKey/value")
                .request().put(Entity.text("testValueUpdated"))
        then:
        response.status == Response.Status.OK.statusCode
    }

}
