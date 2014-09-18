package org.beetl.core.parser;

/**
 * 生成token流
 * 
 * @author lijiazhi
 * 
 */
public class BeetlLexer {
	// 静态文本
	public final static int TEXT_TT = 0;;
	// 站位符开始
	public final static int PH_SS_TT = 1;;
	// 占位符结束
	public final static int PH_SE_TT = 2;
	// ID
	public final static int ID_TT = 3;
	// .
	public final static int PERIOD_TT = 4;

	public final static int INTEGER_TT = 5;
	public final static int FLOAT_TT = 6;
	
	public final static int INCREASE_TT = 7;
	public final static int DECREASE_TT = 8;
	
	public final static int ADD_TT = 9;
	public final static int MIN_TT = 10;
	
	public final static int LEFT_PAR_TT = 11;
	public final static int RIGHT_PAR_TT = 12;
	
	public final static int STRING_TT = 13;
	

	
	public static String PERIOD_CHAR =  ".";
	public static String ADD_CHAR =  "+";
	public static String MIN_CHAR =  "-";
	public static String INCREASE_CHAR =  "++";
	public static String DECREASE_CHAR =  "--";
	public static String LEFT_PAR_CHAR =  "(";
	public static String RIGHT_PAR_CHAR =  ")";
	
	
	
	
	
	
	LexerState state = null;
	Source source = null;
	LexerDelimiter ld;

	public BeetlLexer(Source source, LexerDelimiter ld) {
		state = new LexerState();
		this.source = source;
		this.ld = ld;
		this.source.setState(state);
	}

	public Token nextToken() {
		switch (state.model) {
		case LexerState.STATIC_MODEL:
			return textModel();
		case LexerState.PH_MODEL:
			return holderModel();
		case LexerState.PH_START:
			return placeHolderStartToken();
		case LexerState.PH_END:
			return placeHolderEndToken();

		}
		return null;
	}

	private Token textModel() {
		int c;

		int start = source.pos();
		int col = state.col;
		int line = state.line;
		while ((c = source.get()) != source.EOF) {
			if (c != ld.ps[0] && c != ld.ss[0]) {
				source.consume();
				if (c == '\r' || c == '\n') {
					consumeMoreCR(c);
				}
			} else {
				if (c == ld.ps[0] && source.isMath(ld.ps)) {

					if (source.hasEscape()) {
						source.consume();
						continue;
					} else {
						state.model = LexerState.PH_START;

						break;
					}

				} else if (c == ld.ss[0] && source.isMath(ld.ss)) {
					if (source.hasEscape()) {
						source.consume();
						continue;
					} else {
						state.model = LexerState.ST_START;

						break;
					}

				} else {
					source.consume();
				}
			}
		}

		return getToken(start, source.pos(), col, line, TEXT_TT);

	}

	private void consumeMoreCR(int crFirst) {
		state.addLine();
		if (state.cr_len == 1) {
			source.consume();
			return;
		} else if (state.cr_len == 2) {
			source.consume(2);
		} else {
			int c = source.get();
			if (c == source.EOF) {
				return;
			} else if (crFirst == '\n' && c == '\r') {
				state.cr_len = 2;
				source.consume(1);
			} else if (crFirst == '\r' && c == '\n') {
				state.cr_len = 2;
				source.consume(1);
			} else {
				state.cr_len = 1;
			}
		}
	}

