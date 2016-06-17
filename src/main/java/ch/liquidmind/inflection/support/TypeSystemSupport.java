package ch.liquidmind.inflection.support;

import java.util.HashSet;
import java.util.Set;

public class TypeSystemSupport
{
	public static final Set< Class< ? > > BASIC_TYPES = new HashSet< Class< ? > >();
	public static final Set< Class< ? > > WRAPPER_TYPES = new HashSet< Class< ? > >();
	public static final Set< Class< ? > > BASIC_AND_WRAPPER_TYPES = new HashSet< Class< ? > >();

	static
	{
		BASIC_TYPES.add( byte.class );
		BASIC_TYPES.add( short.class );
		BASIC_TYPES.add( int.class );
		BASIC_TYPES.add( long.class );
		BASIC_TYPES.add( float.class );
		BASIC_TYPES.add( double.class );
		BASIC_TYPES.add( char.class );
		BASIC_TYPES.add( boolean.class );
		
		WRAPPER_TYPES.add( Byte.class );
		WRAPPER_TYPES.add( Short.class );
		WRAPPER_TYPES.add( Integer.class );
		WRAPPER_TYPES.add( Long.class );
		WRAPPER_TYPES.add( Float.class );
		WRAPPER_TYPES.add( Double.class );
		WRAPPER_TYPES.add( Character.class );
		WRAPPER_TYPES.add( Boolean.class );
		
		BASIC_AND_WRAPPER_TYPES.addAll( BASIC_TYPES );
		BASIC_AND_WRAPPER_TYPES.addAll( WRAPPER_TYPES );
	}
	
	
}
