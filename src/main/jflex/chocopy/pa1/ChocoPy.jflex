package chocopy.pa1;
import java_cup.runtime.*;
import java.util.Stack;
%%

/*** Do not change the flags below unless you know what you are doing. ***/

%unicode
%line
%column

%class ChocoPyLexer
%public

%cupsym ChocoPyTokens
%cup
%cupdebug

%eofclose false
%states INDENTSTATE

/*** Do not change the flags above unless you know what you are doing. ***/

/* The following code section is copied verbatim to the
 * generated lexer class. */
%{
    /* The code below includes some convenience methods to create tokens
     * of a given type and optionally a value that the CUP parser can
     * understand. Specifically, a lot of the logic below deals with
     * embedded information about where in the source code a given token
     * was recognized, so that the parser can report errors accurately.
     * (It need not be modified for this project.) */

    /** Producer of token-related values for the parser. */
    final ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();

    /** Return a terminal symbol of syntactic category TYPE and no
     *  semantic value at the current source location. */
    private Symbol symbol(int type) {
        return symbol(type, yytext());
    }

    /** Return a terminal symbol of syntactic category TYPE and semantic
     *  value VALUE at the current source location. */
    private Symbol symbol(int type, Object value) {
        return symbolFactory.newSymbol(ChocoPyTokens.terminalNames[type], type,
            new ComplexSymbolFactory.Location(yyline + 1,yycolumn + 1),
            new ComplexSymbolFactory.Location(yyline + 1,yycolumn + yylength()),
            value);
    }

    private Symbol stringsymbol(int type, String s) {
        StringBuffer ns=new StringBuffer();
        for(int i=0; i<s.length(); i++){
            if(s.charAt(i) == '\\' && s.charAt(i+1) == 'n'){
               ns.append('\n');
               i++;
            }else if(s.charAt(i) == '\\' && s.charAt(i+1) == 't'){
               ns.append('\t');
               i++;
            }else if(s.charAt(i) == '\\' && s.charAt(i+1) == '"'){
               ns.append('\"');
               i++;
            }else if(s.charAt(i) == '\\' && s.charAt(i+1) == '\\'){
               ns.append('\\');
               i++;
            }else{
               ns.append(s.charAt(i));
            }
        }
        return symbolFactory.newSymbol(ChocoPyTokens.terminalNames[type], type,
            new ComplexSymbolFactory.Location(yyline + 1,yycolumn + 1),
            new ComplexSymbolFactory.Location(yyline + 1,yycolumn + yylength()),
            ns.toString());
    }


    private int ws=0; // Number of white spaces. To determine whether to output
                      // NEWLINE token in the begining of the program.

    /* Indentation handling */
    // indent : (int) storing current indent level, start from 0. 
    // indents: (Stack<int>) storing indent levels that have been seen so far (
    //                       exclude the latest one which is stored in "indent")
    //
    // boolean addIndent(int col): (fun) to determine whether current line has
    //                              an indentation level that needs to update
    //                              the current indentation ("indent") and push 
    //                              the old one to "indents" stack.
    //
    // boolean rmIndent(int col) : (fun) to determine whether current line has
    //                              an indentation level that needs to pop out
    //                              "indents" and set the current indentation 
    //                              ("indent") as the value popped out. 
    private int indent = 0;
    private Stack<Integer> indents = new Stack<Integer>();
    private boolean addIndent(int col) {
        if (this.indent < col) {
            this.indents.push(this.indent);
            this.indent = col;
            return true;
        }
        return false;
    }

    private boolean rmIndent(int col) {
        if (this.indent > col) {
            this.indent = this.indents.pop();
                return true;
        }
        return false;
    }

%}

/* Macros (regexes used in rules below) */
letter = [A-Za-z_]
digit  = [0-9]
WhiteSpace = [ \t]
LineBreak  = \r|\n|\r\n

IntegerLiteral = 0 | [1-9][0-9]*
Identifier = {letter}({letter}|{digit})*
Comment = "#".*
IDString = \"{Identifier}\"
String = \"([^"\\"\"] | "\\t" | "\\n" | "\\\\" | "\\\"")+\" | \"{WhiteSpace}*\"
InvalidString = \"[^\"]*\"

%%


