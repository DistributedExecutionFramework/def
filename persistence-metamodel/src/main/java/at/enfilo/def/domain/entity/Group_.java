package at.enfilo.def.domain.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Group.class)
public abstract class Group_ {

	public static volatile SingularAttribute<Group, String> name;
	public static volatile SingularAttribute<Group, String> description;
	public static volatile SingularAttribute<Group, String> id;
	public static volatile CollectionAttribute<Group, User> users;

	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ID = "id";
	public static final String USERS = "users";

}

