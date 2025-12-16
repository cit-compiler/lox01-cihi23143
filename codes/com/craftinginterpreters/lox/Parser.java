package com.craftinginterpreters.lox;

import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    // Parserの動作に必要なTokenのリストと現在の位置
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ★ ステップ1-1: エントリポイントとなる expression メソッド
    Expr expression() {
        return equality();
    }

    // ★ ステップ1-2: 結合の弱い equality 演算子 (=, !=)
    private Expr equality() {
        Expr expr = comparison(); // 次のより強い結合のメソッドを呼び出す

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // ★ ステップ2-1: comparison 演算子 (>, >=, <, <=)
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // ★ ステップ2-2: term 演算子 (+, -)
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // ★ ステップ2-3: factor 演算子 (*, /)
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // ★ ステップ2-4: unary 演算子 (!, -)
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // ★ ステップ2-5: primary 演算子（最も結合が強いもの：リテラル、括弧）
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression."); // 括弧閉じを消費
            return new Expr.Grouping(expr);
        }

        // どこにもマッチしなかった場合はエラー
        throw error(peek(), "Expect expression.");
    }
    
    // --- 補助メソッド群（ステップ1-3） ---

    // ★ match メソッド: 現在のトークンが指定された型のいずれかに一致するか確認し、一致すれば消費
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    // ★ consume メソッド: 現在のトークンが期待する型であることを保証し、消費する
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    
    // ★ check メソッド: 現在のトークンが指定された型であるかを確認 (消費はしない)
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // ★ advance メソッド: 現在のトークンを消費し、次に進む
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // ★ isAtEnd メソッド: トークンリストの終端に達したか確認
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // ★ peek メソッド: 現在のトークンを覗き見る (消費はしない)
    private Token peek() {
        return tokens.get(current);
    }

    // ★ previous メソッド: 直前に消費したトークンを返す
    private Token previous() {
        return tokens.get(current - 1);
    }

    // エラー報告用のプライベートクラスとメソッド
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private static class ParseError extends RuntimeException {}
    
    
}