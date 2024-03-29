package ast;

public class ChrLiteral extends Expr {
	public char c;
	public String clabel;
	public int eval() {return Character.getNumericValue(c);}
//	String toStr() {return ""+c;}
	public ChrLiteral(char c){ this.c = c; }
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }
}