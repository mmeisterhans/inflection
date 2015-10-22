package ch.liquidmind.inflection.compiler;

import java.io.FileInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import __java.io.__Closeable;
import __java.io.__FileInputStream;
import __org.antlr.v4.runtime.__ANTLRInputStream;
import ch.liquidmind.inflection.grammar.InflectionLexer;
import ch.liquidmind.inflection.grammar.InflectionParser;
import ch.liquidmind.inflection.model.compiled.TaxonomyCompiled;

public class InflectionCompiler
{
	public static final String INFLECT_SUFFIX = ".inflect";
	
	private static ParseTreeWalker parseTreeWalker = new ParseTreeWalker();

	public static void compile( CompilationJob job )
	{
		parse( job );
		
		if ( !job.hasCompilationErrors() )
			compilePass1( job );
		
		if ( !job.hasCompilationErrors() )
			compilePass2( job );
		
		if ( !job.hasCompilationErrors() )
			save( job );
	}
	
	private static void parse( CompilationJob job )
	{
		for ( CompilationUnit compilationUnit : job.getCompilationUnits() )
		{
			FileInputStream compilationUnitIs = __FileInputStream.__new( compilationUnit.getCompilationUnitRaw().getSourceFile() );
			
			try
			{
				parse( compilationUnit, compilationUnitIs );
			}
			finally
			{
				__Closeable.close( compilationUnitIs );
			}
		}
	}

	private static void parse( CompilationUnit compilationUnit, FileInputStream compilationUnitIs )
	{
		ANTLRInputStream antlrInputStream = __ANTLRInputStream.__new( compilationUnitIs );
		InflectionLexer lexer = new InflectionLexer( antlrInputStream );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		InflectionParser parser = new InflectionParser( tokens );
		parser.removeErrorListeners();
		parser.addErrorListener( compilationUnit.getErrorListener() );
		ParseTree tree = parser.compilationUnit();
		compilationUnit.getCompilationUnitParsed().setParseTree( tree );
		compilationUnit.getCompilationUnitParsed().setTokens( tokens );
	}
	
	private static void compilePass1( CompilationJob job )
	{
		for ( CompilationUnit compilationUnit : job.getCompilationUnits() )
			parseTreeWalker.walk( new Pass1Listener( compilationUnit ), compilationUnit.getCompilationUnitParsed().getParseTree() );
	}
	
	private static void compilePass2( CompilationJob job )
	{
		for ( CompilationUnit compilationUnit : job.getCompilationUnits() )
			parseTreeWalker.walk( new Pass2Listener( compilationUnit ), compilationUnit.getCompilationUnitParsed().getParseTree() );
	}
	
	private static void save( CompilationJob job )
	{
		for ( CompilationUnit compilationUnit : job.getCompilationUnits() )
			for ( TaxonomyCompiled taxonomyCompiled : compilationUnit.getCompilationUnitCompiled().getTaxonomiesCompiled() )
				taxonomyCompiled.save( job.getTargetDirectory() );
	}
}
