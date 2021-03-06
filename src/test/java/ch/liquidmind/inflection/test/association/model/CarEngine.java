package ch.liquidmind.inflection.test.association.model;

import ch.liquidmind.inflection.association.Aggregation;
import ch.liquidmind.inflection.association.annotations.Association;
import ch.liquidmind.inflection.association.annotations.Property;

public abstract class CarEngine extends PowerSource
{
	@Property( redefines = "vehicle" )
	public abstract Car getCar();
	public abstract void setCar( Car car );
	
	@Association( otherEnd = "carEngine" )
	@Property( aggregation = Aggregation.COMPOSITE, redefines = "vehicleConfiguration" )
	public abstract CarConfiguration getCarConfiguration();
	public abstract void setCarConfiguration( CarConfiguration carConfiguration );
}
