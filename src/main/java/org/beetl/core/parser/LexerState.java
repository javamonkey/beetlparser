package org.beetl.core.parser;

public class LexerState {
	/*0:静态文本 1 占位符 2 语句 3 注释*/
	public int model = 0;
	public int start = 0;
	public int end = 0;
	public int line = 1;
	
	public static final int STATIC_MODEL = 0;
	public static final int PH_MODEL = 0;
	public static final int ST_MODEL = 0;
	public static final int COMMENT_MODEL = 0;
	
	
	
	
}