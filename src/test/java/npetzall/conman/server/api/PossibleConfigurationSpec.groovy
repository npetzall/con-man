package npetzall.conman.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.jackson.Jackson
import spock.lang.Shared
import spock.lang.Specification

import static io.dropwizard.testing.FixtureHelpers.fixture
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson

class PossibleConfigurationSpec extends Specification {

    @Shared ObjectMapper MAPPER = Jackson.newObjectMapper();
    @Shared PossibleConfiguration possibleConfiguration = new PossibleConfiguration("possibleService", "possibleKey", "possibleDescription", "one,two,three", "csv")

    def "Can serialize possible configuration"() {
        given: "new simple possible configuration"

        when: "serialized to string"
        String configurationAsString = MAPPER.writeValueAsString(possibleConfiguration);

        then: "string equals fixture"
        assertThatJson(configurationAsString).isEqualTo(fixture("fixtures/possibleconfiguration.json"))
    }

    def "Can deserialize possible configuration"() {
        given: "new simple possible configuration"

        when: "deserialized fixture"
        PossibleConfiguration deserializedConfiguration = MAPPER.readValue(fixture("fixtures/possibleconfiguration.json"), PossibleConfiguration.class)

        then:
        with(deserializedConfiguration){
            service == possibleConfiguration.service
            key == possibleConfiguration.key
            description == possibleConfiguration.description
            valueRestriction == possibleConfiguration.valueRestriction
            valueRestrictionType == possibleConfiguration.valueRestrictionType
        }
    }
}
