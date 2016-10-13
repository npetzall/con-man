package npetzall.conman.server.at

import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.testing.ConfigOverride
import io.dropwizard.testing.junit.DropwizardAppRule
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import npetzall.conman.server.ConManApplication
import npetzall.conman.server.ConManConfiguration
import npetzall.conman.server.api.PossibleConfiguration
import npetzall.conman.server.api.PossibleConfigurationData
import npetzall.conman.server.jdbi.PossibleConfigurationDAO
import org.assertj.core.groups.FieldsOrPropertiesExtractor
import org.junit.ClassRule
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.extractor.Extractors.byName

class ConManPossibleSpec extends Specification{

    @Shared
    PossibleConfiguration possibleConfigurationOne = new PossibleConfiguration("testService", "one", "first description", "one,two,three", "csv")
    @Shared
    PossibleConfiguration possibleConfigurationTwo = new PossibleConfiguration(possibleConfigurationOne.service, "two", "second description", "four,five,six", "csv")
    @Shared
    PossibleConfiguration possibleConfigurationUpdates = new PossibleConfiguration("testServiceUpdates", "updateKey", "oldDescription", "old, older, oldest", "oldType")

    @ClassRule
    @Shared
    DropwizardAppRule<ConManConfiguration> RULE =
            new DropwizardAppRule<ConManConfiguration>(ConManApplication.class, "./src/dist/conman-config.yaml",
                    ConfigOverride.config("database.url","jdbc:h2:mem:ConManPossibleSpec"),
                    ConfigOverride.config("server.applicationConnectors[0].port", "0"),
                    ConfigOverride.config("server.adminConnectors[0].port", "0"))

    def setupSpec() {
        DBI dbi = new DBIFactory().build(RULE.getEnvironment(),RULE.getConfiguration().getDataSourceFactory(),"ConManPossibleSpec")
        Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(dbi.open().getConnection()));
        liquibase.update("");
        PossibleConfigurationDAO possibleConfigurationDAO = dbi.onDemand(PossibleConfigurationDAO.class);
        possibleConfigurationDAO.createOrUpdate(
                possibleConfigurationOne.service,
                possibleConfigurationOne.key,
                possibleConfigurationOne.description,
                possibleConfigurationOne.valueRestriction,
                possibleConfigurationOne.valueRestrictionType)
        possibleConfigurationDAO.createOrUpdate(
                possibleConfigurationTwo.service,
                possibleConfigurationTwo.key,
                possibleConfigurationTwo.description,
                possibleConfigurationTwo.valueRestriction,
                possibleConfigurationTwo.valueRestrictionType)
        possibleConfigurationDAO.createOrUpdate(
                possibleConfigurationUpdates.service,
                possibleConfigurationUpdates.key,
                possibleConfigurationUpdates.description,
                possibleConfigurationUpdates.valueRestriction,
                possibleConfigurationUpdates.valueRestrictionType)
    }

    def "Get single possible configuration service"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        PossibleConfiguration response = client.target(
                String.format("http://localhost:%d/possible/"+possibleConfigurationOne.service+"/"+possibleConfigurationOne.key, RULE.getLocalPort()))
                .request().get(PossibleConfiguration.class);

        then:
        with(response) {
            service == possibleConfigurationOne.service
            key == possibleConfigurationOne.key
            description == possibleConfigurationOne.description
            valueRestriction == possibleConfigurationOne.valueRestriction
            valueRestrictionType == possibleConfigurationOne.valueRestrictionType
        }
    }

    def "Get all possible configurations for service"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        List<PossibleConfiguration> responses = client.target(
                String.format("http://localhost:%d/possible/"+possibleConfigurationOne.service, RULE.getLocalPort()))
                .request().get(new GenericType<List<PossibleConfiguration>>(){});

        then:
        responses.size() == 2
        assertThat(responses)
                .extracting("service","key","description","valueRestriction", "valueRestrictionType")
                .containsAll(FieldsOrPropertiesExtractor
                .extract([possibleConfigurationOne,possibleConfigurationTwo],
                byName("service","key","description","valueRestriction", "valueRestrictionType")
                )
            )
    }

    def "Create a new possible configuration"() {
        setup:
        PossibleConfiguration newPossibleConfiguration = new PossibleConfiguration("testServiceNew", "newKey", "new description", "true,false", "csv")
        Client client = ClientBuilder.newClient()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/possible/%s/%s", RULE.getLocalPort(), newPossibleConfiguration.service, newPossibleConfiguration.key))
                .request().put(Entity.json(new PossibleConfigurationData(newPossibleConfiguration)))
        and:
        PossibleConfiguration retrieved = client.target(
                String.format("http://localhost:%d/possible/%s/%s", RULE.getLocalPort(), newPossibleConfiguration.service, newPossibleConfiguration.key))
                .request().get(PossibleConfiguration.class)

        then:
        response.status == Response.Status.CREATED.statusCode
        with(newPossibleConfiguration) {
            service == retrieved.service
            key == retrieved.key
            description == retrieved.description
            valueRestriction == retrieved.valueRestriction
            valueRestrictionType == retrieved.valueRestrictionType
        }
    }

    def "Update description"() {
        setup:
        Client client = ClientBuilder.newClient()
        String newDescription = "newDescription"

        when:
        Response response = client.target(
                String.format("http://localhost:%d/possible/%s/%s/description", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().put(Entity.text(newDescription))
        and:
        PossibleConfiguration retrieved = client.target(
                String.format("http://localhost:%d/possible/%s/%s", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().get(PossibleConfiguration.class)

        then:
        response.status == Response.Status.OK.statusCode
        with(retrieved) {
            service == possibleConfigurationUpdates.service
            key == possibleConfigurationUpdates.key
            description == newDescription
        }
    }
    def "Update valueRestriction"() {
        setup:
        Client client = ClientBuilder.newClient()
        String newValueRestriction = "newValueRestriction"

        when:
        Response response = client.target(
                String.format("http://localhost:%d/possible/%s/%s/valueRestriction", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().put(Entity.text(newValueRestriction))
        and:
        PossibleConfiguration retrieved = client.target(
                String.format("http://localhost:%d/possible/%s/%s", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().get(PossibleConfiguration.class)

        then:
        response.status == Response.Status.OK.statusCode
        with(retrieved) {
            service == possibleConfigurationUpdates.service
            key == possibleConfigurationUpdates.key
            valueRestriction == newValueRestriction
        }
    }

    def "Update valueRestricionType"() {
        setup:
        Client client = ClientBuilder.newClient()
        String newValueRestricionType = "newValueRestricionType"

        when:
        Response response = client.target(
                String.format("http://localhost:%d/possible/%s/%s/valueRestrictionType", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().put(Entity.text(newValueRestricionType))
        and:
        PossibleConfiguration retrieved = client.target(
                String.format("http://localhost:%d/possible/%s/%s", RULE.getLocalPort(), possibleConfigurationUpdates.service, possibleConfigurationUpdates.key))
                .request().get(PossibleConfiguration.class)

        then:
        response.status == Response.Status.OK.statusCode
        with(retrieved) {
            service == possibleConfigurationUpdates.service
            key == possibleConfigurationUpdates.key
            valueRestrictionType == newValueRestricionType
        }
    }

}
