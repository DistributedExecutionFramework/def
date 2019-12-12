package at.enfilo.def.domain.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FormalParameter.class)
public abstract class FormalParameter_ {

	public static volatile SingularAttribute<FormalParameter, DataType> dataType;
	public static volatile SingularAttribute<FormalParameter, String> name;
	public static volatile SingularAttribute<FormalParameter, String> description;
	public static volatile SingularAttribute<FormalParameter, String> id;

	public static final String DATA_TYPE = "dataType";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ID = "id";

}

