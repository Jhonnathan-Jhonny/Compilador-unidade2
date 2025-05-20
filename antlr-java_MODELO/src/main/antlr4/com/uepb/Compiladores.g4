grammar Compiladores;

// --- Tokens: palavras-chave e operadores ---
VAR     : 'var';
PRINT   : 'print';
INPUT   : 'input';
IF      : 'if';
ELSE    : 'else';
WHILE   : 'while';
AND     : 'and';
OR      : 'or';
NOT     : 'not';
TRUE    : 'true';
FALSE   : 'false';

// --- Símbolos e operadores ---
LPAREN  : '(';
RPAREN  : ')';
LKEY    : '{';
RKEY    : '}';
EQUALS  : '=';
SEMICOLON: ';';
COMMA   : ',';
PLUS    : '+';
MINUS   : '-';
MULT    : '*';
DIV     : '/';
POW     : '^';
LT      : '<';
GT      : '>';
EQ      : '==';
NOTEQ   : '!=';

// --- Literais e identificadores ---
NUMBER  : [0-9]+('.'[0-9]+)?;
STRING  : '"' .*? '"';
ID      : [a-zA-Z_][a-zA-Z0-9_]*;

// --- Espaços e comentários ---
WS      : [ \t\r\n]+ -> skip;
COMMENT : '//' ~[\r\n]* -> skip;

// --- Regras do Parser ---
program: statement* EOF;

statement
    : varDeclaration SEMICOLON
    | assignment SEMICOLON
    | printStatement SEMICOLON
    | inputStatement SEMICOLON
    | ifStatement
    | whileStatement
    | SEMICOLON
    | block;

block: LKEY statement* RKEY;

varDeclaration: VAR ID (EQUALS expr)?;

assignment: ID EQUALS expr;

printStatement: PRINT LPAREN expr RPAREN;

inputStatement: INPUT LPAREN ID RPAREN;

ifStatement: IF LPAREN expr RPAREN statement (ELSE statement)?;

whileStatement: WHILE LPAREN expr RPAREN statement;

// --- Expressões ---
expr: logicalOrExpr;

logicalOrExpr: logicalAndExpr (OR logicalAndExpr)*;

logicalAndExpr: equalityExpr (AND equalityExpr)*;

equalityExpr: relationalExpr ((EQ | NOTEQ) relationalExpr)?;

relationalExpr: additiveExpr ((LT | GT) additiveExpr)?;

additiveExpr: multiplicativeExpr ((PLUS | MINUS) multiplicativeExpr)*;

multiplicativeExpr: powerExpr ((MULT | DIV) powerExpr)*;

powerExpr: unaryExpr (POW powerExpr)?;

unaryExpr: (PLUS | MINUS | NOT)* primaryExpr;

primaryExpr
    : NUMBER
    | ID
    | STRING
    | booleanLiteral
    | LPAREN expr RPAREN;

booleanLiteral: TRUE | FALSE;
