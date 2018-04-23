lexer grammar GalagoTypeBuilder;
@header {
  package org.lemurproject.galago.tupleflow.typebuilder;
}

T8 : 'bytes' ;
T9 : 'boolean' ;
T10 : 'int' ;
T11 : 'long' ;
T12 : 'short' ;
T13 : 'byte' ;
T14 : 'float' ;
T15 : 'double' ;
T16 : 'String' ;
T17 : ';' ;
T18 : '+' ;
T19 : '-' ;
T20 : 'order:' ;
T21 : '.' ;
T22 : 'package' ;
T23 : 'type' ;
T24 : '{' ;
T25 : '}' ;

// $ANTLR src "/u/data/u87/arayabha/galago-3.12/tupleflow-typebuilder/src/main/antlr/org/lemurproject/galago/tupleflow/typebuilder/GalagoTypeBuilder.g" 72
ID  :   ('a'..'z'|'A'..'Z'|'_'|'0'..'9')+ ;
// $ANTLR src "/u/data/u87/arayabha/galago-3.12/tupleflow-typebuilder/src/main/antlr/org/lemurproject/galago/tupleflow/typebuilder/GalagoTypeBuilder.g" 73
NEWLINE:'\r'? '\n' {skip();} ;
// $ANTLR src "/u/data/u87/arayabha/galago-3.12/tupleflow-typebuilder/src/main/antlr/org/lemurproject/galago/tupleflow/typebuilder/GalagoTypeBuilder.g" 74
WS  :   (' '|'\t')+ {skip();} ;
// $ANTLR src "/u/data/u87/arayabha/galago-3.12/tupleflow-typebuilder/src/main/antlr/org/lemurproject/galago/tupleflow/typebuilder/GalagoTypeBuilder.g" 75
COMMENT : '/' '/' (~('\n'|'\r'))* {skip();} ;
