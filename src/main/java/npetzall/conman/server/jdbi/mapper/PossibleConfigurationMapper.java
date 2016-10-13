package npetzall.conman.server.jdbi.mapper;

import npetzall.conman.server.api.PossibleConfiguration;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PossibleConfigurationMapper implements ResultSetMapper<PossibleConfiguration> {
    @Override
    public PossibleConfiguration map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new PossibleConfiguration(
            r.getString("service"),
            r.getString("key"),
            r.getString("description"),
            r.getString("valueRestriction"),
            r.getString("valueRestrictionType")
        );
    }
}
