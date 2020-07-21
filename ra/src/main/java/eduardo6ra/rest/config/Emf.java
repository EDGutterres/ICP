package eduardo6ra.rest.config;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Emf {

	private static Emf theInstance = null;
	private EntityManagerFactory entityManagerFactory = null;

	private Emf() {
		entityManagerFactory = Persistence.createEntityManagerFactory("eduardo6ra", System.getProperties());
	}

	public static synchronized Emf getInstance() {
		if (theInstance == null) {
			theInstance = new Emf();
		}
		return theInstance;
	}

	public EntityManagerFactory getFactory() {
		return entityManagerFactory;
	}
}

