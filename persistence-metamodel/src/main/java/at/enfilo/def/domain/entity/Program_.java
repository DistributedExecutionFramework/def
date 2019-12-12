package at.enfilo.def.domain.entity;

import at.enfilo.def.transfer.dto.ExecutionState;
import java.time.Instant;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Program.class)
public abstract class Program_ {

	public static volatile SingularAttribute<Program, User> owner;
	public static volatile SingularAttribute<Program, Instant> finishTime;
	public static volatile SingularAttribute<Program, Instant> createTime;
	public static volatile CollectionAttribute<Program, Job> jobs;
	public static volatile CollectionAttribute<Program, Group> groups;
	public static volatile SingularAttribute<Program, String> id;
	public static volatile SingularAttribute<Program, ExecutionState> state;
	public static volatile SingularAttribute<Program, Boolean> masterLibraryRoutine;

	public static final String OWNER = "owner";
	public static final String FINISH_TIME = "finishTime";
	public static final String CREATE_TIME = "createTime";
	public static final String JOBS = "jobs";
	public static final String GROUPS = "groups";
	public static final String ID = "id";
	public static final String STATE = "state";
	public static final String MASTER_LIBRARY_ROUTINE = "masterLibraryRoutine";

}

