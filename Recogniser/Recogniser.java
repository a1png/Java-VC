/***
 * *
 * * Recogniser.java            
 * *
 ***/

/* At this stage, this parser accepts a subset of VC defined	by
 * the following grammar. 
 *
 * You need to modify the supplied parsing methods (if necessary) and 
 * add the missing ones to obtain a parser for the VC language.
 *
 * (17---March---2017)

program       -> func-decl

// declaration

func-decl     -> void identifier "(" ")" compound-stmt

identifier    -> ID

// statements 
compound-stmt -> "{" stmt* "}" 
stmt          -> continue-stmt
    	      |  expr-stmt
continue-stmt -> continue ";"
expr-stmt     -> expr? ";"

// expressions 
expr                -> assignment-expr
assignment-expr     -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
*/

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;
import jdk.nashorn.internal.runtime.regexp.joni.Syntax;

public class Recogniser {

    private Scanner scanner;
    private ErrorReporter errorReporter;
    private Token currentToken;

    public Recogniser (Scanner lexer, ErrorReporter reporter) {
        scanner = lexer;
        errorReporter = reporter;

        currentToken = scanner.getToken();
    }

    private void print(Token token) {
        System.out.println(token);
    }

// match checks to see if the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

    void match(int tokenExpected) throws SyntaxError {

        if (currentToken.kind == tokenExpected) {
            currentToken = scanner.getToken();
        } else {
            syntacticError("\"%\" expected here", Token.spell(tokenExpected));
        }
    }

    // accepts the current token and fetches the next
    void accept() {
        currentToken = scanner.getToken();
    }

    void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
        SourcePosition pos = currentToken.position;
        errorReporter.reportError(messageTemplate, tokenQuoted, pos);
        throw(new SyntaxError());
    }

//    void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
//        SourcePosition pos = currentToken.position;
//        errorReporter.reportError(messageTemplate, tokenQuoted, pos);
//        SyntaxError error = new SyntaxError();
//        error.printStackTrace();
//        print(currentToken);
//        throw(error);
//    }


// ========================== PROGRAMS ========================

    public void parseProgram() {

        try {
            while (currentToken.kind != Token.EOF) {
                parseType();
                parseInitDeclList();
                switch (currentToken.kind) {
                    case Token.LPAREN:
                        accept();
                        parseFuncDecl();
                        break;
                    default:
                        match(Token.SEMICOLON);
                        break;
                }
            }
        }
        catch (SyntaxError s) {  }
    }

// ========================== DECLARATIONS ========================

    void parseFuncDecl() throws SyntaxError {
        parseParaList();
        parseCompoundStmt();
    }

    void parseVarDeclList() throws SyntaxError {
        while (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT
            || currentToken.kind == Token.BOOLEAN) {
            accept();
            parseVarDecl();
        }
    }

    void parseVarDecl() throws SyntaxError {
        parseInitDeclList();
        match(Token.SEMICOLON);
    }

    void parseInitDeclList() throws SyntaxError {
        parseInitDecl();
        while(currentToken.kind == Token.COMMA) {
            accept();
            parseInitDecl();
        }
    }

    void parseInitDecl() throws SyntaxError {
        parseDeclarator();
        if (currentToken.kind == Token.EQ) {
            acceptOperator();
            parseInitialiser();
        }
    }

    void parseInitialiser() throws SyntaxError {
        if(currentToken.kind==Token.LCURLY) {
            accept();
            parseExpr();
            while(currentToken.kind != Token.RCURLY) {
                match(Token.COMMA);
                parseExpr();
            }
            accept();
        } else {
            parseExpr();
        }
    }

    void parseParaList() throws SyntaxError {
        if (currentToken.kind == Token.RPAREN) {
            accept();
            return;
        }
        parseType();
        parseDeclarator();
        while (currentToken.kind == Token.COMMA) {
            accept();
            parseType();
            parseDeclarator();
        }
        match(Token.RPAREN);
    }

    void parseDeclarator() throws SyntaxError {
        parseIdent();
        if (currentToken.kind == Token.LBRACKET) {
            accept();
            if (currentToken.kind != Token.RBRACKET) {
                parseIntLiteral();
            }
            match(Token.RBRACKET);
        }

    }

