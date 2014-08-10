package org.beetl.core.parser;

/** 读取或者消费单个字符
 * @author lijiazhi
 *
 */
public class Source {
	char[] cs = null;
	int p ;
	int size = 0;
	static int EOF = -1;
	public Source(String template){
		cs = template.toCharArray();
		size  = cs.length;
	}
	public char LA(){
		if(p>size-1){
			return  (char)-1 ;
		}else{
			return cs[p];
		}
	}
	

	public void consume(){
		p++;
	}
	
	public String getRange(int start,int end){
		return new String(cs,start,end-start);
	}
}
