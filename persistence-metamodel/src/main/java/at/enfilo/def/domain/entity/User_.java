package at.enfilo.def.domain.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ {

	public static volatile SingularAttribute<User, String> salt;
	public static volatile SingularAttribute<User, String> pass;
	public static volatile SingularAttribute<User, String> name;
	public static volatile SetAttribute<User, Group> groups;
	public static volatile SingularAttribute<User, String> id;

	public static final String SALT = "salt";
	public static final String PASS = "pass";
	public static final String NAME = "name";
	public static final String GROUPS = "groups";
	public static final String ID = "id";

}

