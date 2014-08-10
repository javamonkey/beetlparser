package org.beetl.core.parser;

/**
 * 生成token流
 * @author lijiazhi
 *
 */
public class BeetlLexer {
	public final static  int TEXT_TT = 0;;
	LexerState state = null;
	Source source = null;
	LexerDelimiter ld ;
	public BeetlLexer(Source source,LexerDelimiter ld){
		state = new LexerState();
		this.source = source;
		this.ld = ld ;
	}
	
	public Token nextToken(){
		switch(state.model){
		case LexerState.STATIC_MODEL:	return textModel() ;
		
		}
		return null;
	}
	
	private Token textModel(){
		
		 char ch = source.LA();
		 
		 while(ch!=Source .EOF){
			 	if(isText(ch)){
			 		continue;
			 	}else{
			 		break ;
			 	}
		 }
		 
		 return getToke(TEXT_TT);
		 
		 
	}
	
	private boolean isText(char c){
		
	}
	
	private Token getToke(int type){
		String text = source.getRange(start, end)
		Token token = new Token(type);
		
	}
}
