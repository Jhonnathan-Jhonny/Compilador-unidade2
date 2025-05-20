grammar Compiladores;

// — Tokens
// Palavras-chave e operadores (ordem importa)
VAR: 'var';
PRINT: 'print';
INPUT: 'input';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
AND: 'and';
OR: 'or';
NOT: 'not';
TRUE: 'true';
FALSE: 'false';

// Símbolos e operadores aritméticos/lógicos
LPAREN: '(';
RPAREN: ')';
LKEY: '{';
RKEY: '}';
EQUALS: '=';
SEMICOLON: ';';
COMMA: ',';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
POW: '^';
LT: '<';
GT: '>';
EQ: '==';
NOTEQ: '!=';

NUMBER: [0-9]+('.'[0-9]+)?;
STRING: '"' .*? '"';
ID: [a-zA-Z_][a-zA-Z0-9_]*;

WS: [ \t\r\n]+ -> skip;
COMMENT: '//' ~[\r\n]* -> skip;

// --- Regras do Parser ---
program: (statement | COMMENT)* EOF;



// tipos de instruções
statement:
    varDeclaration SEMICOLON       // var x = 5;
    | assignment SEMICOLON          // x = x + 1;
    | printStatement SEMICOLON    // print(x);
    | inputStatement SEMICOLON   // input(x);
    | ifStatement                               // if (...)
    | whileStatement                        // while (...)
    | SEMICOLON
    | block;                                       // { ... }

block: LKEY (statement)* RKEY;

varDeclaration: VAR ID (EQUALS expr)?;

assignment: ID EQUALS expr;

printStatement: PRINT LPAREN expr RPAREN;

inputStatement: INPUT LPAREN ID RPAREN;

ifStatement: IF LPAREN expr RPAREN statement (ELSE statement)?;

whileStatement: WHILE LPAREN expr RPAREN statement;
// EXPRESSÕES
// Ordem de precedência (da menor para a maior):

expr: logicalOrExpr;

logicalOrExpr: logicalAndExpr (OR logicalAndExpr)*;

logicalAndExpr: equalityExpr (AND equalityExpr)*;

equalityExpr: relationalExpr ((EQ | NOTEQ) relationalExpr)?;

relationalExpr: additiveExpr ((LT | GT) additiveExpr)?;

additiveExpr: multiplicativeExpr ((PLUS | MINUS) multiplicativeExpr)*;

multiplicativeExpr: powerExpr ((MULT | DIV) powerExpr)*;

powerExpr: unaryExpr (POW powerExpr)?;

unaryExpr: (PLUS | MINUS | NOT)* primaryExpr;

primaryExpr:
    NUMBER
    | ID
    | STRING
    | booleanLiteral
    | LPAREN expr RPAREN;

booleanLiteral: TRUE | FALSE;
