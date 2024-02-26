# Projeto Lab-JackCompiler
Alunos: João José Penha Sousa e Lucas Salustriano dos Santos

Este projeto é uma implementação de um compilador para a linguagem Jack, uma linguagem de programação de alto nível usada no curso "Nand to Tetris". O compilador é composto por duas partes principais: um analisador léxico (também conhecido como scanner) e um analisador sintático (também conhecido como parser).

## Analisador Léxico (Scanner)

O analisador léxico é a primeira fase de um compilador. Sua principal função é ler a entrada de caracteres do código-fonte, agrupá-los em lexemas e produzir como saída uma sequência de tokens. Cada token é uma estrutura de dados que contém um atributo que identifica o tipo de lexema (por exemplo, identificador, número, operador) e o próprio lexema.

No nosso projeto, a classe `Scanner` é responsável por essa tarefa. Ela lê a entrada de caracteres, ignora espaços em branco e comentários, e identifica os tokens. A classe `Scanner` também contém um mapa de palavras-chave para identificar rapidamente se um lexema é uma palavra-chave da linguagem Jack.

## Analisador Sintático (Parser)

O analisador sintático é a segunda fase de um compilador. Ele pega a sequência de tokens produzida pelo analisador léxico e tenta gerar uma árvore de análise sintática, verificando se a sequência de tokens pode ser gerada pela gramática da linguagem.

No nosso projeto, a classe `Parser` seria responsável por essa tarefa. Embora o código para o `Parser` não esteja presente nos arquivos referenciados, ele normalmente trabalharia em conjunto com o `Scanner` para produzir a árvore de análise sintática.

## Testes

Os testes são uma parte crucial de qualquer projeto de software. Eles garantem que o código funciona como esperado e ajudam a prevenir a introdução de bugs. No nosso projeto, a classe `ScannerTest` contém vários testes para o `Scanner`.

## Conclusão

Este projeto é um excelente exemplo de como um compilador funciona internamente. Embora seja um projeto simples, ele abrange muitos dos conceitos fundamentais da teoria da compilação. Se você está interessado em compiladores, linguagens de programação ou ciência da computação em geral, este projeto é um ótimo lugar para começar a aprender.
