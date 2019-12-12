package at.enfilo.def.domain.entity;

import at.enfilo.def.transfer.dto.ExecutionState;
import java.time.Instant;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Task.class)
public abstract class Task_ {

	public static volatile SingularAttribute<Task, Instant> finishTime;
	public static volatile SingularAttribute<Task, Instant> createTime;
	public static volatile SingularAttribute<Task, Instant> startTime;
	public static volatile SingularAttribute<Task, String> id;
	public static volatile SingularAttribute<Task, ExecutionState> state;

	public static final String FINISH_TIME = "finishTime";
	public static final String CREATE_TIME = "createTime";
	public static final String START_TIME = "startTime";
	public static final String ID = "id";
	public static final String STATE = "state";

}

