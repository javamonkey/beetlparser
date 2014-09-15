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
	int mark = 0;
	LexerState state = null;
	public Source(String template){
		cs = template.toCharArray();
		size  = cs.length;
	}
	public char get(){
		if( p<size-1){
			return cs[p];
		}else{
			return  (char)-1 ;
			
		}
	}
	
	public char get(int i){
		if( (p+i)<size-1){
			return cs[p+i];
		}else{
			return  (char)-1 ;
			
		}
	}
	
	
	public boolean isMath(char[] str){
		int cur = p;
		for(int i=0;i<cs.length;i++){
			if(cur<size-1&&cs[p]==str[i]){
				cur++;
			}else{
				return false ;
			}
		}
		return false;
	}
	


	public void consume(){
		p++;
		state.col++;
	}
	
	public void consume(int i){
		p = p+i;
		state.col= state.col+i;
	}
	
	public int mark(){
		 return  this.p;
	}
	
	public void seek(int index){
		this.p = index;
	}
	
	public String getRange(int start,int end){
		return new String(cs,start,end-start);
	}
	public void setState(LexerState state) {
		this.state = state;
	}
	
	
}
