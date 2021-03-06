package eduardo5ca.rest.config;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Emf {

	private static Emf theInstance = null;
	private EntityManagerFactory entityManagerFactory = null;

	private Emf() {
		entityManagerFactory = Persistence.createEntityManagerFactory("eduardo5ca", System.getProperties());
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

