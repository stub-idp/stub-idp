package stubidp.stubidp.repositories.jdbc.rowmappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import stubidp.stubidp.repositories.jdbc.User;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.apache.commons.text.StringEscapeUtils.unescapeJson;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User map(ResultSet rs, StatementContext ctx) throws SQLException {

        Integer id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String idpFriendlyId = rs.getString("identity_provider_friendly_id");
        String data = rs.getString("data");
        String jsonData = unescapeJson(data.substring(1, data.length() - 1));

        return new User(id, username, password, idpFriendlyId, jsonData);
    }
}
