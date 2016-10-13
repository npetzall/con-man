package npetzall.conman.server.at

import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.testing.ConfigOverride
import io.dropwizard.testing.junit.DropwizardAppRule
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import npetzall.conman.server.ConManApplication
import npetzall.conman.server.ConManConfiguration
import npetzall.conman.server.api.Configuration
import npetzall.conman.server.jdbi.ConfigurationDAO
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

class ConManConfigSpec extends Specification{

    @Shared
    Configuration configurationOne = new Configuration("testService","testKey","testValue")
    @Shared
    Configuration configurationTwo = new Configuration(configurationOne.service,"testKeyUpdate","testValueOld")

    @ClassRule
    @Shared
    DropwizardAppRule<ConManConfiguration> RULE =
            new DropwizardAppRule<ConManConfiguration>(ConManApplication.class, "./src/dist/conman-config.yaml",
                    ConfigOverride.config("database.url","jdbc:h2:mem:ConManConfigSpec"),
                    ConfigOverride.config("server.applicationConnectors[0].port", "0"),
                    ConfigOverride.config("server.adminConnectors[0].port", "0"))

    def setupSpec() {
        DBI dbi = new DBIFactory().build(RULE.getEnvironment(),RULE.getConfiguration().getDataSourceFactory(),"ConManConfigSpec")
        Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(dbi.open().getConnection()));
        liquibase.update("");
        ConfigurationDAO configurationDAO = dbi.onDemand(ConfigurationDAO.class);
        configurationDAO.createOrUpdate(configurationOne.service, configurationOne.key, configurationOne.value)
        configurationDAO.createOrUpdate(configurationTwo.service, configurationTwo.key, configurationTwo.value)
    }

    def "Get configuration"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        Configuration response = client.target(
                String.format("http://localhost:%d/config/testService/testKey", RULE.getLocalPort()))
                .request().get(Configuration.class);

        then:
        with(response) {
            service == configurationOne.service
            key == configurationOne.key
            value == configurationOne.value
        }
    }

    def "Get all configurations for service"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        List<Configuration> responses = client.target(
                String.format("http://localhost:%d/config/testService", RULE.getLocalPort()))
                .request().get(new GenericType<List<Configuration>>(){});

        then:
        assertThat(responses).extracting("service","key","value")
                .containsAll(FieldsOrPropertiesExtractor.extract(
                [configurationOne,configurationTwo], byName("service","key","value")))
    }

    def "Get configuration value"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        String response = client.target(String.format("http://localhost:%d/config/"+configurationOne.service+"/"+configurationOne.key+"/value", RULE.getLocalPort()))
                .request().get(String.class)

        then:
        response == configurationOne.value
    }

    def "Create configuration"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        Response response = client.target(String.format("http://localhost:%d/config/testServiceNew/testKeyNew/value", RULE.getLocalPort()))
                .request().put(Entity.text("valueNew"))
        and:
        String value = client.target(String.format("http://localhost:%d/config/testServiceNew/testKeyNew/value", RULE.getLocalPort()))
                .request().get(String.class)

        then:
        response.status == Response.Status.CREATED.statusCode
        value == "valueNew"
    }

    def "Try to create existing configuration"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        Response response = client.target(String.format("http://localhost:%d/config/testService/testKey/value", RULE.getLocalPort()))
                .request().put(Entity.text("testValue"))

        then:
        response.status == Response.Status.OK.statusCode
    }

    def "Update configuration"() {
        setup:
        Client client = ClientBuilder.newClient()

        when:
        Response responseFromUpdate = client.target(String.format("http://localhost:%d/config/testService/testKeyUpdate/value", RULE.getLocalPort()))
                .request().put(Entity.text("testValueNew"))
        and:
        String newValue = client.target(String.format("http://localhost:%d/config/testService/testKeyUpdate/value", RULE.getLocalPort()))
                .request().get(String.class)

        then:
        responseFromUpdate.status == Response.Status.OK.statusCode
        newValue == "testValueNew"
    }

}
