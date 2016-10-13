package npetzall.conman.server.jdbi.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExistsMapper implements ResultSetMapper<Boolean> {
    @Override
    public Boolean map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return r.isFirst();
    }
}
