package ast;

public class StrLiteral extends Expr {
	public String str;
	public String label;
	public int eval() {return -1;}
//	String toStr() {return ""+str;}
	public char[] chararray;
	public StrLiteral(String s){ str = s; } 
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }
	
}