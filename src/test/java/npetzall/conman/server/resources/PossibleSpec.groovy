package npetzall.conman.server.resources

import io.dropwizard.testing.junit.ResourceTestRule
import npetzall.conman.server.api.PossibleConfiguration
import npetzall.conman.server.api.PossibleConfigurationData
import npetzall.conman.server.jdbi.PossibleConfigurationDAO
import org.assertj.core.groups.FieldsOrPropertiesExtractor
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.extractor.Extractors.byName

class PossibleSpec extends Specification {

    @Shared
    PossibleConfiguration possibleConfiguration1 = new PossibleConfiguration("service", "key1", "Description 1", "one,two,three", "csv")

    @Shared
    PossibleConfiguration possibleConfiguration2 = new PossibleConfiguration("service", "key2", "Description 2", "four,five,six", "csv")

    @Shared
    PossibleConfigurationDAO possibleConfigurationDAO = Stub(PossibleConfigurationDAO.class) {
        fetchPossibleConfigurationForService(_,_) >> possibleConfiguration1
        fetchAllForService(_) >> [possibleConfiguration1, possibleConfiguration2]
        createOrUpdate("newService","newKey",_,_,_) >> PossibleConfigurationDAO.Event.CREATED
        createOrUpdate("newService","unmodifiedKey",_,_,_) >> PossibleConfigurationDAO.Event.UNMODIFIED
        createOrUpdate("newService","updatedKey",_,_,_) >> PossibleConfigurationDAO.Event.UPDATED
        exists(_,"none") >> false
        exists(_,_) >> true
        updateDescription(_,"new",_) >> Response.Status.OK.statusCode
        updateDescription(_,"same",_) >> Response.Status.OK.statusCode
        updateDescription(_,"none",_) >> Response.Status.NOT_FOUND.statusCode
        updateValueRestriction(_,"new",_) >> Response.Status.OK.statusCode
        updateValueRestriction(_,"same",_) >> Response.Status.OK.statusCode
        updateValueRestriction(_,"none",_) >> Response.Status.NOT_FOUND.statusCode
    }

    @ClassRule
    @Shared
    ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new Possible(possibleConfigurationDAO))
            .build();

    def "Retrieve_possible_configuration"() {
        when:
        PossibleConfiguration retrievedPossibleConfiguration = resources.client().target("/possible/service/key1")
                .request().get(PossibleConfiguration.class)

        then:
        with(retrievedPossibleConfiguration) {
            service == possibleConfiguration1.service
            key == possibleConfiguration1.key
            description == possibleConfiguration1.description
            valueRestriction == possibleConfiguration1.valueRestriction
            valueRestrictionType == possibleConfiguration1.valueRestrictionType
        }
    }

    def "Retrieve all possible configurations for service"(){
        when:
        List<PossibleConfiguration> retrievedPossibleConfigurations = resources.client().target("/possible/service")
                .request().get(new GenericType<List<PossibleConfiguration>>(){})

        then:
        assertThat(retrievedPossibleConfigurations)
            .extracting("service","key","description","valueRestriction", "valueRestrictionType")
            .containsAll(FieldsOrPropertiesExtractor
                .extract([possibleConfiguration1,possibleConfiguration2],
                    byName("service","key","description","valueRestriction", "valueRestrictionType")
                )
            )
    }

    def "Add possible configuration, created"() {
        setup:
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("newService", "newKey", "this is a new description", "true,false", "csv")

        when:
        Response responeAdd = resources.client().target("/possible/" + possibleConfiguration.service + "/" + possibleConfiguration.key)
            .request().put(Entity.json(new PossibleConfigurationData(possibleConfiguration)))

        then:
        responeAdd.status == Response.Status.CREATED.statusCode
    }

    def "Update possible configuration, unmodified"() {
        setup:
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("newService", "unmodifiedKey", "this is the new description", "[yYnNmM]{1,1}", "regex")

        when:
        Response responeAdd = resources.client().target("/possible/" + possibleConfiguration.service + "/" + possibleConfiguration.key)
                .request().put(Entity.json(new PossibleConfigurationData(possibleConfiguration)))

        then:
        responeAdd.status == Response.Status.OK.statusCode
    }

    def "Update possible configuration, update"() {
        setup:
        PossibleConfiguration possibleConfiguration = new PossibleConfiguration("newService", "updatedKey", "this is the new description", "[yYnNmM]{1,1}", "regex")

        when:
        Response responeAdd = resources.client().target("/possible/" + possibleConfiguration.service + "/" + possibleConfiguration.key)
                .request().put(Entity.json(new PossibleConfigurationData(possibleConfiguration)))

        then:
        responeAdd.status == Response.Status.OK.statusCode
    }

    def "Update possible configuration #field, returns #status when value is #key"() {
        setup:
        String service = "serviceIndividual"
        String value = "something"

        when:
        Response response = resources.client().target("/possible/"+service + "/" + key + "/" + field)
                .request().put(Entity.text(value))

        then:
        response.status == status

        where:
        key         |   status  | field
        "new"       |      200  | "description"
        "same"      |      200  | "description"
        "none"      |      404  | "description"
        "new"       |      200  | "valueRestriction"
        "same"      |      200  | "valueRestriction"
        "none"      |      404  | "valueRestriction"
        "new"       |      200  | "valueRestrictionType"
        "same"      |      200  | "valueRestrictionType"
        "none"      |      404  | "valueRestrictionType"

    }

}
