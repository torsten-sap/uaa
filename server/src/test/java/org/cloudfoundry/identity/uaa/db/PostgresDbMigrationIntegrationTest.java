package org.cloudfoundry.identity.uaa.db;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.lang.String.format;
import static java.lang.System.getProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/data-source.xml", "classpath*:/spring/env.xml"})
public class PostgresDbMigrationIntegrationTest {
    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String checkPrimaryKeyExists = "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_CATALOG = ? AND TABLE_NAME = LOWER(?) AND CONSTRAINT_NAME LIKE LOWER(?)";
    private String getAllTableNames = "SELECT distinct TABLE_NAME from information_schema.KEY_COLUMN_USAGE where TABLE_CATALOG = ? and TABLE_NAME != 'schema_version'";
    private String insertNewOauthCodeRecord = "insert into oauth_code(code) values('code');";

    @Before
    public void setup() {
        assumeTrue("Expected db profile to be enabled", getProperties().getProperty("spring.profiles.active").contains("postgresql"));

        flyway.clean();
    }

    @After
    public void cleanup() {
        flyway.clean();
    }

    @Test
    public void everyTableShouldHaveAPrimaryKeyColumn() throws Exception {
        flyway.migrate();

        List<String> tableNames = jdbcTemplate.queryForList(getAllTableNames, String.class, jdbcTemplate.getDataSource().getConnection().getCatalog());
        assertThat(tableNames, hasSize(greaterThan(0)));
        for (String tableName : tableNames) {
            int count = jdbcTemplate.queryForObject(checkPrimaryKeyExists, Integer.class, jdbcTemplate.getDataSource().getConnection().getCatalog(), tableName, "%" + tableName + "_pk%");
            assertThat(format("%s is missing primary key", tableName), count, greaterThanOrEqualTo(1));
        }

        try {
            jdbcTemplate.execute(insertNewOauthCodeRecord);
        } catch (Exception _) {
            fail("oauth_code table should auto increment primary key when inserting data.");
        }
    }
}