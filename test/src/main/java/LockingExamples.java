import ua.procamp.util.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LockingExamples {
    private static DataSource dataSource;

    private static final String SQL_SELECT = "select * from programs where id = ?";
    private static final String SQL_SELECT_FOR_UPDATE = "select * from programs where id = ? for update";
    private static final String SQL_UPDATE = "UPDATE programs set name = ?, version = ? where id = ?";
    private static final String SQL_UPDATE_WITH_VERSION = "UPDATE programs set name = ?, version = ? where id = ? AND version = ?";

    public static void main(String... a) throws SQLException, InterruptedException {
        dataSource = JdbcUtil.createPostgresDataSource("jdbc:postgresql://localhost:5432/postgres",
                "postgres", "qwertyuiop");
        long id = 2L;

        Runnable opt = new Runnable() {
            @Override
            public void run() {
                try {
                    LockingExamples.optimistic(2L);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable nonOpt = new Runnable() {
            @Override
            public void run() {
                try {
                    LockingExamples.nonOptimistic(2L);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            new Thread(opt).start();
        }

        Thread.sleep(1000);
        System.out.println("______________________________");

        for (int i = 0; i < 10; i++) {
            new Thread(nonOpt).start(); // For right work nonOptimistic method must be synchronized
        }

    }

    public static void optimistic(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            var conLifeTime = new ConnectionLifeTime(2 * 60 * 1000);
            conLifeTime.start();

            while (conLifeTime.isWorking()) {
                int version = getVersion(connection, id);
                selectForConsole(connection, id, false);
                int rows = updateTableAndIncrVer(connection, id, version, true);
                if(rows != 0){
                    connection.commit();
                    conLifeTime.stop();
                } else {
                    connection.rollback();
                }
            }
        }
    }

    private static void selectForConsole(Connection connection, long id, boolean lock) throws SQLException {
        PreparedStatement preparedStatementSelect;
        if (lock) {
            preparedStatementSelect = connection.prepareStatement(SQL_SELECT_FOR_UPDATE);
        } else {
            preparedStatementSelect = connection.prepareStatement(SQL_SELECT);
        }
        preparedStatementSelect.setLong(1, id);
        ResultSet resultSet = preparedStatementSelect.executeQuery();
        printInConsole(resultSet);
    }

    private static int updateTableAndIncrVer(Connection connection, long id, int version, boolean checkVersion) throws SQLException {
        PreparedStatement preparedStatementUpdate = connection.prepareStatement(
                (checkVersion)?SQL_UPDATE_WITH_VERSION:SQL_UPDATE
        );
        preparedStatementUpdate.setString(1, "Name v." + (version));
        preparedStatementUpdate.setInt(2, version + 1);
        preparedStatementUpdate.setLong(3, id);
        if(checkVersion){
            preparedStatementUpdate.setLong(4, version);
        }
        return preparedStatementUpdate.executeUpdate();
    }

    private static int getVersion(Connection connection, long id) throws SQLException {
        PreparedStatement preparedStatementSelect = connection.prepareStatement(SQL_SELECT);
        preparedStatementSelect.setLong(1, id);
        ResultSet resultSet = preparedStatementSelect.executeQuery();
        resultSet.next();
        return resultSet.getInt(4);
    }

    private static synchronized void nonOptimistic(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            int version = getVersion(connection, id);
            selectForConsole(connection, id, true);
            updateTableAndIncrVer(connection, id, version, false);
            connection.commit();
        }
    }

    private static synchronized void printInConsole(ResultSet set) throws SQLException {
        String form = "%3s | %10s | %11s | %7s";
        System.out.println(String.format(form, "ID", "Name", "Description", "Version"));
        while (set.next()) {

            long id = set.getInt(1);
            String name = set.getString(2);
            String description = set.getString(3);
            int ver = set.getInt(4);

            System.out.println(String.format(form, Long.toString(id), name, description, ver));
        }
    }

    private static class ConnectionLifeTime {
        private long startTime;
        private long duration;
        private boolean isWorking;

        ConnectionLifeTime(long millis) {
            duration = millis;
        }

        void start() {
            startTime = System.currentTimeMillis();
            isWorking = true;
        }

        boolean isWorking() {
            if (!isWorking) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > duration) {
                isWorking = false;
                return false;
            } else {
                return true;
            }
        }

        void stop() {
            isWorking = false;
        }
    }
}
