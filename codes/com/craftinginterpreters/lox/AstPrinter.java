package com.craftinginterpreters.lox;

// AstPrinter クラスは Expr.Visitor<String> を実装する
class AstPrinter implements Expr.Visitor<String> {

    // 3. print メソッドを追加
    String print(Expr expr) {
        return expr.accept(this);
    }

    // 4. 式の要素型のための visit メソッドを追加
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        // JavaのObjectを文字列化
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // 5. parenthesize メソッドを追加
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            // ASTノードを再帰的に訪問 (Visitorパターン)
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
    
    // 6. main メソッドを追加 (AstPrinter単体でのデバッグ実行用)
    public static void main(String[] args) {
        // (1 + 2) * -(4 - 3) の AST を手動で構築する例
        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)
            ),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Literal(45.67)
            )
        );

        // 出力例: (* (- 123) (group 45.67))
        System.out.println(new AstPrinter().print(expression));
    }
}