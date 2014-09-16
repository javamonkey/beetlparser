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
		this.source.setState(state);
	}
	
	public Token nextToken(){
		switch(state.model){
		case LexerState.STATIC_MODEL:	return textModel() ;
		case LexerState.PH_MODEL:	return textModel() ;
		}
		return null;
	}
	
	private Token textModel(){
		char c ;
		
		int mark = source.mark();
		while((c=source.get())!=-1){
			if(c!=ld.ps[0]&&c!=ld.ss[0]){
				source.consume();
				if(c=='\r'||c=='\n'){
					consumeMoreCR(c);
				}
			}else{
				if(c==ld.ps[0]&&source.isMath(ld.ps)){	
						
					if(source.hasEscape()){
						source.consume();
						continue ;
					}else{
						state.model = LexerState.ST_MODEL;
						source.consume(ld.ps.length);
						break;
					}
					
				
				}else if(c==ld.ss[0]&&source.isMath(ld.ss)){
					if(source.hasEscape()){
						source.consume();
						continue ;
					}else{
						state.model = LexerState.ST_MODEL;
						source.consume(ld.ss.length);
						break;
					}
					
				}else{
					source.consume();
				}
			}
		}
		
		 
		 return getToke(mark,TEXT_TT);
		 
		 
	}
	
	
	
	private void consumeMoreCR(char crFirst){
		state.addLine();
		if(state.cr_len==1){	
			source.consume();
			return ;
		}else if(state.cr_len==2){			
			source.consume(2);
		}else{
			int c = source.get(1);
			if(c==-1){
				return ;
			}else if(crFirst=='\n'&&c=='\r'){
				state.cr_len = 2;
				source.consume(2);
			}else if(crFirst=='\r'&&c=='\n'){
				state.cr_len = 2;
				source.consume(2);
			}else{
				state.cr_len = 1;
			}
		}
	}
	
	
	private Token holderModel(){
		return null;
	}
	
	private boolean isText(char c){
		return true;
	}
	
	private Token getToke(int start,int type){
		int end = source.mark;
		String text = source.getRange(start, end);
		Token token = new Token(type,text);
		token.col = state.col;
		token.line = state.line;
		token.start = start;
		token.end = end;
		return token;
		
		
	}
	
	public static void main(String[] args){
		String template = "h\nt";
		Source source = new Source(template);
		LexerDelimiter ld = new LexerDelimiter();
		ld.pe="}".toCharArray();
		ld.ps="${".toCharArray();
		ld.se="%>".toCharArray();
		ld.ss ="<%".toCharArray();
		BeetlLexer lexer = new BeetlLexer(source,ld);
		Token token = lexer.nextToken();
		System.out.println(token);
	}
}
