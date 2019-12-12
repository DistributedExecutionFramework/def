package at.enfilo.def.domain.entity;

import at.enfilo.def.transfer.dto.Language;
import at.enfilo.def.transfer.dto.RoutineType;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Routine.class)
public abstract class Routine_ {

	public static volatile SetAttribute<Routine, Feature> requiredFeatures;
	public static volatile SingularAttribute<Routine, Boolean> privateRoutine;
	public static volatile ListAttribute<Routine, FormalParameter> inParameters;
	public static volatile SingularAttribute<Routine, String> description;
	public static volatile SingularAttribute<Routine, Language> language;
	public static volatile SingularAttribute<Routine, Routine> predecessor;
	public static volatile SingularAttribute<Routine, RoutineType> type;
	public static volatile SingularAttribute<Routine, Short> revision;
	public static volatile SingularAttribute<Routine, String> name;
	public static volatile ListAttribute<Routine, RoutineBinary> routineBinaries;
	public static volatile ListAttribute<Routine, String> arguments;
	public static volatile SingularAttribute<Routine, String> id;
	public static volatile SingularAttribute<Routine, FormalParameter> outParameter;

	public static final String REQUIRED_FEATURES = "requiredFeatures";
	public static final String PRIVATE_ROUTINE = "privateRoutine";
	public static final String IN_PARAMETERS = "inParameters";
	public static final String DESCRIPTION = "description";
	public static final String LANGUAGE = "language";
	public static final String PREDECESSOR = "predecessor";
	public static final String TYPE = "type";
	public static final String REVISION = "revision";
	public static final String NAME = "name";
	public static final String ROUTINE_BINARIES = "routineBinaries";
	public static final String ARGUMENTS = "arguments";
	public static final String ID = "id";
	public static final String OUT_PARAMETER = "outParameter";

}