	private Token holderModel() {
		int c ;
		while((c = source.get()) != source.EOF){
			// 判断是否是结束符号
			if (c == this.ld.pe[0] && source.isMath(this.ld.pe)) {
				if (!source.hasEscape()) {
					state.model = LexerState.PH_END;
					return null;
				}
			}

			Token t = new Token();
			t.start = source.pos();
			t.col = state.col;
			t.line = state.line;

			if (c > '0' && c < '9') {
				t.type = this.INTEGER_TT;
				numberToken(t);
				return t;
			} else if (c == '\'' || c == '\"') {
				t.type = this.STRING_TT;
				stringToken(t);
				return t;
			} else if (c == '.') {
				if(this.forwardMatch('0','9')){
					t.type = this.FLOAT_TT;
					numberToken(t);
					return t;
				}else{
					source.consume();
					t.end = source.pos();
					t.text=this.PERIOD_CHAR;
					t.type = this.PERIOD_TT;
					return t ;
				}
			} else if(c=='+'){
				if(this.forwardMatch('+')){
					source.consume(2);
					t.end = source.pos();
					t.text=this.INCREASE_CHAR;
					t.type = this.INCREASE_TT;
				}else{
					source.consume();
					t.end = source.pos();
					t.type = this.ADD_TT;
					t.text=this.ADD_CHAR;
					return t ;
				}
				
				
			}else if(c=='('){
				source.consume();
				t.end = source.pos();
				t.type = this.LEFT_PAR_TT;
				t.text=this.LEFT_PAR_CHAR;
				return t ;
				
			}else if(c==')'){
				source.consume();
				t.end = source.pos();
				t.type = this.RIGHT_PAR_TT;
				t.text=this.RIGHT_PAR_CHAR;
				return t ;
			}else if(c==' ') {
				//skip continue;
				source.consume();
				continue;
			}else if(c>='a'&&c<='z'||c>='A'&&c<='Z'){
				t.type= ID_TT;
				idToken(t);
				return t;
			}else{
				throw new RuntimeException("not support");
			}
		}
		
		//文件结束
		return null;

	}

	private void idToken(Token t){
		int c ;
		while((c = source.get())!=source.EOF){
			if(c>='a'&&c<='z'||c>='A'&&c<='Z'){
				source.consume();
			}else{
				break;
			}
		}
		int end = source.pos();
		t.end = end ;
		String text = source.getRange(t.start, t.end);
		t.text = text;
	}
	private void stringToken(Token t) {
		int c = source.get();		
		int find = c ;
		while((c = source.get())!=source.EOF){
			if(c==find){
				if(!source.hasEscape()){
					//结束
					source.consume();
					break ;
				}
			}else if (c=='\r'||c=='\n'){
				throw new RuntimeException("错误的字符串格式化");
			}
			else{
				source.consume();
			}
		}
		int end = source.pos();
		t.end = end ;
		String text = source.getRange(t.start, t.end);
		t.text = text;
		
		
	}

	private void numberToken(Token t) {
		int c;
		boolean period = false;
		while ((c = source.get()) != source.EOF) {
			if (c >= '0' || c <= '9') {
				source.consume();
			} else if (c == 'h' || c == 'H') {
				source.consume();
				break;
			} else if (c == '.') {
				if (period) {
					throw new RuntimeException("数字格式错误，多了一个点号");
				}
				source.consume();
				break;
			} else {
				throw new RuntimeException("数字格式错误，多了一个符号" + c + "  "
						+ source.pos());

			}

		}

		t.end = source.pos();
		t.text = source.getRange(t.start, t.end);
		

	}

	private Token placeHolderStartToken() {
		int start = source.pos();
		source.consume(ld.ps.length);
		int end = source.pos();
		Token token = getToken(start, end, state.col, state.line, PH_SS_TT);
		state.model = LexerState.ST_MODEL;
		return token;

	}

	private Token placeHolderEndToken() {
		int start = source.pos();
		source.consume(ld.se.length);
		int end = source.pos();
		Token token = getToken(start, end, state.col, state.line, PH_SE_TT);
		return token;

	}
	
	

	private Token getToken(int start, int end, int col, int line, int type) {

		String text = source.getRange(start, end);
		Token token = new Token(type, text);
		token.col = col;
		token.line = line;
		token.start = start;
		token.end = end;
		return token;

	}
	
	
	
	private boolean forwardMatch(int start,int end){
		int c = source.get(1);
		return c>=start&&c<=end;
	}
	
	private boolean forwardMatch(int m){
		int c = source.get(1);
		return c==m;
	}
	private boolean forwardMatch(int[] cs){
		int c = source.get(1);
		for(int i:cs){
			if(c==i){
				return true;
			}
		}
		return false;
	}
	

	public static void main(String[] args) {
		String template = "h\nt\ntttt";
		Source source = new Source(template);
		LexerDelimiter ld = new LexerDelimiter();
		ld.pe = "}".toCharArray();
		ld.ps = "${".toCharArray();
		ld.se = "%>".toCharArray();
		ld.ss = "<%".toCharArray();
		BeetlLexer lexer = new BeetlLexer(source, ld);
		Token token = lexer.nextToken();
		System.out.println(token);
	}
}
