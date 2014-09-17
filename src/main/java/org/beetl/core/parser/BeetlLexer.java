package org.beetl.core.parser;

/**
 * 生成token流
 * @author lijiazhi
 *
 */
public class BeetlLexer {
	//静态文本
	public final static  int TEXT_TT = 0;;
	//站位符开始
	public final static  int PH_SS_TT = 1;;
	//占位符结束
	public final static  int PH_SE_TT = 2;
	// ID
	public final static  int ID_TT = 0;	
	// .
	public final static  int PERIOD_TT = 0;
	
	public final static  int NUMBER_TT = 0;
	
	
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
		case LexerState.PH_MODEL:	return holderModel() ;
		case LexerState.PH_START:	return placeHolderStartToken() ;
		case LexerState.PH_END:	return placeHolderEndToken() ;
		
		
		}
		return null;
	}
	
	private Token textModel(){
		int c ;
		
		int start = source.pos();
		int col = state.col;
		int line = state.line;
		while((c=source.get())!=source.EOF){
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
						state.model = LexerState.PH_START;
						
						break;
					}
					
				
				}else if(c==ld.ss[0]&&source.isMath(ld.ss)){
					if(source.hasEscape()){
						source.consume();
						continue ;
					}else{
						state.model = LexerState.ST_START;
						
						break;
					}
					
				}else{
					source.consume();
				}
			}
		}
		
		 
		 return getToken(start,source.pos(),col,line,TEXT_TT);
		 
		 
	}
	
	
	
	private void consumeMoreCR(int crFirst){
		state.addLine();
		if(state.cr_len==1){	
			source.consume();
			return ;
		}else if(state.cr_len==2){			
			source.consume(2);
		}else{
			int c = source.get();
			if(c==source.EOF){
				return ;
			}else if(crFirst=='\n'&&c=='\r'){
				state.cr_len = 2;
				source.consume(1);
			}else if(crFirst=='\r'&&c=='\n'){
				state.cr_len = 2;
				source.consume(1);
			}else{
				state.cr_len = 1;
			}
		}
	}
	
	
	private Token holderModel(){
		int c  =source.get();
		if(c==source.EOF) return null;
		//判断是否是结束符号
		if(c==this.ld.pe[0]&&source.isMath(this.ld.pe)){
			if(!source.hasEscape()){
				state.model = LexerState.PH_END;
				return  null;
			}
		}
		
		Token  t = new Token();
		t.start = source.pos();
		t.col = state.col;
		t.line = state.line;
		
		if(c>'0'&&c<'9'){
			  numberToken(t);
			  return t;
		}else if(c=='\''||c=='\"'){
			return stringToken(t)
		}else if(c=='.'){
			
		}else{
			//id token;
		}
	
	}
	
	private void numberToken(Token t){
		int c ;
		while((c=source.get())!=source.EOF){
			if(c>='0'||c<='9'){
				source.consume();
			}else{
				break;
			}
		}
		
		t.end = source.pos();
		t.text = source.getRange(t.start, t.end);
		t.type = NUMBER_TT;
		
		
		
	}
	
	private Token placeHolderStartToken(){
		int start = source.pos();
		source.consume(ld.ps.length);
		int end = source.pos();
		Token token = getToken(start,end,state.col,state.line,PH_SS_TT);
		state.model = LexerState.ST_MODEL;
		return token ;
		
	}
	
	private Token placeHolderEndToken(){
		int start = source.pos();
		source.consume(ld.se.length);
		int end = source.pos();
		Token token = getToken(start,end,state.col,state.line,PH_SE_TT);		
		return token ;
		
	}
	
	private Token getToken(int start,int end,int col,int line,int type){
	
		String text = source.getRange(start, end);
		Token token = new Token(type,text);
		token.col = col;
		token.line = line;
		token.start = start;
		token.end = end;
		return token;
		
	}
	
	public static void main(String[] args){
		String template = "h\nt\ntttt";
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
