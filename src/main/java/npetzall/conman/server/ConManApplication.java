package npetzall.conman.server;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import npetzall.conman.server.jdbi.ConfigurationDAO;
import npetzall.conman.server.jdbi.PossibleConfigurationDAO;
import npetzall.conman.server.resources.Config;
import npetzall.conman.server.resources.Possible;
import org.skife.jdbi.v2.DBI;

public class ConManApplication extends Application<ConManConfiguration> {

    @Override
    public String getName() {
        return "Con-Man";
    }

    @Override
    public void initialize(Bootstrap<ConManConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<ConManConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ConManConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(new DBIExceptionsBundle());
    }

    @Override
    public void run(ConManConfiguration configuration, Environment environment) throws Exception {
        DBI dbi = new DBIFactory().build(environment,configuration.getDataSourceFactory(), "h2");
        ConfigurationDAO configurationDAO = dbi.onDemand(ConfigurationDAO.class);
        PossibleConfigurationDAO possibleConfigurationDAO = dbi.onDemand(PossibleConfigurationDAO.class);
        environment.jersey().register(new Config(configurationDAO));
        environment.jersey().register(new Possible(possibleConfigurationDAO));
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equalsIgnoreCase("server")) {
            new ConManApplication().run("server", "../config/conman-config.yaml");
        } else {
            new ConManApplication().run(args);
        }
    }

}