// ======================= STATEMENTS ==============================


    void parseCompoundStmt() throws SyntaxError {
        match(Token.LCURLY);
        parseVarDeclList();
        parseStmtList();
        match(Token.RCURLY);
    }

    // Here, a new nontermial has been introduced to define { stmt } *
    void parseStmtList() throws SyntaxError {
        while (currentToken.kind != Token.RCURLY)
            parseStmt();
    }

    void parseStmt() throws SyntaxError {
        switch (currentToken.kind) {
            case Token.LCURLY:
                parseCompoundStmt();
                break;
            case Token.IF:
                parseIfStmt();
                break;
            case Token.FOR:
                parseForStmt();
                break;
            case Token.WHILE:
                parseWhileStmt();
                break;
            case Token.BREAK:
                parseBreakStmt();
                break;
            case Token.CONTINUE:
                parseContinueStmt();
                break;
            case Token.RETURN:
                parseReturnStmt();
                break;
            default:
                parseExprStmt();
                break;

        }
    }


    void parseIfStmt() throws SyntaxError {
        accept();
        match(Token.LPAREN);
        parseExpr();
        match(Token.RPAREN);
        parseStmt();
        if (currentToken.kind == Token.ELSE) {
            accept();
            parseStmt();
        }
    }

    void parseForStmt() throws SyntaxError {
        accept();
        match(Token.LPAREN);
        if (currentToken.kind != Token.SEMICOLON) {
            parseExpr();
        }
        match(Token.SEMICOLON);
        if (currentToken.kind != Token.SEMICOLON) {
            parseExpr();
        }
        match(Token.SEMICOLON);
        if (currentToken.kind != Token.RPAREN) {
            parseExpr();
        }
        match(Token.RPAREN);
        parseStmt();
    }

    void parseWhileStmt() throws SyntaxError {
        accept();
        match(Token.LPAREN);
        parseExpr();
        match(Token.RPAREN);
        parseStmt();
    }

    void parseBreakStmt() throws SyntaxError {
        match(Token.BREAK);
        match(Token.SEMICOLON);
    }

    void parseContinueStmt() throws SyntaxError {
        match(Token.CONTINUE);
        match(Token.SEMICOLON);
    }

    void parseReturnStmt() throws SyntaxError {
        accept();
        if (currentToken.kind != Token.SEMICOLON) {
            parseExpr();
        }
        match(Token.SEMICOLON);
    }


    void parseExprStmt() throws SyntaxError {
        if (currentToken.kind == Token.ID
                || currentToken.kind == Token.INTLITERAL
                || currentToken.kind == Token.FLOATLITERAL
                || currentToken.kind == Token.BOOLEANLITERAL
                || currentToken.kind == Token.MINUS
                || currentToken.kind == Token.PLUS
                || currentToken.kind == Token.NOT
                || currentToken.kind == Token.LPAREN
                || currentToken.kind == Token.STRINGLITERAL) {
            parseExpr();
            match(Token.SEMICOLON);
        } else {
            match(Token.SEMICOLON);
        }
    }


    void parseType() throws SyntaxError {
        if (!(currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT ||
            currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN)) {
            syntacticError("Expected type int/float/char/void, got \"%\"", currentToken.spelling);
        } else {
            accept();
        }
    }


// ======================= IDENTIFIERS ======================

    // Call parseIdent rather than match(Token.ID).
    // In Assignment 3, an Identifier node will be constructed in here.


    void parseIdent() throws SyntaxError {
        if (currentToken.kind == Token.ID) {
            currentToken = scanner.getToken();
        } else
            syntacticError("identifier expected here, got %",
                currentToken.spell(currentToken.kind));
    }

// ======================= OPERATORS ======================

    // Call acceptOperator rather than accept().
    // In Assignment 3, an Operator Node will be constructed in here.

    void acceptOperator() throws SyntaxError {

        currentToken = scanner.getToken();
    }


