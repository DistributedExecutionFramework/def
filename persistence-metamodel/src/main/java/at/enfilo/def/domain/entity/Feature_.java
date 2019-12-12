package at.enfilo.def.domain.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Feature.class)
public abstract class Feature_ {

	public static volatile SingularAttribute<Feature, String> name;
	public static volatile SingularAttribute<Feature, Feature> baseFeature;
	public static volatile SingularAttribute<Feature, String> id;
	public static volatile CollectionAttribute<Feature, Feature> subFeatures;
	public static volatile SingularAttribute<Feature, String> version;
	public static volatile SingularAttribute<Feature, String> group;
	public static volatile SingularAttribute<Feature, String> baseFeatureId;

	public static final String NAME = "name";
	public static final String BASE_FEATURE = "baseFeature";
	public static final String ID = "id";
	public static final String SUB_FEATURES = "subFeatures";
	public static final String VERSION = "version";
	public static final String GROUP = "group";
	public static final String BASE_FEATURE_ID = "baseFeatureId";

}

