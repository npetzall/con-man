package npetzall.conman.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.jackson.Jackson
import spock.lang.Shared
import spock.lang.Specification

import static io.dropwizard.testing.FixtureHelpers.fixture
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson

class ConfigurationSpec extends Specification {

    @Shared ObjectMapper MAPPER = Jackson.newObjectMapper();
    @Shared Configuration configuration = new Configuration("testService", "testKey", "testValue")

    def "Can serialize configuration"() {
        given: "new simple configuration"

        when: "serialized to string"
        String configurationAsString = MAPPER.writeValueAsString(configuration);

        then: "string equals fixture"
        assertThatJson(configurationAsString).isEqualTo(fixture("fixtures/configuration.json"))
    }

    def "Can deserialize configuration"() {
        given: "new simple configuration"

        when: "deserialized fixture"
        Configuration deserializedConfiguration = MAPPER.readValue(fixture("fixtures/configuration.json"), Configuration.class)

        then:
        with(deserializedConfiguration){
            service == configuration.service
            key == configuration.key
            env == configuration.env
            value == configuration.value
        }
    }
}