<YYINITIAL> {

  /* Delimiters. */
 ^{WhiteSpace}*{Comment}{LineBreak} {yybegin(INDENTSTATE);}
  {LineBreak}                 { yybegin(INDENTSTATE);

                                /* Only return NEWLINE token if: */
                                //   (1) it is not the first line of the file
                                //   (2) if it is in the first line, {LineBreak} 
                                //       is not the first non-whitespaces char. 
                                if((yyline>0) || (yycolumn-ws>0)){
                                    ws=0;
                                    return symbol(ChocoPyTokens.NEWLINE);
                                } 
                              }

  /* Kerywords */
  "False"                     { return symbol(ChocoPyTokens.FALSE, false); }
  "None"                      { return symbol(ChocoPyTokens.NONE); }
  "True"                      { return symbol(ChocoPyTokens.TRUE, true); }
  "and"                       { return symbol(ChocoPyTokens.AND); }
  "as"                        { return symbol(ChocoPyTokens.AS); }
  "assert"                    { return symbol(ChocoPyTokens.ASSERT); }
  "async"                     { return symbol(ChocoPyTokens.ASYNC); }
  "await"                     { return symbol(ChocoPyTokens.AWAIT); }
  "break"                     { return symbol(ChocoPyTokens.BREAK); }
  "class"                     { return symbol(ChocoPyTokens.CLASS); }
  "continue"                  { return symbol(ChocoPyTokens.CONTINUE); }
  "def"                       { return symbol(ChocoPyTokens.DEF); }
  "del"                       { return symbol(ChocoPyTokens.DEL); }
  "elif"                      { return symbol(ChocoPyTokens.ELIF); }
  "else"                      { return symbol(ChocoPyTokens.ELSE); }
  "except"                    { return symbol(ChocoPyTokens.EXCEPT); }
  "finally"                   { return symbol(ChocoPyTokens.FINALLY); }
  "for"                       { return symbol(ChocoPyTokens.FOR); }
  "from"                      { return symbol(ChocoPyTokens.FROM); }
  "global"                    { return symbol(ChocoPyTokens.GLOBAL); }
  "if"                        { return symbol(ChocoPyTokens.IF); }
  "import"                    { return symbol(ChocoPyTokens.IMPORT); }
  "in"                        { return symbol(ChocoPyTokens.IN); }
  "is"                        { return symbol(ChocoPyTokens.IS); }
  "lambda"                    { return symbol(ChocoPyTokens.LAMBDA); }
  "nonlocal"                  { return symbol(ChocoPyTokens.NONLOCAL); }
  "not"                       { return symbol(ChocoPyTokens.NOT); }
  "or"                        { return symbol(ChocoPyTokens.OR); }
  "pass"                      { return symbol(ChocoPyTokens.PASS); }
  "raise"                     { return symbol(ChocoPyTokens.RAISE); }
  "return"                    { return symbol(ChocoPyTokens.RETURN); }
  "try"                       { return symbol(ChocoPyTokens.TRY); }
  "while"                     { return symbol(ChocoPyTokens.WHILE); }
  "with"                      { return symbol(ChocoPyTokens.WITH); }
  "yield"                     { return symbol(ChocoPyTokens.YIELD); }

  /* Literals. */
  {IntegerLiteral}            { 
                                /* Overflow integer handling */
                                try{ 
                                
                                  // Becasue integer value between -2^31 and (2^31 - 1)
                                  // If number value > 2^31 => <overflow integer> error
                                  // Rest of them let semantic check handle
                                  long l = Long.parseLong(yytext());
                                  long intMaxLongValue = Integer.MAX_VALUE;
                                  if ( l > intMaxLongValue + 1L) {
		                                throw new NumberFormatException("overflow integer");
		                              }
		                              
                                  return symbol(ChocoPyTokens.INTEGER,l);
                                  
                                }catch(NumberFormatException ex){
                                  return symbol(ChocoPyTokens.UNRECOGNIZED, 
                                                          "<overflow integer>");
                                }
                              }

  /* Operators. */
  "+"                         { return symbol(ChocoPyTokens.PLUS  , yytext()); }
  "-"                         { return symbol(ChocoPyTokens.MINUS , yytext()); }
  "*"                         { return symbol(ChocoPyTokens.MULT  , yytext()); }
  "//"                        { return symbol(ChocoPyTokens.DIV   , yytext()); }
  "%"                         { return symbol(ChocoPyTokens.MOD   , yytext()); }
  ">"                         { return symbol(ChocoPyTokens.GT    , yytext()); }
  "<"                         { return symbol(ChocoPyTokens.LT    , yytext()); }
  ">="                        { return symbol(ChocoPyTokens.GE    , yytext()); }
  "<="                        { return symbol(ChocoPyTokens.LE    , yytext()); }
  "=="                        { return symbol(ChocoPyTokens.EQEQ  , yytext()); }
  "!="                        { return symbol(ChocoPyTokens.NEQ   , yytext()); }
  "="                         { return symbol(ChocoPyTokens.EQ    , yytext()); }
  "("                         { return symbol(ChocoPyTokens.LPAREN, yytext()); }
  ")"                         { return symbol(ChocoPyTokens.RPAREN, yytext()); }
  "["                         { return symbol(ChocoPyTokens.LINDEX, yytext()); }
  "]"                         { return symbol(ChocoPyTokens.RINDEX, yytext()); }
  ","                         { return symbol(ChocoPyTokens.COMMA , yytext()); }
  ":"                         { return symbol(ChocoPyTokens.COLON , yytext()); }
  "."                         { return symbol(ChocoPyTokens.DOT   , yytext()); }
  "->"                        { return symbol(ChocoPyTokens.ARROW , yytext()); }


  /* Whitespace. */
  {WhiteSpace}                { ws++;
                                if(yyline==0 && yycolumn==0 ) {
                                    yypushback(yylength()); 
                                    yybegin(INDENTSTATE);
                                }
                              }

  /* Identifier */
  {Identifier}                { return symbol(ChocoPyTokens.IDENTIFIER ,
                                                                    yytext()); }
  /* String */
  {IDString}                  { return symbol(ChocoPyTokens.IDSTRING, 
                                                                     yytext());}
  {String}                    { return stringsymbol(ChocoPyTokens.STRING, 
                                                          yytext().toString());}
  {InvalidString}             { return symbol(ChocoPyTokens.UNRECOGNIZED, 
                                                                     yytext());}
  {Comment}                   { /* ignore */ }
}

