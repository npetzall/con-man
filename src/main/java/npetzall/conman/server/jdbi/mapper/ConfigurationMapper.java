package npetzall.conman.server.jdbi.mapper;

import npetzall.conman.server.api.Configuration;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigurationMapper implements ResultSetMapper<Configuration> {
    @Override
    public Configuration map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Configuration(
                r.getString("service"),
                r.getString("key"),
                r.getString("value")
        );
    }
}
