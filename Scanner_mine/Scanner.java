/**
 **	Scanner.java                        
 **/

package VC.Scanner;

import VC.ErrorReporter;

import java.util.Arrays;
import java.util.List;

public final class Scanner { 

    private SourceFile sourceFile;
    private boolean debug;

    private ErrorReporter errorReporter;
    private StringBuffer currentSpelling;
    private char currentChar;
    private SourcePosition sourcePos;

    private int line;
    private int col;
    private int col_start;
    private int col_end;
    private int line_start;
    private int line_end;

    private String escapes =  "bfnrt'\"\\";
// =========================================================

    public Scanner(SourceFile source, ErrorReporter reporter) {
        sourceFile = source;
        errorReporter = reporter;
        currentChar = sourceFile.getNextChar();
        debug = false;

        line = 1;
        col = 1;
    }

    public void enableDebugging() {
        debug = true;
    }

    // accept gets the next character from the source program.

    private void accept() {
        currentSpelling.append(currentChar);
        currentChar = sourceFile.getNextChar();

        col += 1;
    }

    // inspectChar returns the n-th character after currentChar
    // in the input stream. 
    //
    // If there are fewer than nthChar characters between currentChar 
    // and the end of file marker, SourceFile.eof is returned.
    // 
    // Both currentChar and the current position in the input stream
    // are *not* changed. Therefore, a subsequent call to accept()
    // will always return the next char after currentChar.

    private char inspectChar(int nthChar) {
        return sourceFile.inspectChar(nthChar);
    }

