package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;
import static br.ufma.ecp.token.TokenType.*;

import br.ufma.ecp.SymbolTable.Kind;
import br.ufma.ecp.SymbolTable.Symbol;
import br.ufma.ecp.VMWriter.Command;
import br.ufma.ecp.VMWriter.Segment;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();
    private VMWriter vmWriter = new VMWriter();
    private int ifLabelNum = 0 ;
    private int whileLabelNum = 0;
    private SymbolTable symTable = new SymbolTable();
    private String className = "";
    public Parser(byte[] input) {
        scan = new Scanner(input);
        vmWriter = new VMWriter();
        nextToken();
    }

    public void parse() {
        parseClass();
    }

    void parseSubroutineCall() {

        var nArgs = 0;

        var ident = currentToken.value();
        var symbol = symbolTable.resolve(ident); 
        var functionName = ident + ".";

        if (peekTokenIs(LPAREN)) { 
            expectPeek(LPAREN);
            vmWriter.writePush(Segment.POINTER, 0);
            nArgs = parseExpressionList() + 1;
            expectPeek(RPAREN);
            functionName = className + "." + ident;
        } else {
            
            expectPeek(DOT);
            expectPeek(IDENTIFIER); 

            if (symbol != null) { 
                functionName = symbol.type() + "." + currentToken.value();
                vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
                nArgs = 1; 
            } else {
                functionName += currentToken.value(); 
            }

            expectPeek(LPAREN);
            nArgs += parseExpressionList();

            expectPeek(RPAREN);
        }

        vmWriter.writeCall(functionName, nArgs);
    }
    void parseClass() {
        printNonTerminal("class");
        expectPeek(CLASS);
        expectPeek(IDENT);
        expectPeek(LBRACE);
        className = currentToken.lexeme;

        while (peekTokenIs(STATIC) || peekTokenIs(FIELD)) {
            System.out.println(peekToken);
            parseClassVarDec();
        }
    
        while (peekTokenIs(FUNCTION) || peekTokenIs(CONSTRUCTOR) || peekTokenIs(METHOD)) {
            parseSubroutineDec();
        }

        expectPeek(RBRACE);

        printNonTerminal("/class");
    }
    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }

    boolean peekTokenIs(TokenType type) {
        return peekToken.type == type;
    }

    boolean currentTokenIs(TokenType type) {
        return currentToken.type == type;
    }


    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }
 
    private static void report(int line, String where,
    String message) {
        System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    }
    
    private void expectPeek(TokenType... types) {
        for (TokenType type : types) {
            if (peekToken.type == type) {
                expectPeek(type);
                return;
            }
        }

        throw error(peekToken, "Expected a statement");

    }
    private ParseError error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
        return new ParseError();
    }
    
    private void match(TokenType t) {
        if (currentToken.type == t) {
            nextToken();
        }else {
            throw new Error("syntax error");
        }
   }



  void parseTerm() {
        printNonTerminal("term");
        switch (peekToken.type) {
            case NUMBER:
                expectPeek(NUMBER);
                vmWriter.writePush(Segment.CONST, Integer.parseInt(currentToken.lexeme));

                break;
            case STRING:
                expectPeek(STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
            case THIS:
                expectPeek(FALSE, NULL, TRUE, THIS);
                break;
            case IDENT:
                expectPeek(IDENT);
                Symbol sym = symTable.resolve(currentToken.lexeme);

                if (peekTokenIs(LBRACKET)) { // array
                    expectPeek(LBRACKET);
                    parseExpression();
                    vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
                    vmWriter.writeArithmetic(Command.ADD);
    

                    expectPeek(RBRACKET);
                    vmWriter.writePop(Segment.POINTER, 1); // pop address pointer into pointer 1
                    vmWriter.writePush(Segment.THAT, 0);   // push the value of the address pointer back onto stack
    
                } else {
                    vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
                }
                break;
                case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
                break;
            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                parseTerm();
                break;
            default:
                ;
        }
        printNonTerminal("/term");
    }
    static public boolean isOperator(String op) {
        return op != "" && "+-*/<>=~&|".contains(op);
    }


 
    void parseIf() {
       
        ifLabelNum++;
        printNonTerminal("ifStatement");
        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
        printNonTerminal("/ifStatement");
    }
    
        int parseExpressionList() {
            printNonTerminal("expressionList");
    
            var nArgs = 0;
    
            if (!peekTokenIs(RPAREN)) // verifica se tem pelo menos uma expressao
            {
                parseExpression();
                nArgs = 1;
            }
    
            // procurando as demais
            while (peekTokenIs(COMMA)) {
                expectPeek(COMMA);
                parseExpression();
                nArgs++;
            }
    
            printNonTerminal("/expressionList");
            return nArgs;
        }
    

    public void parseExpression() {
        printNonTerminal("expression");
        parseTerm ();
        while (isOperator(peekToken.lexeme)) {
            var ope = peekToken.type;
            expectPeek(peekToken.type);
            parseTerm();
            compileOperators(ope);
        }
        printNonTerminal("/expression");
    }



    void parseLet() {
        var isArray = false;

        printNonTerminal("letStatement");
        expectPeek(LET);
        expectPeek(IDENT);

        if (peekTokenIs(LBRACKET)) { // array
            expectPeek(LBRACKET);
            parseExpression();
            
            vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
            vmWriter.writeArithmetic(Command.ADD);
    
            expectPeek(RBRACKET);



            isArray = true;
        }

        expectPeek(EQ);
        parseExpression();
        if (isArray) {

            vmWriter.writePop(Segment.TEMP, 0);    // push result back onto stack
            vmWriter.writePop(Segment.POINTER, 1); // pop address pointer into pointer 1
            vmWriter.writePush(Segment.TEMP, 0);   // push result back onto stack
            vmWriter.writePop(Segment.THAT, 0);    // Store right hand side evaluation in THAT 0.
    

        } else {
            vmWriter.writePop(kind2Segment(symbol.kind()), symbol.index());
        }

        void parseStatements() {
            printNonTerminal("statements");
            while (peekToken.type == WHILE ||
                    peekToken.type == IF ||
                    peekToken.type == LET ||
                    peekToken.type == DO ||
                    peekToken.type == RETURN) {
                parseStatement();
            }
    
            printNonTerminal("/statements");
        }
        void parseStatement() {
        switch (peekToken.type) {
            case LET:
                parseLet();
                break;
            case WHILE:
                parseWhile();
                break;
            case IF:
                parseIf();
                break;
            case RETURN:
                parseReturn();
                break;
            case DO:
                parseDo();
                break;
            default:
                throw error(peekToken, "Expected a statement");
        }
    }


    public void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(DO);
        expectPeek(IDENT);
        parseSubroutineCall();
        expectPeek(SEMICOLON);
        printNonTerminal("/doStatement");
    }

    void parseWhile() {
        printNonTerminal("whileStatement");

        var labelTrue = "WHILE_EXP" + whileLabelNum;
        var labelFalse = "WHILE_END" + whileLabelNum;
        whileLabelNum++;

        vmWriter.writeLabel(labelTrue);

        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();

        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(labelFalse);

        expectPeek(RPAREN);
        expectPeek(LBRACE);
        parseStatements();

        vmWriter.writeGoto(labelTrue); // Go back to labelTrue and check condition
        vmWriter.writeLabel(labelFalse); // Breaks out of while loop because ~(condition) is true

        expectPeek(RBRACE);
        printNonTerminal("/whileStatement");
    }
    void parseReturn() {
        printNonTerminal("returnStatement");
        expectPeek(RETURN);
        if (!peekTokenIs(SEMICOLON)) {
            parseExpression();
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/returnStatement");
    }

    void parseSubroutineDec() {
        printNonTerminal("subroutineDec");
        ifLabelNum = 0;
        expectPeek(CONSTRUCTOR, FUNCTION, METHOD);
        // 'int' | 'char' | 'boolean' | className
        expectPeek(VOID, INT, CHAR, BOOLEAN, IDENT);
        expectPeek(IDENT);

        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);
        parseSubroutineBody();
        symTable.startSubroutine();

        
        printNonTerminal("/subroutineDec");
        ifLabelNum = 0;
        whileLabelNum = 0;
        symTable.startSubroutine();
        expectPeek(TokenType.CONSTRUCTOR, TokenType.FUNCTION, TokenType.METHOD);
        var subroutineType = currentToken.type;
      
        
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

        SymbolTable.Kind kind = Kind.ARG;

        if (!peekTokenIs(RPAREN)) // verifica se tem pelo menos uma expressao
        {
            expectPeek(INT, CHAR, BOOLEAN, IDENT);
            String type = currentToken.lexeme;

            expectPeek(IDENT);
            String name = currentToken.lexeme;
            symTable.define(name, type, kind);

            while (peekTokenIs(COMMA)) {
                expectPeek(COMMA);
                expectPeek(INT, CHAR, BOOLEAN, IDENT);
                type = currentToken.lexeme;

                expectPeek(IDENT);
                name = currentToken.lexeme;

                symTable.define(name, type, kind);
            }

        }

        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody() {
    
        printNonTerminal("subroutineBody");
        expectPeek(LBRACE);
        while (peekTokenIs(VAR)) {
            parseClassVarDec();
        }

        parseStatements();
        expectPeek(RBRACE);
        printNonTerminal("/subroutineBody");
    }

    void parseClassVarDec() {
        printNonTerminal("classVarDec");
        expectPeek(FIELD, STATIC);

        SymbolTable.Kind kind = Kind.STATIC;
        if (currentTokenIs(FIELD))
            kind = Kind.FIELD;

        // 'int' | 'char' | 'boolean' | className
        expectPeek(INT, CHAR, BOOLEAN, IDENT);
        String type = currentToken.lexeme;

        expectPeek(IDENT);
        String name = currentToken.lexeme;

        symTable.define(name, type, kind);
        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            expectPeek(IDENT);

            name = currentToken.lexeme;
            symTable.define(name, type, kind);
        }

        expectPeek(SEMICOLON);
        printNonTerminal("/classVarDec");
    }

    public void compileOperators(TokenType type) {

        if (type == ASTERISK) {
            vmWriter.writeCall("Math.multiply", 2);
        } else if (type == SLASH) {
            vmWriter.writeCall("Math.divide", 2);
        } else {
            vmWriter.writeArithmetic(typeOperator(type));
        }
    }

    private Command typeOperator(TokenType type) {
        if (type == PLUS)
            return Command.ADD;
        if (type == MINUS)
            return Command.SUB;
        if (type == LT)
            return Command.LT;
        if (type == GT)
            return Command.GT;
        if (type == EQ)
            return Command.EQ;
        if (type == AND)
            return Command.AND;
        if (type == OR)
            return Command.OR;
        return null;
    }
    public String VMOutput() {
        return vmWriter.vmOutput();
}
private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
    }
private ParseError error(Token token, String message) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message);
    } else {
        report(token.line, " at '" + token.value() + "'", message);
    }
    return new ParseError();
}
private Segment kind2Segment(Kind kind) {
    if (kind == Kind.STATIC)
        return Segment.STATIC;
    if (kind == Kind.FIELD)
        return Segment.THIS;
    if (kind == Kind.VAR)
        return Segment.LOCAL;
    if (kind == Kind.ARG)
        return Segment.ARG;
    return null;
}
private Command typeOperator(TokenType type) {
    if (type == PLUS)
        return Command.ADD;
    if (type == MINUS)
        return Command.SUB;
    if (type == LT)
        return Command.LT;
    if (type == GT)
        return Command.GT;
    if (type == EQ)
        return Command.EQ;
    if (type == AND)
        return Command.AND;
    if (type == OR)
        return Command.OR;
    return null;
}

}
}
