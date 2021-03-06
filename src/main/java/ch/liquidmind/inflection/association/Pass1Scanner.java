package ch.liquidmind.inflection.association;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.reflect.ClassPath.ClassInfo;

import __java.beans.__PropertyDescriptor;
import ch.liquidmind.inflection.support.DimensionsTypeVisitor;
import ch.liquidmind.inflection.support.RelatedTypeVisitor;
import ch.liquidmind.inflection.support.TypeSystemSupport;
import ch.liquidmind.inflection.support.TypeWalker;

public class Pass1Scanner extends AbstractScanner
{
	private Set< ClassInfo > rawClasses;
	
	public Pass1Scanner( Set< ClassInfo > rawClasses, Map< String, Class > classes )
	{
		super( classes );
		this.rawClasses = rawClasses;
	}

	public void scan()
	{
		getClasses().putAll( rawClasses.stream().map( classInfo -> createClass( classInfo ) ).
			collect( Collectors.toMap( aClass -> aClass.getName(), aClass -> aClass ) ) );
		getClasses().values().forEach( aClass -> setupProperties( aClass ) );
	}
	
	private Class createClass( ClassInfo classInfo )
	{
		java.lang.Class< ? > aClass = classInfo.load();
		
		Map< String, Property > properties = createProperties( aClass );
		Class createdClass = new Class( aClass, properties );
		
		return createdClass;
	}
	
	private Map< String, Property > createProperties( java.lang.Class< ? > aClass )
	{
		Set< PropertyDescriptor > javaBeanProperties = getDeclaredJavaBeanProperties( aClass );
		Map< String, Property > properties = javaBeanProperties.stream().map( property -> new Property( property ) ).
				collect( Collectors.toMap( property -> property.getName(), property -> property ) );
		
		return properties;
	}

	private Set< PropertyDescriptor > getDeclaredJavaBeanProperties( java.lang.Class< ? > declaringClass )
	{
		List< PropertyDescriptor > allProperties = Arrays.asList( PropertyUtils.getPropertyDescriptors( declaringClass ) ).stream().map( property -> 
			__PropertyDescriptor.__new( property.getName(), property.getReadMethod(), property.getWriteMethod() ) ).collect( Collectors.toList() );
		allProperties.forEach( property -> removeInheritedMethods( declaringClass, property ) );
		Set< PropertyDescriptor > declaredProperties = allProperties.stream().filter( property -> !isPropertyEmpty( property ) ).collect( Collectors.toSet() );
		
		return declaredProperties;
	}
	
	private void removeInheritedMethods( java.lang.Class< ? > inheritingClass, PropertyDescriptor property )
	{
		if ( property.getWriteMethod() != null && !property.getWriteMethod().getDeclaringClass().equals( inheritingClass ) )
			__PropertyDescriptor.setWriteMethod( property, null );
		if ( property.getReadMethod() != null && !property.getReadMethod().getDeclaringClass().equals( inheritingClass ) )
			__PropertyDescriptor.setReadMethod( property, null );
	}
	
	private boolean isPropertyEmpty( PropertyDescriptor property )
	{
		return property.getReadMethod() == null && property.getWriteMethod() == null;
	}
	
	private void setupProperties( Class aClass )
	{
		aClass.getOwnedProperties().forEach( property -> setupProperty( property ) );
	}
	
	private void setupProperty( Property property )
	{
		ch.liquidmind.inflection.association.annotations.Property propertyAnnotation = getPropertyAnnotation( property );
		property.setRelatedType( determineRelatedType( property ) );
		property.setAggregation( determineAggregation( property, propertyAnnotation ) );
		property.setDimensions( determineDimensions( property ) );
		property.setDerived( ( propertyAnnotation == null ? false : propertyAnnotation.isDerived() ) );
		property.setDerivedUnion( ( propertyAnnotation == null ? false : propertyAnnotation.isDerivedUnion() ) );
		property.setDeclared( propertyAnnotation != null );
	}
	
	private Type determineRelatedType( Property property )
	{
		Type propertyType = getPropertyType( property );
		RelatedTypeVisitor visitor = new RelatedTypeVisitor();
		TypeWalker walker = new TypeWalker( visitor );
		walker.walk( propertyType );
		Type relatedType = visitor.getRelatedType();
		
		return relatedType;
	}
	
	private Type getPropertyType( Property property )
	{
		PropertyDescriptor descriptor = property.getTargetProperty();
		Method readMethod = descriptor.getReadMethod();
		Method writeMethod = descriptor.getWriteMethod();
		Type propertyType;
		
		if ( readMethod != null )
			propertyType = readMethod.getGenericReturnType();
		else if ( writeMethod != null )
			propertyType = writeMethod.getGenericParameterTypes()[ 0 ];
		else
			throw new IllegalStateException( "descriptor does not contain a read or write method." );
		
		return propertyType;
	}

	private Aggregation determineAggregation( Property property, ch.liquidmind.inflection.association.annotations.Property propertyAnnotation )
	{
		Aggregation aggregation;
		
		if ( propertyAnnotation == null )
		{
			if ( TypeSystemSupport.isValueType( property.getTargetProperty().getPropertyType() ) )
				aggregation = Aggregation.COMPOSITE;
			else
				aggregation = Aggregation.NONE;
		}
		else
		{
			aggregation = propertyAnnotation.aggregation();
		}

		return aggregation;
	}
	
	private List< Dimension > determineDimensions( Property property )
	{
		Type propertyType = getPropertyType( property );
		DimensionsTypeVisitor visitor = new DimensionsTypeVisitor();
		TypeWalker walker = new TypeWalker( visitor );
		walker.walk( propertyType );
		List< Dimension > dimensions = visitor.getDimensions();

		return dimensions;
	}
}
