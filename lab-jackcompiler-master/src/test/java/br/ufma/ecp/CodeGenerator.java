package br.ufma.ecp;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import br.ufma.ecp.Parser;
import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;
public class CodeGenerator extends TestSupport{
    @Test
    public void testInt () {
        var input = """
            10 + 20 + 30
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression(); // Modify the access modifier of the parseExpression() method to make it visible
        String actual = parser.VMOutput();
        String expected = """
                push constant 10       
                    """;
            assertEquals(expected, actual);
    }
    @Test
    public void testLiteralString () {
        var input = """
            "OLA"
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 3
                call String.new 1
                push constant 79
                call String.appendChar 2
                push constant 76
                call String.appendChar 2
                push constant 65
                call String.appendChar 2
                    """;
            assertEquals(expected, actual);
    }
    @Test
    public void testFalse () {
        var input = """
            false
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0       
                    """;
            assertEquals(expected, actual);
    }

    @Test
    public void testNull () {
        var input = """
            null
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0       
                    """;
            assertEquals(expected, actual);
    }


    @Test
    public void testTrue () {
        var input = """
            true
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                not       
                    """;
            assertEquals(expected, actual);
    }


    @Test
    public void testThis () {
        var input = """
            this
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push pointer 0
                    """;
            assertEquals(expected, actual);
    }
}