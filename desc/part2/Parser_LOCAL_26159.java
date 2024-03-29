package parser;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;



    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();

        parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private void parseProgram() {
        parseIncludes();
        
        parseStructDecls();
        System.out.println("parsedstructs");
        parseVarDecls();
        System.out.println("parsedvars");
        parseFunDecls();
        System.out.println("parsedfuncs");
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
	    if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }
    private void parseType() {
    	if(accept(TokenClass.STRUCT)) {
    		parseStructType();
    		if (accept(TokenClass.ASTERIX))
    			nextToken();
    	}
    	else {
    		expect(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID);
    		if (accept(TokenClass.ASTERIX))
    			nextToken();
    	}
    }
    private void parseTI() {
    	parseType();
    	expect(TokenClass.IDENTIFIER);
    }
    private void parseStructType() {
    	expect(TokenClass.STRUCT);
    	expect(TokenClass.IDENTIFIER);
    }
    private void parseStructDecl() {
    	parseStructType();
    	expect(TokenClass.LBRA);
    	parseVarDecl();
    	parseVarDecls();
    	expect(TokenClass.RBRA);
    	expect(TokenClass.SC);
    }
    private void parseStructDecls() {
        if(accept(TokenClass.STRUCT)) {
        	parseStructDecl();
        	parseStructDecls();
        }
    }
    private void parseVarDecl() {
        parseTI();
        parseVarTail();
    }
    
    private void parseVarDecls() {
        if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)) {
        //	parseTI();
        	System.out.println("var1");
           	if(accept(TokenClass.STRUCT)) {
           		if(lookAhead(1).tokenClass==TokenClass.IDENTIFIER) {
           			
        		     if(lookAhead(2).tokenClass==TokenClass.ASTERIX) {
        			     if(lookAhead(3).tokenClass==TokenClass.IDENTIFIER) {
                    	     if(lookAhead(4).tokenClass==TokenClass.LSBR || lookAhead(4).tokenClass==TokenClass.SC) {
                    		     parseVarDecl();
                    		     parseVarDecls();
                           	 }
            		      }
        		     }
        	    	 else if(lookAhead(2).tokenClass==TokenClass.IDENTIFIER) {
                   	    if(lookAhead(3).tokenClass==TokenClass.LSBR || lookAhead(3).tokenClass==TokenClass.SC) {

                   	       parseVarDecl();
                   		   parseVarDecls();
                 	    }
        		     }
        		     
           		}

        	}
        	else {
        		if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID)) {
            	//	System.out.println("var2");
            	//	System.out.println(lookAhead(1).tokenClass.toString());
            	//	System.out.println(lookAhead(2).tokenClass.toString());
        			if(lookAhead(1).tokenClass==TokenClass.ASTERIX) {
            			if(lookAhead(2).tokenClass==TokenClass.IDENTIFIER) {
                        	if(lookAhead(3).tokenClass==TokenClass.LSBR || lookAhead(3).tokenClass==TokenClass.SC) {
                        	    System.out.println("before_var_pointer");
                        		parseVarDecl();
                        		parseVarDecls();
                        	}
                		}
            		}
            		else if(lookAhead(1).tokenClass==TokenClass.IDENTIFIER) {
                    	if(lookAhead(2).tokenClass==TokenClass.LSBR || lookAhead(2).tokenClass==TokenClass.SC) {
                    		System.out.println("before_var");
                    		parseVarDecl();
                    		parseVarDecls();
                    	}
            		} 	
            		
        		}
        	}
            
        }
    }
    private void parseVarTail() {
    	if(accept(TokenClass.LSBR)) {
    		nextToken();
    		expect(TokenClass.INT_LITERAL);
    		expect(TokenClass.RSBR);
    		expect(TokenClass.SC);
    	}
    	else 
    		expect(TokenClass.SC);
    }


    private void parseParams() {
    	if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)) {
    		System.out.println("before_params");
    		parseTI();
    		parseParamsList();
    	}
    }
    private void parseParamsList() {
    	if(accept(TokenClass.COMMA)) {
    		nextToken();
    		parseTI();
    		parseParamsList();
    	}
    }
    private void parseExp() {
    	parseExp1();
    	if(accept(TokenClass.OR)) {
    		
    		nextToken();
    		parseExp();
    	}
    }
    private void parseExp1() {
    	parseExp2();
    	if(accept(TokenClass.AND)) {
    		nextToken();
    		parseExp1();
    	}
    }
    private void parseExp2() {
    	parseExp3();
    	if(accept(TokenClass.EQ,TokenClass.NE)) {
    		nextToken();
    		parseExp2();
    	}
    }
    private void parseExp3() {
    	parseExp4();
    	if(accept(TokenClass.LT,TokenClass.LE,TokenClass.GT,TokenClass.GE)) {
    		nextToken();
    		parseExp3();
    	}
    }
    private void parseExp4() {
    	parseExp5();
    	if(accept(TokenClass.PLUS,TokenClass.MINUS)) {
    		System.out.println("before_plusminus");
    		nextToken();
    		parseExp4();
    	}
    }
    private void parseExp5() {
    	parseExp6();
    	if(accept(TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM)) {
    		nextToken();
    		parseExp5();
    	}
    }
    private void parseExp6() {
    	if (accept(TokenClass.MINUS)) {
    		System.out.println("before_exp6minus");
    		nextToken();
    		expect(TokenClass.IDENTIFIER,TokenClass.INT_LITERAL);
    		if(accept(TokenClass.LSBR,TokenClass.DOT)) {
    			if(accept(TokenClass.LSBR)) {
    	  			nextToken();
    	  			parseExp();
    	  			expect(TokenClass.RSBR);
    	  		}
    	  		else {
    	  			expect(TokenClass.DOT);
    	  			expect(TokenClass.IDENTIFIER);
    	  		}
    		}
    	}
        else if(accept(TokenClass.SIZEOF)) {
    		parseSizeof();
    	}
    	else if(accept(TokenClass.ASTERIX)) {
    		nextToken();
    		parseExp();
    	}
    	else if(accept(TokenClass.LPAR) && (lookAhead(1).tokenClass==TokenClass.INT || lookAhead(1).tokenClass==TokenClass.CHAR || lookAhead(1).tokenClass==TokenClass.VOID || lookAhead(1).tokenClass==TokenClass.STRUCT) ) {
    		parseTcast();
    	}
    	else 
   
    		
    		
   
    	    parseExp7();
    }
    private void parseFuncall() {
    	System.out.println("before_funcall");
    	expect(TokenClass.IDENTIFIER);
    	expect(TokenClass.LPAR);
    	parseArglist();
    	expect(TokenClass.RPAR);
    }
    private void parseArglist() {
    	if(accept(TokenClass.INT_LITERAL,TokenClass.MINUS,TokenClass.ASTERIX,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.SIZEOF,TokenClass.LPAR,TokenClass.IDENTIFIER)) {
    	 	parseExp();
    	 	parseArgrep();
    	}
    }
    private void parseArgrep() {
    	if(accept(TokenClass.COMMA)) {
    		nextToken();
    		parseExp();
    		parseArgrep();
    	}
    }
    private void parseExp7() {
    	System.out.println("before_lastexp");
    	if(accept(TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.IDENTIFIER)) {
    		if(accept(TokenClass.IDENTIFIER)) {
        		if(lookAhead(1).tokenClass==TokenClass.LPAR) {
        			parseFuncall();
        		}
        		else {
        			expect(TokenClass.IDENTIFIER);
        			if(accept(TokenClass.LSBR,TokenClass.DOT)) {
            			if(accept(TokenClass.LSBR)) {
            	  			nextToken();
            	  			parseExp();
            	  			expect(TokenClass.RSBR);
            	  		}
            	  		else {
            	  			expect(TokenClass.DOT);
            	  			expect(TokenClass.IDENTIFIER);
            	  		}
            		}
        		}
    		}
    		else {
    			expect(TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL);
    			if(accept(TokenClass.LSBR,TokenClass.DOT)) {
        			if(accept(TokenClass.LSBR)) {
        	  			nextToken();
        	  			parseExp();
        	  			expect(TokenClass.RSBR);
        	  		}
        	  		else {
        	  			expect(TokenClass.DOT);
        	  			expect(TokenClass.IDENTIFIER);
        	  		}
        		}
    		}
    	  		
    	}
    	else {
    	  if(accept(TokenClass.LPAR)) {
    		System.out.println(token.toString());
    		expect(TokenClass.LPAR);
    		parseExp();
    		expect(TokenClass.RPAR);
    		if(accept(TokenClass.LSBR,TokenClass.DOT)) {
    			if(accept(TokenClass.LSBR)) {
    	  			nextToken();
    	  			parseExp();
    	  			expect(TokenClass.RSBR);
    	  		}
    	  		else {
    	  			expect(TokenClass.DOT);
    	  			expect(TokenClass.IDENTIFIER);
    	  		}
    		}
    	  }
    	  else 
    		  expect(TokenClass.LPAR,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.IDENTIFIER);
    	}
 
    }
    private void parseSizeof() {
    	expect(TokenClass.SIZEOF);
    	expect(TokenClass.LPAR);
    	parseType();
    	expect(TokenClass.RPAR);
    }
    private void parseTcast() {
    	expect(TokenClass.LPAR);
    	parseType();
    	expect(TokenClass.RPAR);
    	parseExp();
    }
    private void parseStmt() {
    	if(accept(TokenClass.LBRA)) {
    		
    		parseBlock();
    	}
    	else if(accept(TokenClass.IF)) {
    		System.out.println("before_if");
        	nextToken();
       // 	System.out.println(token.toString());
        	expect(TokenClass.LPAR);
        	parseExp();
       // 	System.out.println(token.toString());
        	expect(TokenClass.RPAR);
        	parseStmt();
        	if(accept(TokenClass.ELSE)) {
        		nextToken();
        		parseStmt();
        	}
    	}
    	else if(accept(TokenClass.WHILE)) {
    		nextToken();
    		expect(TokenClass.LPAR);
    		
    		parseExp();
    		
    		expect(TokenClass.RPAR);
    		parseStmt();
    	}
    	else if(accept(TokenClass.RETURN)) {
    		nextToken();
    		if(accept(TokenClass.LPAR,TokenClass.CHAR_LITERAL,TokenClass.INT_LITERAL, TokenClass.STRING_LITERAL, TokenClass.IDENTIFIER, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF)) {
    			parseExp();
    		    expect(TokenClass.SC);
    		}
    		else{
    		    expect(TokenClass.SC);
    		}
    	}
    	else {
    		parseExp();
    		if(accept(TokenClass.ASSIGN)) {
    			System.out.println("before_assign");
    			nextToken();
    			parseExp();
    			expect(TokenClass.SC);
    		}
    		else
    			expect(TokenClass.SC);
    	}

    }
    private void parseStmts() {
    	if(accept(TokenClass.LBRA,TokenClass.IF,TokenClass.WHILE,TokenClass.RETURN,TokenClass.MINUS,TokenClass.LPAR,TokenClass.SIZEOF,TokenClass.ASTERIX,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.IDENTIFIER)) {
    		System.out.println("before_stmts");
    		parseStmt();
    		parseStmts();
    	}
    }
    private void parseBlock() {
    	System.out.println("before_block");
    	expect(TokenClass.LBRA);
    	parseVarDecls();
    	parseStmts();
    	
    	expect(TokenClass.RBRA);
    	System.out.println("end_block");
    }
    private void parseFunDecl() {
    	parseTI();
    	expect(TokenClass.LPAR);
    	parseParams();
    	expect(TokenClass.RPAR);
    	parseBlock();
    }

    private void parseFunDecls() {
    	if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT))
           	if(accept(TokenClass.STRUCT)) {
           		if(lookAhead(1).tokenClass==TokenClass.IDENTIFIER) {
           			
        		     if(lookAhead(2).tokenClass==TokenClass.ASTERIX) {
        			     if(lookAhead(3).tokenClass==TokenClass.IDENTIFIER) {
                    	     if(lookAhead(4).tokenClass==TokenClass.LPAR) {
                    		 
                    	    	 parseFunDecl();
                    		     parseFunDecls();
                           	 }
            		      }
        		     }
        	    	 else if(lookAhead(2).tokenClass==TokenClass.IDENTIFIER) {
                   	    if(lookAhead(3).tokenClass==TokenClass.LPAR) {
                  	       parseFunDecl();
                   		   parseFunDecls();
                 	    }
        		     }
        		     
           		}

        	}
        	else {
        		if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID)) {
            		
        			if(lookAhead(1).tokenClass==TokenClass.ASTERIX) {
            			if(lookAhead(2).tokenClass==TokenClass.IDENTIFIER) {
                        	if(lookAhead(3).tokenClass==TokenClass.LPAR) {
                        	   
                        		parseFunDecl();
                        		parseFunDecls();
                        	}
                		}
            		}
            		else if(lookAhead(1).tokenClass==TokenClass.IDENTIFIER) {
                    	if(lookAhead(2).tokenClass==TokenClass.LPAR) {
                    		 System.out.println("before_func");
                    		parseFunDecl();
                    		parseFunDecls();
                    	}
            		} 	
            		
        		}
        	}


    }

    
    
    // to be completed ...        
}