    private int getPossibleKeyword(){
        String [] possible_keywords = {
            "true", "false", "continue", "else", "float",
            "for", "if", "int", "return", "void", "while"
        };
        int word_len = 1;
        String matched = null;

        while (Character.isLetter(inspectChar(word_len))) { word_len += 1;}

        for (String possible : possible_keywords) {
            if (possible.length() != word_len) continue;
            boolean match = true;
            for (int i=1; i<word_len; ++i) {
                if (inspectChar(i) != possible.charAt(i)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matched = possible;
                break;
            }
        }
        if (matched != null) {
            for (int i=0; i<matched.length(); ++i) accept();
            switch (matched) { 
                case "true": 
                case "false":
                    return Token.BOOLEANLITERAL;
                case "boolean":
                    return Token.BOOLEAN;
                case "break":
                    return Token.BREAK;
                case "continue":
                    return Token.CONTINUE;
                case "else":
                    return Token.ELSE;
                case "float":
                    return Token.FLOAT;
                case "for":
                    return Token.FOR;
                case "if":
                    return Token.IF;
                case "int":
                    return Token.INT;
                case "return":
                    return Token.RETURN;
                case "void":
                    return Token.VOID;
                case "while":
                    return Token.WHILE;
            }
        } else {
            consumeId();
        }

        return Token.ID;
    }

    private int getPossibleNum() {
        // TODO
        int type = currentChar == '.' ? Token.FLOATLITERAL : Token.INTLITERAL;
        int literal_len = 1;
        boolean is_sci_notation = false;
        boolean dot_appear = (currentChar == '.');

        while (true) {
            if (inspectChar(literal_len) == '.') {
                if (dot_appear || is_sci_notation) break;
                type = Token.FLOATLITERAL;
            } else if (Character.toUpperCase(inspectChar(literal_len)) == 'E') {
                // for *.* [Ee] *.*
                if (is_sci_notation == true) break;
                if (inspectChar(literal_len+1) == '.' 
                        || inspectChar(literal_len+1) == '+'
                        || inspectChar(literal_len+1) == '-') {
                    if (!Character.isDigit(inspectChar(literal_len+2))) {
                        break;
                    } else {
                        is_sci_notation = true;
                        type = Token.FLOATLITERAL;
                        literal_len += 2;
                        continue;
                    }
                } else {
                    type = Token.FLOATLITERAL;
                    is_sci_notation = true;
                }
            } else if (Character.isDigit(inspectChar(literal_len))) {

            } else break;
            literal_len++;
        }
        for (int i=0; i<literal_len; ++i) accept();
        return type;
    }

    private void consumeId () {
        while (Character.isDigit(currentChar) ||
                Character.isLetter(currentChar) ||
                currentChar == '_') {
            accept();
        }
    }

    private int getString() {
        col_start = col;
        col += 1;
        currentChar = sourceFile.getNextChar();

        while (true){
            if (currentChar == '\\') {
                String escape = "" + '\\' + inspectChar(1);

                col += 1;
                switch (inspectChar(1)) {
                    case 'b':
                        currentChar = '\b';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case 'f':
                        currentChar = '\f';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case 'n':
                        currentChar = '\n';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case 'r':
                        currentChar = '\r';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case 't':
                        currentChar = '\t';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case '\'':
                        currentChar = '\'';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case '\"':
                        currentChar = '\"';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    case '\\':
                        currentChar = '\\';
                        accept();
                        currentChar = sourceFile.getNextChar();
                        break;
                    default:
                        col -= 1;
                        System.out.printf("ERROR: %d(%d)..%d(%d): %s: illegal escape character\n",
                            line, col_start, line, col, escape);
                        accept();
                        accept();
                        break;
                }
            } else {
                if (currentChar == '"') {
                    currentChar = sourceFile.getNextChar();
                    col += 1;
                    break;
                }
                if (currentChar == '\n') {
                    System.out.printf("ERROR: %d(%d)..%d(%d): comp9102: unterminated string\n",
                        line, col_start, line, col_start); 
                    break;
                }
                accept();
            }
            
        }

        return Token.STRINGLITERAL;
    }

    private int nextToken() {
      // Tokens: separators, operators, literals, identifiers and keyworods
        if (Character.isLetter(currentChar)) return getPossibleKeyword();
        if (Character.isDigit(currentChar) || 
                (currentChar == '.' && Character.isDigit(inspectChar(1)))) {
            return getPossibleNum();
        }
        if (currentChar == '"') {
            return getString();
        }

        switch (currentChar) {
            // separators 
            case '+':
                accept();
                return Token.PLUS;
            case '-':
                accept();
                return Token.MINUS;
            case '*':
                accept();
                return Token.MULT;
            case '/':
                accept();
                return Token.DIV;
            case '!':
                accept();
                if (currentChar == '=') {
                    accept();
                    return Token.NOTEQ;
                }
                return Token.NOT;
            case '=':
                accept();
                if (currentChar == '=') {
                    accept();
                    return Token.EQEQ;
                }
                return Token.EQ;
            case '<':
                accept();
                if (currentChar == '=') {
                    accept();
                    return Token.LTEQ;
                }
                return Token.LT;
            case '>':
                accept();
                if (currentChar == '=') {
                    accept();
                    return Token.GTEQ;
                }
                return Token.GT;
            case '&':
                accept();
                if(currentChar=='&') {
                    accept();
                    return Token.ANDAND;
                }
                else return Token.ERROR;
            case '|':
                accept();
                if(currentChar=='|') {
                    accept();
                    return Token.OROR;
                }
                else return Token.ERROR;
            case '{':
                accept();
                return Token.LCURLY;
            case '}':
                accept();
                return Token.RCURLY;
            case '(':
                accept();
                return Token.LPAREN;
            case ')':
                accept();
                return Token.RPAREN;
            case '[':
                accept();
                return Token.LBRACKET;
            case ']':
                accept();
                return Token.RBRACKET;
            case ';':
                accept();
                return Token.SEMICOLON;
            case ',':
                accept();
                return Token.COMMA;
            case Token.EOF:
                    currentSpelling.append(Token.spell(Token.EOF));
                    col += 1;
                    return Token.EOF;
            default:
                    break;
            }

            accept(); 
            return Token.ERROR;
    }

    void skipSpaceAndComments() {
        while (currentChar == ' ' || currentChar == '\n' 
                || (currentChar == '/' && (inspectChar(1) == '/' || inspectChar(1) == '*'))
                || currentChar == '\t') {
            if (currentChar == '/') {
                if(inspectChar(1) == '/') {
                    while (currentChar != '\n') {
                        currentChar = sourceFile.getNextChar();
                    }
                    col = 1;
                } else if (inspectChar(1) == '*') {
                    while (currentChar != '*' || inspectChar(1) != '/') {
                        if (currentChar == '\n') {
                            line += 1;
                            col = 1;
                        }
                        currentChar = sourceFile.getNextChar();
                        col += 1;
                    }
                    currentChar = sourceFile.getNextChar();
                    currentChar = sourceFile.getNextChar();
                    col += 2;
                }
            }

            if(currentChar == '\n') {
                line++;
                col = 1;
                currentChar = sourceFile.getNextChar();
            } else if (currentChar == '\t') {
                col += 8 - (col % 8) + 1;
                currentChar = sourceFile.getNextChar();
            } else if (currentChar == ' '){
                col += 1;
                currentChar = sourceFile.getNextChar();
            }
        }

    }

    public Token getToken() {
        Token tok;
        int kind;

        // skip white space and comments

        skipSpaceAndComments();

        currentSpelling = new StringBuffer("");

       // You must record the position of the current token somehow
        col_start = col;
        line_start = line;
        kind = nextToken();
        col_end = col - 1;
        line_end = line;
        sourcePos = new SourcePosition(line_end, col_start, col_end);
        tok = new Token(kind, currentSpelling.toString(), sourcePos);

       // * do not remove these three lines
        if (debug)
           System.out.println(tok);
       return tok;
    }

}
