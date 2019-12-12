package at.enfilo.def.persistence.util;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.persistence.dao.PersistenceFacade;

import java.util.Collection;

public class ManuelTest {
	public static void main(String[] args) {
		IPersistenceFacade persistenceFacade = new PersistenceFacade();
		Routine r = persistenceFacade.getNewRoutineDAO().findById("cfec958c-e34f-3240-bcea-cdeebd186cf6");
		Collection<Feature> features = r.getRequiredFeatures();
		for (Feature f : features) {
			System.out.println(f);
		}
	}
}
