package at.enfilo.def.persistence.util;


import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.config.util.ConfigReader;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mase on 11.08.2016.
 */
public class ConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionProvider.class);
    private static final String CONFIG_FILE = "database.yml";
    private static final String PROPERTY_DIALECT = "hibernate.dialect";
	private static final String PROPERTY_DRIVER = "hibernate.connection.driver_class";
	private static final String PROPERTY_URL = "hibernate.connection.url";
	private static final String PROPERTY_USER = "hibernate.connection.username";
	private static final String PROPERTY_PASSWORD = "hibernate.connection.password";

    private final SessionFactory sessionFactory;

    private static class ThreadSafeLazySingletonWrapper {
        private static final ConnectionProvider INSTANCE = new ConnectionProvider();
    }

    private ConnectionProvider()
    throws ExceptionInInitializerError {
		// Should be implemented as thread safe lazy singleton.
        try {
			// Read configuration (database settings)
			DatabaseConfiguration databaseConfig = ConfigReader.readConfiguration(
					CONFIG_FILE, ConnectionProvider.class, DatabaseConfiguration.class
			);

			// Read configuration from hibernate.cfg.xml
			Configuration hibernateConfig = new Configuration().configure();
			// Merge properties from databaseConfig
			hibernateConfig.setProperty(PROPERTY_DIALECT, databaseConfig.getDialect());
			hibernateConfig.setProperty(PROPERTY_DRIVER, databaseConfig.getDriver());
			hibernateConfig.setProperty(PROPERTY_URL, databaseConfig.getUrl());
			hibernateConfig.setProperty(PROPERTY_USER, databaseConfig.getUser());
			hibernateConfig.setProperty(PROPERTY_PASSWORD, databaseConfig.getPassword());

            sessionFactory = hibernateConfig.buildSessionFactory();

        } catch (Exception e) {
            LOGGER.error("Something went wrong by initializing hibernate. Check config files for errors and DB availability.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private static ConnectionProvider getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    private Session getNewSession()
    throws HibernateException {
        return sessionFactory.openSession();
    }

    /**
     * Performs objective transaction with a query that potentially may produce result data.
     *
     * @param transactionFunction proxy function that executes prepared query on a supplied session.
     * @param <T> type of data that may be produced as a result.
     * @return potential result data of type {@code T}.
     * @throws PersistenceException
     */
    public static <T> T makeSimpleTransaction(IThrowingFunction<Session, T> transactionFunction)
    throws PersistenceException {

        try (Session session = getDetachedSession()) {

            Transaction transaction = null;

            try {

                transaction = session.beginTransaction();
                T result = transactionFunction.apply(session);
                transaction.commit();

                return result;

            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }

                throw e;
            }

        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Performs objective transaction with a query that potentially do not produce result data.
     *
     * @param transactionFunction proxy function that executes prepared query on a supplied session.
     * @throws PersistenceException
     */
    public static void makeSimpleTransaction(IThrowingConsumer<Session> transactionFunction)
    throws PersistenceException {

        try (Session session = getDetachedSession()){

            Transaction transaction = null;

            try {

                transaction = session.beginTransaction();
                transactionFunction.accept(session);
                transaction.commit();

            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }

                throw e;
            }

        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Returns detached session object that will be not maintained by this class,
     * all contracts should be fulfilled by developer himself..
     *
     * @return new detached session.
     * @throws PersistenceException
     */
    public static Session getDetachedSession()
    throws PersistenceException {
        try {
            return getInstance().getNewSession();
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
}
