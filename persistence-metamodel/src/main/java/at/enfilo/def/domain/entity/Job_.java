package at.enfilo.def.domain.entity;

import at.enfilo.def.transfer.dto.ExecutionState;
import java.time.Instant;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Job.class)
public abstract class Job_ {

	public static volatile SingularAttribute<Job, Instant> finishTime;
	public static volatile SingularAttribute<Job, Instant> createTime;
	public static volatile SingularAttribute<Job, String> id;
	public static volatile SingularAttribute<Job, ExecutionState> state;

	public static final String FINISH_TIME = "finishTime";
	public static final String CREATE_TIME = "createTime";
	public static final String ID = "id";
	public static final String STATE = "state";

}