// ======================= EXPRESSIONS ======================

    void parseExpr() throws SyntaxError {
        parseAssignExpr();
    }


    void parseAssignExpr() throws SyntaxError {
        // assignment-expr     -> ( cond-or-expr "=" )* cond-or-expr
        parseCondOrExpr();
        while (currentToken.kind == Token.EQ) {
            acceptOperator();
            parseCondOrExpr();
        }
    }

    void parseAdditiveExpr() throws SyntaxError {
        parseMultiplicativeExpr();
        while(currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
            acceptOperator();
            parseMultiplicativeExpr();
        }
    }

    void parseMultiplicativeExpr() throws SyntaxError {
        parseUnaryExpr();
        while (currentToken.kind == Token.MULT || currentToken.kind == Token.DIV){
            acceptOperator();
            parseUnaryExpr();
        }
    }

    void parseUnaryExpr() throws SyntaxError {
        switch (currentToken.kind) {
            case Token.PLUS:
            case Token.MINUS:
            case Token.NOT:
                {
                    acceptOperator();
                    parseUnaryExpr();
                }
                break;
            default:
                parsePrimaryExpr();
        }
    }

    void parsePrimaryExpr() throws SyntaxError {
        switch (currentToken.kind) {
            case Token.ID:
                parseIdent();
                switch (currentToken.kind) {
                    case Token.LBRACKET:
                        accept();
                        parseExpr();
                        match(Token.RBRACKET);
                        break;
                    case Token.LPAREN:
                        parseArgList();
                        break;
                    default:
                        break;
                }
                break;
            case Token.LPAREN:
                {
                    accept();
                    parseExpr();
                    match(Token.RPAREN);
                }
                break;
            case Token.INTLITERAL:
                parseIntLiteral();
                break;
            case Token.FLOATLITERAL:
                parseFloatLiteral();
                break;
            case Token.BOOLEANLITERAL:
                parseBooleanLiteral();
                break;
            case Token.STRINGLITERAL:
                accept();
                break;
            default:
                syntacticError("illegal primary expression \"%\"", currentToken.spelling);

        }
    }

    void parseCondOrExpr() throws SyntaxError {
        parseCondAndExpr();
        while (currentToken.kind == Token.OROR) {
            acceptOperator();
            parseCondAndExpr();
        }
    }

    void parseCondAndExpr() throws SyntaxError {
        parseEqualExpr();
        while (currentToken.kind == Token.ANDAND) {
            acceptOperator();
            parseEqualExpr();
        }
    }

    void parseEqualExpr() throws SyntaxError {
        parseRelExpr();
        while(currentToken.kind == Token.EQEQ || currentToken.kind == Token.NOTEQ) {
            acceptOperator();
            parseRelExpr();
        }
    }

    void parseRelExpr() throws SyntaxError {
        parseAdditiveExpr();
        boolean looping = true;
        while(looping) {
            switch (currentToken.kind) {
                case Token.LT:
                case Token.LTEQ:
                case Token.GT:
                case Token.GTEQ:
                    acceptOperator();
                    parseAdditiveExpr();
                    break;
                default:
                    looping = false;
                    break;
            }
        }
    }

    void parseArgList() throws SyntaxError {
        accept();
        if (currentToken.kind == Token.RPAREN) {
            accept();
            return;
        }
        parseExpr();
        while (currentToken.kind == Token.COMMA) {
            accept();
            parseExpr();
        }
        match(Token.RPAREN);
    }



// ========================== LITERALS ========================

    // Call these methods rather than accept().  In Assignment 3,
    // literal AST nodes will be constructed inside these methods.

    void parseIntLiteral() throws SyntaxError {

        if (currentToken.kind == Token.INTLITERAL) {
            currentToken = scanner.getToken();
        } else
            syntacticError("integer literal expected here, got %",
                Token.spell(currentToken.kind));
    }

    void parseFloatLiteral() throws SyntaxError {

        if (currentToken.kind == Token.FLOATLITERAL) {
            currentToken = scanner.getToken();
        } else
            syntacticError("float literal expected here, got %",
                Token.spell(currentToken.kind));
    }

    void parseBooleanLiteral() throws SyntaxError {

        if (currentToken.kind == Token.BOOLEANLITERAL) {
            currentToken = scanner.getToken();
        } else
            syntacticError("boolean literal expected here, got %",
                Token.spell(currentToken.kind));
    }

}