<INDENTSTATE>{
  {WhiteSpace}+               {
                                int col = 0;
                                /* Indentation contains tabs */
                                // If a '\t' is seen, then one need to replace
                                // it with whitespaces such that the up until 
                                // now whitespaces have number be a multiple of
                                // 8.
                                for (int i = 0; i < yylength(); i++) {
                                    if(yycharat(i)==' '){
                                        col++;
                                    }else{
                                        col= (col==0) ? 8:((col+8)/8)*8; 
                                    }
                                }
                                if(addIndent(col)){
                                   yybegin(YYINITIAL);
                                   return symbol(ChocoPyTokens.INDENT);
                                }else{
                                   if (rmIndent(col)){
                                       // the indentation level in the current 
                                       // line ("col") should be greater than the 
                                       // indentation after DEDENT since 
                                       // otherwise "col" will be a line that has 
                                       // a invalid indentation level:
                                       // e.g. 
                                       // for(1):
                                       //     x = 2
                                       //     for (2):
                                       //         y = 3
                                       //       z = 4 #this.indent=4; col=6; 
                                       // for such case, we should not output 
                                       // DEDENT instead of return an error.
                                       if(this.indent < col){
                                           return symbol(
                                                    ChocoPyTokens.UNRECOGNIZED, 
                                                    "<bad indentation>");
                                       }else{
                                           yypushback(yylength());
                                           return symbol(ChocoPyTokens.DEDENT);
                                       }
                                   } else{
                                       yybegin(YYINITIAL);
                                   }
                                }
                              }
  {WhiteSpace}*{LineBreak}    { /* ignore */ }
  {WhiteSpace}*{Comment}      { /* ignore */ }
  \S                          {
                                /* NEWLINE follows with a non-{WhiteSpace} char*/
                                // (1) pushback the buffer by 1
                                // (2) check whether there are any dendentation
                                //     that needs to do
                                yypushback(1);
                                if(rmIndent(0)){
                                    return symbol(ChocoPyTokens.DEDENT);
                                }else{
                                    yybegin(YYINITIAL);
                                }
                              }
}

<<EOF>>                       { 
                                /* EOF with nonempty indentation stack */ 
                                // Modify the variable zzAtEOF so that the lexer
                                // will keep returning DEDENT token until indent
                                // stack is emptied. 
    							if (rmIndent(0)) {
                                    zzAtEOF = false;
        							return symbol(ChocoPyTokens.DEDENT);
    							}else{
									return symbol(ChocoPyTokens.EOF); 
								}
							  }

/* Error fallback. */
[^]                           { return symbol(ChocoPyTokens.UNRECOGNIZED); }
